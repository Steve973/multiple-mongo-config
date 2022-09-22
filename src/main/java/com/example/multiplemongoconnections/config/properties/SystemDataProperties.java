package com.example.multiplemongoconnections.config.properties;

import lombok.Data;
import java.util.List;

@Data
public class SystemDataProperties {

    private List<String> servers;

    private String provider = "mongodb";

    private String database;

    private String authDatabase = "admin";

    private String username;

    private String password;
}