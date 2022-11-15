package com.example.multiplemongoconnections.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "datasources")
public class DataSourcesProperties {

    private List<DataSourceEntry> entries;
}