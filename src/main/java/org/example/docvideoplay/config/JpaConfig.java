package org.example.docvideoplay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {"org.example.docvideoplay.dao.jpa", "org.example.docvideoplay.repository"})
public class JpaConfig {
}