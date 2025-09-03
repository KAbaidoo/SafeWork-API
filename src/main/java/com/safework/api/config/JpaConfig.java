package com.safework.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA Configuration to ensure all entities are properly scanned.
 */
@Configuration
@EntityScan(basePackages = "com.safework.api.domain")
@EnableJpaRepositories(basePackages = "com.safework.api")
public class JpaConfig {
}