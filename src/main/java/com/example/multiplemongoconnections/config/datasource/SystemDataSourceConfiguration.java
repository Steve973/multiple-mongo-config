package com.example.multiplemongoconnections.config.datasource;

import com.example.multiplemongoconnections.config.properties.DataSourcesProperties;
import com.example.multiplemongoconnections.config.properties.SystemDataProperties;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SystemDataSourceConfiguration {

    @Bean(name = "systemMongoClient")
    public MongoClient mongoClient(final DataSourcesProperties dataSourcesProperties) {
        SystemDataProperties systemDataProperties = dataSourcesProperties.getSystem();
        String username = systemDataProperties.getUsername();
        String password = systemDataProperties.getPassword();
        String authDb = systemDataProperties.getAuthDatabase();
        List<ServerAddress> serverAddresses = systemDataProperties.getServers().stream()
                .map(s -> s.split(":"))
                .map(s -> new ServerAddress(s[0], Integer.parseInt(s[1])))
                .collect(Collectors.toList());
        MongoCredential credential = MongoCredential.createCredential(username, authDb, password.toCharArray());
        return MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(serverAddresses))
                .credential(credential)
                .build());
    }

    @Bean(name = "systemMongoDbFactory")
    public MongoDatabaseFactory mongoDatabaseFactory(
            @Qualifier("systemMongoClient") MongoClient mongoClient,
            DataSourcesProperties dataSourcesProperties) {
        SystemDataProperties systemDataProperties = dataSourcesProperties.getSystem();
        String database = systemDataProperties.getDatabase();
        return new SimpleMongoClientDatabaseFactory(mongoClient, database);
    }

    @Bean(name = "systemMongoDbTemplate")
    public MongoTemplate mongoTemplate(
            @Qualifier("systemMongoDbFactory") MongoDatabaseFactory dbFactory) {
        return new MongoTemplate(dbFactory);
    }
}
