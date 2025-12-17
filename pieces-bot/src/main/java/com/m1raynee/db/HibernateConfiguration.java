package com.m1raynee.db;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.m1raynee.db.entity.*;

public final class HibernateConfiguration {
    private static final SessionFactory sessionFactory = new MetadataSources(
            new StandardServiceRegistryBuilder().build())
            .addAnnotatedClass(Student.class)
            .addAnnotatedClass(Box.class)
            .addAnnotatedClass(Piece.class)
            .addAnnotatedClass(PieceAction.class)
            .buildMetadata()
            .buildSessionFactory();

    private HibernateConfiguration() {
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
