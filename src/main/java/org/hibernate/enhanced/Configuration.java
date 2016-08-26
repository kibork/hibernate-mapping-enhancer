package org.hibernate.enhanced;

import org.hibernate.boot.spi.ClassLoaderAccess;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by kibork on 8/25/16.
 */
public class Configuration {

    // ------------------ Constants  --------------------

    private final String propFileName = "HibernateEnhancedMappingService.properties";

    private static final String PROPERTY_PREFIX = "org.hibernate.enhanced.EnhancedMappingService.";

    // ------------------ Fields     --------------------

    // ------------------ Properties --------------------

    private Properties loadedProperties = new Properties(System.getProperties());

    private Boolean createIndexOnForeignKeys;

    private Boolean createIndexOnEnums;

    private Pattern[] excludeKeyColumnPatterns = null;

    private Pattern[] excludeEnumColumnPatterns = null;

    // ------------------ Logic      --------------------


    public Configuration(ClassLoaderAccess classLoaderAccess) {
        final URL configResourceURL = classLoaderAccess.locateResource(propFileName);
        if (configResourceURL != null) {
            loadConfiguration(configResourceURL);
        }
    }

    private void loadConfiguration(URL configResourceURL) {
        InputStream configResource = null;
        try {
            configResource = configResourceURL.openStream();
            loadedProperties.load(configResource);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (configResource != null) {
                try {
                    configResource.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public boolean shouldCreateIndexOnForeignKeys() {
        if (createIndexOnForeignKeys == null) {
            createIndexOnForeignKeys = Boolean.valueOf(loadedProperties.getProperty(PROPERTY_PREFIX + "createIndexOnForeignKeys", "true"));
        }
        return createIndexOnForeignKeys;
    }

    public boolean isForeignKeyColumnExcludedFromIndexes(final String columnName) {
        if (!shouldCreateIndexOnForeignKeys()) {
            return true;
        }
        if (excludeKeyColumnPatterns == null) {
            excludeKeyColumnPatterns = getPatterns("createIndexOnForeignKeys.excludeColumns");
        }
        return patternsMatch(columnName, excludeKeyColumnPatterns);
    }

    public boolean shouldCreateIndexOnEnums() {
        if (createIndexOnEnums == null) {
            createIndexOnEnums = Boolean.valueOf(loadedProperties.getProperty(PROPERTY_PREFIX + "createIndexOnEnums", "true"));
        }
        return createIndexOnEnums;
    }

    public boolean isEnumColumnExcludedFromIndexes(final String columnName) {
        if (!shouldCreateIndexOnEnums()) {
            return true;
        }
        if (excludeEnumColumnPatterns == null) {
            excludeEnumColumnPatterns = getPatterns("createIndexOnEnums.excludeColumns");
        }
        return patternsMatch(columnName, excludeEnumColumnPatterns);
    }

    private Pattern[] getPatterns(String configurationName) {
        final String excludeList = loadedProperties.getProperty(PROPERTY_PREFIX + configurationName, "");
        if ((excludeList == null) || (excludeList.isEmpty())) {
            return new Pattern[0];
        } else {
            return Arrays.stream(excludeList.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).map(Pattern::compile)
                    .toArray(Pattern[]::new);
        }
    }

    private static boolean patternsMatch(String columnName, Pattern[] columnPatterns) {
        for (final Pattern excludedPattern : columnPatterns) {
            if (excludedPattern.matcher(columnName).matches()) {
                return true;
            }
        }
        return false;
    }
}
