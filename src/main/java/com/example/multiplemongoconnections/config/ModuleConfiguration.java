package com.example.multiplemongoconnections.config;

import com.example.multiplemongoconnections.model.Item;
import com.example.multiplemongoconnections.repository.ItemRepository;
import com.example.multiplemongoconnections.repository.ItemRepositoryCustomImpl;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.repository.support.MappingMongoEntityInformation;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationPropertiesScan("com.example.multiplemongoconnections")
@PropertySource(value = "classpath:datasources.yaml", factory = YamlPropertySourceFactory.class)
@PropertySource(value = "classpath:application.yaml", factory = YamlPropertySourceFactory.class)
public class ModuleConfiguration {

    @Bean
    public List<ItemRepository> repositories(DataSourcesProperties dataSourcesProperties) {
        List<ItemRepository> repositories = new ArrayList<>();
        for (DataSourceEntry dataSourceEntry : dataSourcesProperties.getEntries()) {
            MongoClient mongoClient = MongoClients.create(dataSourceEntry.getUri());
            String[] parts = dataSourceEntry.getFqCollection().split("\\.");
            String dbName = parts[0];
            String collectionName = parts[1];
            MongoDatabaseFactory mongoDatabaseFactory = new SimpleMongoClientDatabaseFactory(mongoClient, dbName);
            MongoTemplate mongoTemplate = new MongoTemplate(mongoDatabaseFactory);
            MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext();
            MongoPersistentEntity<Item> persistentEntity = (MongoPersistentEntity<Item>) mappingContext.getRequiredPersistentEntity(Item.class);
            MappingMongoEntityInformation<Item, String> mongoEntityInformation = new MappingMongoEntityInformation<>(persistentEntity, collectionName);
            ItemRepositoryCustomImpl customImpl = new ItemRepositoryCustomImpl(mongoEntityInformation, mongoTemplate);
            ItemRepository repository = new MongoRepositoryFactory(mongoTemplate).getRepository(ItemRepository.class, customImpl);
            repositories.add(repository);
        }
        return repositories;
    }
}