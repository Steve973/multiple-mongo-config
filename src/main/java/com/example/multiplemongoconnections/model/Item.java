package com.example.multiplemongoconnections.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @MongoId
    private ObjectId id;

    private String name;
}
