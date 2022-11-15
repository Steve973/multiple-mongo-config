package com.example.multiplemongoconnections;

import com.example.multiplemongoconnections.config.YamlPropertySourceFactory;
import com.example.multiplemongoconnections.config.properties.DataSourceEntry;
import com.example.multiplemongoconnections.config.properties.DataSourcesProperties;
import com.example.multiplemongoconnections.model.Item;
import com.example.multiplemongoconnections.repository.ItemRepository;
import com.example.multiplemongoconnections.repository.ItemRepositoryCustomImpl;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.ImmutableMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.repository.support.MappingMongoEntityInformation;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "spring.mongodb.embedded.version=4.0.21"
})
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class
})
@ActiveProfiles("test")
public class MultipleMongoDatabaseTest {

    MongodExecutable mongodExecutable;

    @BeforeEach
    public void setup() throws IOException {
        MongodConfig mongodConfig = ImmutableMongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net("localhost", 27027, Network.localhostIsIPv6()))
                .build();
        MongodStarter starter = MongodStarter.getDefaultInstance();
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
    }

    @Test
    public void test(@Autowired List<ItemRepository> repositories) {
        // given
        Item item = Item.builder()
                .name("test item")
                .build();

        // when
        ItemRepository repo = repositories.get(0);
        repo.save(item);

        // then
        List<Item> items = repo.findAll();
        Item foundItem = items.get(0);
        assertNotNull(foundItem);
    }

    @TestConfiguration
    @EnableConfigurationProperties(DataSourcesProperties.class)
    @PropertySource(value = "classpath:datasources-test.yaml", factory = YamlPropertySourceFactory.class)
    public static class TestDataSourceConfiguration {

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
}
