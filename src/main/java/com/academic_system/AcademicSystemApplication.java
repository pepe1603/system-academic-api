package com.academic_system;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AcademicSystemApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().load();
		
		String dbUrl = dotenv.get("URL_DATABASE_POSTGRESQL");
		System.out.println("=== DATABASE URL: " + dbUrl + " ===");
		
		System.setProperty("URL_DATABASE_POSTGRESQL", dbUrl);
		System.setProperty("USERNAME_POSTGRESQL", dotenv.get("USERNAME_POSTGRESQL"));
		System.setProperty("PASSWORD_POSTGRESQL", dotenv.get("PASSWORD_POSTGRESQL"));
		System.setProperty("REDIS_HOST", dotenv.get("REDIS_HOST"));
		System.setProperty("REDIS_PORT", dotenv.get("REDIS_PORT", "6379"));
		System.setProperty("REDIS_PASSWORD", dotenv.get("REDIS_PASSWORD", ""));
		System.setProperty("MAIL_USERNAME", dotenv.get("MAIL_USERNAME"));
		System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));
		System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
		System.setProperty("JWT_ACCESS_TOKEN_EXPIRATION", dotenv.get("JWT_ACCESS_TOKEN_EXPIRATION", "900000"));
		System.setProperty("JWT_REFRESH_TOKEN_EXPIRATION", dotenv.get("JWT_REFRESH_TOKEN_EXPIRATION", "604800000"));
		
		SpringApplication.run(AcademicSystemApplication.class, args);
	}

}
