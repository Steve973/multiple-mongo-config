package com.example.multiplemongoconnections.repository;

import com.example.multiplemongoconnections.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ItemRepository extends MongoRepository<Item, String>, ItemRepositoryCustom {

    @Query(value = "{ name: :#{#name} }")
    Collection<Item> findByName(@Param("name") final String name);
}
