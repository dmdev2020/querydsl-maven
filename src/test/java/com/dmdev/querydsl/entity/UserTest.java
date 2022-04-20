package com.dmdev.querydsl.entity;

import com.dmdev.querydsl.util.HibernateUtil;
import com.querydsl.jpa.impl.JPAQuery;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserTest {

    private static SessionFactory sessionFactory;

    @BeforeAll
    static void init() {
        sessionFactory = HibernateUtil.buildSessionFactory();
    }

    @Test
    void checkEntityMapping() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            var company = Company.builder()
                    .name("Google")
                    .build();
            session.persist(company);
            assertNotNull(company.getId());

            var user = User.builder()
                    .email("ivan@gmail.com")
                    .password("123")
                    .birthday(LocalDate.now())
                    .role(Role.ADMIN)
                    .company(company)
                    .build();
            session.persist(user);
            assertNotNull(user.getId());

            session.getTransaction().commit();
        }
    }

    @Test
    void checkQuerydsl() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();

            session.persist(Company.builder()
                    .name("Google")
                    .build());
            session.persist(Company.builder()
                    .name("Meta")
                    .build());
            session.persist(Company.builder()
                    .name("Amazon")
                    .build());

            List<Company> companies = new JPAQuery<Company>(session)
                    .select(QCompany.company)
                    .from(QCompany.company)
                    .where(QCompany.company.name.like("%a%"))
                    .fetch();

            assertThat(companies).hasSize(2);
            assertThat(companies.stream().map(Company::getName)).contains("Meta", "Amazon");

            session.getTransaction().commit();
        }
    }

    @AfterAll
    static void clean() {
        sessionFactory.close();
    }
}