package com.example.multiplemongoconnections.config.properties;

import lombok.Data;

import java.util.List;

@Data
public class InstanceProperties {

    private List<String> servers;

    private String provider;

    private List<DatabaseProperties> databases;
}
