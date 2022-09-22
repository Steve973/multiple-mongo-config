package com.example.multiplemongoconnections.config.properties;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
public class KnowledgeBaseProperties {

    private String id;

    private String name;

    private List<InstanceProperties> instances;

    private Map<String, Object> metadata;
}