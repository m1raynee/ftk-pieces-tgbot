package com.m1raynee.db;

import com.m1raynee.db.entity.Student;

public final class Database {
    private Database() {
    }

    public static Student createStudent(String name) {
        Student student = new Student(name);
        HibernateConfiguration.getSessionFactory().inTransaction(session -> session.persist(student));
        return student;
    }
}
