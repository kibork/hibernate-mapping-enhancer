package org.hibernate.enhanced;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.enhanced.test_domain.Domain;
import org.hibernate.enhanced.test_domain.Role;
import org.hibernate.enhanced.test_domain.User;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * Created by kibork on 8/25/16.
 */
public class EnhancedMappingServiceTest {

    private SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        try {
            final MetadataSources sources = new MetadataSources(registry);
            sources.addAnnotatedClass(Domain.class);
            sources.addAnnotatedClass(Role.class);
            sources.addAnnotatedClass(User.class);
            sessionFactory = sources.buildMetadata().buildSessionFactory();
        }
        catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            StandardServiceRegistryBuilder.destroy( registry );
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() throws Exception {
        sessionFactory.close();
    }

    @Test
    public void testMapping() throws Exception {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save( new Domain().setName("Domain1") );
        session.save( new Domain().setName("Domain2") );
        session.getTransaction().commit();
        session.close();
    }

    private <T> long getItemCount(Iterator<T> iterator) {
        long count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            ++count;
        }
        return count;
    }


    @Test
    public void testAddSameNameIndex() {
        final Table table = new Table();

        final Index index1 = new Index();
        index1.setName("index1");
        index1.addColumn(new Column("column1"));

        table.addIndex(index1);

        assertEquals(1, getItemCount(table.getIndexIterator()));

        final Index testIndex = new Index();
        testIndex.setName("index1");
        testIndex.addColumn(new Column("column2"));

        EnhancedMappingService.addIndex(testIndex, table);

        assertEquals(1, getItemCount(table.getIndexIterator()));
        assertEquals("column1", table.getIndex("index1").getColumnIterator().next().getName());
    }


    @Test
    public void testAddSameIndexcColumns() {
        final Table table = new Table();

        final Index index1 = new Index();
        index1.setName("index1");
        index1.addColumn(new Column("column1"));
        table.addIndex(index1);

        final Index index2 = new Index();
        index2.setName("index2");
        index2.addColumn(new Column("column1"));
        index2.addColumn(new Column("column2"));
        table.addIndex(index2);

        assertEquals(2, getItemCount(table.getIndexIterator()));

        final Index testIndex = new Index();
        testIndex.setName("index_1");
        testIndex.addColumn(new Column("column1"));
        testIndex.addColumn(new Column("column2"));

        EnhancedMappingService.addIndex(testIndex, table);

        assertEquals(2, getItemCount(table.getIndexIterator()));

        testIndex.addColumn(new Column("column3"));
        EnhancedMappingService.addIndex(testIndex, table);
        assertEquals(3, getItemCount(table.getIndexIterator()));

    }

}