package org.hibernate.enhanced;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.enhanced.test_domain.Domain;
import org.hibernate.enhanced.test_domain.Role;
import org.hibernate.enhanced.test_domain.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

}