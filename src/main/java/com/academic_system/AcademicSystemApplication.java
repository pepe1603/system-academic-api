package com.academic_system;

import com.academic_system.service.ProfileMigrationService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AcademicSystemApplication {

    public static void main(String[] args) {

        // En Docker no hay .env — ignoreIfMissing() permite usar env vars del contenedor
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Helper: primero dotenv, luego variable de entorno del sistema
        java.util.function.BiFunction<String, String, String> get = (key, def) -> {
            String val = dotenv.get(key, null);
            if (val == null) val = System.getenv(key);
            if (val == null) val = def;
            return val;
        };

        String dbUrl = get.apply("URL_DATABASE_POSTGRESQL", "");
        System.out.println("=== DATABASE URL: " + dbUrl + " ===");

        System.setProperty("URL_DATABASE_POSTGRESQL",         get.apply("URL_DATABASE_POSTGRESQL", ""));
        System.setProperty("USERNAME_POSTGRESQL",             get.apply("USERNAME_POSTGRESQL", ""));
        System.setProperty("PASSWORD_POSTGRESQL",             get.apply("PASSWORD_POSTGRESQL", ""));
        System.setProperty("MYSQL_HOST",                      get.apply("MYSQL_HOST", ""));
        System.setProperty("MYSQL_PORT",                      get.apply("MYSQL_PORT", ""));
        System.setProperty("MYSQL_DATABASE",                  get.apply("MYSQL_DATABASE", ""));
        System.setProperty("USERNAME_MYSQL",                  get.apply("USERNAME_MYSQL", ""));
        System.setProperty("PASSWORD_MYSQL",                  get.apply("PASSWORD_MYSQL", ""));
        System.setProperty("REDIS_HOST",                      get.apply("REDIS_HOST", ""));
        System.setProperty("REDIS_PORT",                      get.apply("REDIS_PORT", "6379"));
        System.setProperty("REDIS_PASSWORD",                  get.apply("REDIS_PASSWORD", ""));
        System.setProperty("REDIS_SSL_ENABLED",               get.apply("REDIS_SSL_ENABLED", "false"));
        System.setProperty("MAIL_HOST",                       get.apply("MAIL_HOST", ""));
        System.setProperty("MAIL_PORT",                       get.apply("MAIL_PORT", "587"));
        System.setProperty("MAIL_USERNAME",                   get.apply("MAIL_USERNAME", ""));
        System.setProperty("MAIL_PASSWORD",                   get.apply("MAIL_PASSWORD", ""));
        System.setProperty("MAIL_FROM",                       get.apply("MAIL_FROM", ""));
        System.setProperty("JWT_SECRET",                      get.apply("JWT_SECRET", ""));
        System.setProperty("JWT_ACCESS_TOKEN_EXPIRATION",     get.apply("JWT_ACCESS_TOKEN_EXPIRATION", "900000"));
        System.setProperty("JWT_REFRESH_TOKEN_EXPIRATION",    get.apply("JWT_REFRESH_TOKEN_EXPIRATION", "604800000"));
        System.setProperty("SERVER_PORT",                     get.apply("SERVER_PORT", "9090"));

        ConfigurableApplicationContext context = SpringApplication.run(AcademicSystemApplication.class, args);

        try {
            ProfileMigrationService migrationService = context.getBean(ProfileMigrationService.class);
            migrationService.migrateExistingProfiles();
        } catch (Exception e) {
            System.err.println("Error during profile migration: " + e.getMessage());
        }
    }

}
