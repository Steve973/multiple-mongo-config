package com.example.multiplemongoconnections.config.properties;

import lombok.Data;

@Data
public class DataSourceEntry {

    private String name;

    private String uri;

    private String fqCollection;
}
