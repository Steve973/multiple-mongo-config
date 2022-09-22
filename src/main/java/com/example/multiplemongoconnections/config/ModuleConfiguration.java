package com.example.multiplemongoconnections.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationPropertiesScan("com.example.multiplemongoconnections")
@PropertySource(value = "classpath:datasources.yaml", factory = YamlPropertySourceFactory.class)
@PropertySource("classpath:application.yaml")
public class ModuleConfiguration {
}