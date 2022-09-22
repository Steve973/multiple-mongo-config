package com.example.multiplemongoconnections.config.properties;

import lombok.Data;

@Data
public class DatabaseProperties {

    private String name;

    private String description;

    private String username;

    private String password;

    private String authDatabase = "admin";
}
