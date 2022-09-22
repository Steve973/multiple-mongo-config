package com.example.multiplemongoconnections.model;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "items")
public class Item {

    @MongoId
    private ObjectId id;

    private String name;
}
