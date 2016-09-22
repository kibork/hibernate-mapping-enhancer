package org.hibernate.enhanced;

import org.hibernate.boot.jaxb.internal.MappingBinder;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitIndexNameSource;
import org.hibernate.boot.model.source.internal.hbm.MappingDocument;
import org.hibernate.boot.spi.AdditionalJaxbMappingProducer;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.mapping.*;
import org.hibernate.mapping.Collection;
import org.hibernate.type.CustomType;
import org.hibernate.type.EnumType;
import org.jboss.jandex.IndexView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by kibork on 8/25/16.
 */
public class EnhancedMappingService implements AdditionalJaxbMappingProducer {

    // ------------------ Constants  --------------------

    private static final CoreMessageLogger log = CoreLogging.messageLogger( EnhancedMappingService.class );

    // ------------------ Fields     --------------------

    private Configuration configuration = null;

    // ------------------ Properties --------------------

    // ------------------ Logic      --------------------


    @Override
    public java.util.Collection<MappingDocument> produceAdditionalMappings(MetadataImplementor metadata, IndexView jandexIndex, MappingBinder mappingBinder, MetadataBuildingContext buildingContext) {
        if (configuration == null) {
            configuration = new Configuration(buildingContext.getClassLoaderAccess());
        }

        processMetadata(metadata, buildingContext);

        return Collections.emptyList();
    }

    private void processMetadata(MetadataImplementor metadata, MetadataBuildingContext buildingContext) {
        for (final PersistentClass persistentClass : metadata.getEntityBindings()) {
            processTableForeignKeys(persistentClass.getTable());
            final Iterator propertyIterator = persistentClass.getPropertyIterator();
            while (propertyIterator.hasNext()) {
                final Object propertyObject = propertyIterator.next();
                if (propertyObject instanceof Property) {
                    final Property property = (Property)propertyObject;
                    final Value value = property.getValue();
                    if (value.getType().isCollectionType()) {
                        final Collection collectionValue = (Collection)value;
                        processTableForeignKeys(collectionValue.getCollectionTable());
                    } else if (value.getType() instanceof CustomType) {
                        final CustomType customType = (CustomType)value.getType();
                        if (customType.getUserType() instanceof EnumType) {
                            processEnumProperty(persistentClass, property, metadata, buildingContext);
                        }
                    }
                }
            }
        }
    }

    private void processEnumProperty(PersistentClass persistentClass, Property property, MetadataImplementor metadata, MetadataBuildingContext buildingContext) {
        final ArrayList<String> columnNames = new ArrayList<>();
        property.getColumnIterator().forEachRemaining(c -> columnNames.add(((Column)c).getName()));
        for (final String columnName : columnNames) {
            final String fullColumnName = persistentClass.getTable().getName() + "." + columnName;
            if (!configuration.isEnumColumnExcludedFromIndexes(fullColumnName)) {
                final Index index = new Index();

                final String indexName = generateIndexName(persistentClass, metadata, buildingContext, columnNames);
                index.setName(indexName);

                index.addColumns(property.getColumnIterator());
                index.setTable(property.getPersistentClass().getTable());
                log.infof("Added index on table %s with column %s to enhance Enum property %s" ,
                        property.getPersistentClass().getTable().getName(), columnName, property.toString()

                );
                addIndex(index, persistentClass.getTable());
            } else {
                log.infof("Skip creating index on table %s with column %s to enhance Enum property %s as it's excluded" ,
                        property.getPersistentClass().getTable().getName(), columnName, property.toString()

                );
            }
        }
    }

    private static java.util.List<String> getColumnNames(final Index index) {
        final java.util.List<String> columnNames = new ArrayList<>();
        final Iterator<Column> columnIterator = index.getColumnIterator();
        while (columnIterator.hasNext()) {
            columnNames.add(columnIterator.next().getName());
        }
        return columnNames;
    }

    static void addIndex(final Index index, final Table table) {
        final String newIndexName = index.getName();
        final java.util.List<String> newIndexColumns = getColumnNames(index);

        final Iterator<Index> indexIterator = table.getIndexIterator();
        while (indexIterator.hasNext()) {
            final Index existingIndex = indexIterator.next();
            if (existingIndex.getName().equals(newIndexName)) {
                return;
            }
            if (getColumnNames(existingIndex).equals(newIndexColumns)) {
                return;
            }
        }

        table.addIndex(index);
    }

    private String generateIndexName(final PersistentClass persistentClass, final MetadataImplementor metadata, final MetadataBuildingContext buildingContext, final ArrayList<String> columnNames) {
        final Identifier keyNameIdentifier = buildingContext.getBuildingOptions().getImplicitNamingStrategy().determineIndexName(
                new ImplicitIndexNameSource() {
                    @Override
                    public MetadataBuildingContext getBuildingContext() {
                        return buildingContext;
                    }

                    @Override
                    public Identifier getTableName() {
                        return persistentClass.getTable().getNameIdentifier();
                    }

                    private java.util.List<Identifier> columnNameIdentifiers;

                    @Override
                    public java.util.List<Identifier> getColumnNames() {
                        // be lazy about building these
                        if ( columnNameIdentifiers == null ) {
                            columnNameIdentifiers = toIdentifiers( columnNames.toArray(new String[columnNames.size()]) );
                        }
                        return columnNameIdentifiers;
                    }

                    private java.util.List<Identifier> toIdentifiers(String[] names) {
                        if ( names == null ) {
                            return Collections.emptyList();
                        }

                        final java.util.List<Identifier> columnNames = CollectionHelper.arrayList( names.length );
                        for ( String name : names ) {
                            columnNames.add(metadata.getDatabase().toIdentifier( name ) );
                        }
                        return columnNames;
                    }
                }

        );
        return keyNameIdentifier.render(metadata.getDatabase().getJdbcEnvironment().getDialect() );
    }

    private void processTableForeignKeys(Table table) {
        if (table == null) {
            return;
        }
        for (final ForeignKey key : table.getForeignKeys().values()) {
            final ArrayList<String> columnNames = new ArrayList<>();
            key.getColumnIterator().forEachRemaining(c -> columnNames.add(c.getName()));
            final String columnNameList = columnNames.stream().collect(Collectors.joining(","));

            if (isExcluded(key, columnNames)) {
                log.infof("Skip creating index on table %s with columns %s on a ForeignKey %s as it was excluded in configuration",
                        key.getTable().getName(), columnNameList, key.getName());
                continue;
            }

            addIndex(getIndex(key, columnNameList), key.getTable());
        }
    }

    private Index getIndex(final ForeignKey key, final String columnNameList) {
        final Index index = new Index();
        index.setName("I" + key.getName());
        index.addColumns(key.getColumnIterator());
        index.setTable(key.getTable());
        log.infof("Added index on table %s with columns %s to enhance ForeignKey %s referencing %s " ,
                key.getTable().getName(), columnNameList, key.getName(),
                key.getReferencedTable() != null ? key.getReferencedTable().getName() : "unknown"
        );
        return index;
    }

    private boolean isExcluded(ForeignKey key, ArrayList<String> columnNames) {
        final java.util.List<String> propertyNames = columnNames.stream().map(c -> key.getTable().getName() + "." + c).collect(Collectors.toList());
        for (final String propertyName : propertyNames) {
            if (configuration.isForeignKeyColumnExcludedFromIndexes(propertyName)) {
                return true;
            }
        }
        return false;
    }

}
