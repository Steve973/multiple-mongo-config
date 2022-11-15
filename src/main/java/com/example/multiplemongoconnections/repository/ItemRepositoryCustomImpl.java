package com.example.multiplemongoconnections.repository;

import com.example.multiplemongoconnections.model.Item;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

public class ItemRepositoryCustomImpl extends SimpleMongoRepository<Item, String> implements ItemRepositoryCustom {

    public ItemRepositoryCustomImpl(MongoEntityInformation<Item, String> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
    }
}
