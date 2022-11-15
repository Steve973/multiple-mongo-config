package com.example.multiplemongoconnections;

import com.example.multiplemongoconnections.config.DataSourceEntry;
import com.example.multiplemongoconnections.config.DataSourcesProperties;
import com.example.multiplemongoconnections.config.YamlPropertySourceFactory;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Example;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.repository.support.MappingMongoEntityInformation;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration(exclude = {
        EmbeddedMongoAutoConfiguration.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class
})
public class MultipleMongoDatabaseTest {

    MongodExecutable mongodExecutable;

    @Autowired
    MongodConfig mongodConfig;

    @BeforeEach
    public void setup() throws IOException {
        MongodStarter starter = MongodStarter.getDefaultInstance();
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
    }

    @AfterEach
    public void afterEach() {
        mongodExecutable.stop();
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

        // then find the item that was persisted
        Collection<Item> foundByCustomImplMethod = repo.findAll();
        assertEquals(1, foundByCustomImplMethod.size());

        // and using the interface searches in a collection named after the model class
        // which is wrong, so none will be found
        Collection<Item> foundByInterfaceMethod = repo.findByName("test item");
        assertEquals(0, foundByInterfaceMethod.size());

        // and this verifies that the item can be found by name when a method from the
        // custom implementation is used
        Item foundOne = repo.findOne(
                Example.of(
                        Item.builder()
                                .name("test item")
                                .build()))
                                .orElse(null);
        assertNotNull(foundOne);
    }

    @TestConfiguration
    @EnableConfigurationProperties(DataSourcesProperties.class)
    @PropertySource(value = "classpath:datasources.yaml", factory = YamlPropertySourceFactory.class)
    public static class TestDataSourceConfiguration {

        @Bean
        public MongodConfig mongodConfig() throws UnknownHostException {
            return ImmutableMongodConfig.builder()
                    .version(Version.Main.V4_4)
                    .net(new Net("localhost", 27027, Network.localhostIsIPv6()))
                    .build();
        }

        @Bean
        @Lazy
        @DependsOn("mongodConfig")
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
