package com.shopmanager.core;

import com.shopmanager.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.Properties;

public class HibernateUtil {
    private HibernateUtil() {}

    private static SessionFactory buildSessionFactory() {
        Properties settings = new Properties();
        String dbType = AppConfig.get("db.type", "mysql");
        String url = "mysql".equalsIgnoreCase(dbType)
                ? AppConfig.get("db.mysql.url", "jdbc:mysql://localhost:3306/shop_manager")
                : AppConfig.get("db.postgresql.url", "jdbc:postgresql://localhost:5432/shop_manager");
        String dialect = "mysql".equalsIgnoreCase(dbType)
                ? AppConfig.get("hibernate.dialect.mysql", "org.hibernate.dialect.MySQL8Dialect")
                : AppConfig.get("hibernate.dialect.postgresql", "org.hibernate.dialect.PostgreSQLDialect");

        settings.put("hibernate.connection.driver_class", "mysql".equalsIgnoreCase(dbType) ? "com.mysql.cj.jdbc.Driver" : "org.postgresql.Driver");
        settings.put("hibernate.connection.url", url);
        settings.put("hibernate.connection.username", AppConfig.get("db.username", "root"));
        settings.put("hibernate.connection.password", AppConfig.get("db.password", ""));
        settings.put("hibernate.dialect", dialect);
        settings.put("hibernate.hbm2ddl.auto", AppConfig.get("hibernate.hbm2ddl.auto", "update"));
        settings.put("hibernate.show_sql", AppConfig.get("hibernate.show_sql", "false"));
        settings.put("hibernate.format_sql", AppConfig.get("hibernate.format_sql", "true"));

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();
        MetadataSources sources = new MetadataSources(registry)
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Role.class)
                .addAnnotatedClass(Category.class)
                .addAnnotatedClass(Product.class)
                .addAnnotatedClass(Customer.class)
                .addAnnotatedClass(Sale.class)
                .addAnnotatedClass(SaleItem.class);
        return sources.buildMetadata().buildSessionFactory();
    }

    // Initialization-on-demand holder idiom
    private static class Holder {
        static final SessionFactory INSTANCE = buildSessionFactory();
    }

    public static SessionFactory getSessionFactory() {
        return Holder.INSTANCE;
    }
}
