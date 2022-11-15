package com.example.multiplemongoconnections.repository;

import com.example.multiplemongoconnections.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemRepository extends MongoRepository<Item, String>, ItemRepositoryCustom {
}
