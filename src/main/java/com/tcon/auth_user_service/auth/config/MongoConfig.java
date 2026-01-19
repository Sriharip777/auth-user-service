package com.tcon.auth_user_service.auth.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.tcon.auth_user_service.repository")
@EnableMongoAuditing
public class MongoConfig {
}
