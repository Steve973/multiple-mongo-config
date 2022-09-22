package com.example.multiplemongoconnections.config.datasource;

import com.example.multiplemongoconnections.config.properties.DataSourcesProperties;
import com.example.multiplemongoconnections.config.properties.DatabaseProperties;
import com.example.multiplemongoconnections.config.properties.InstanceProperties;
import com.example.multiplemongoconnections.config.properties.KnowledgeBaseProperties;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class KnowledgeBaseDataSourceConfiguration {

    @Bean("knowledgeBaseMongoTemplates")
    public List<MongoTemplate> knowledgeBaseMongoTemplates(DataSourcesProperties dataSourcesProperties) {
        List<MongoTemplate> mongoTemplates = new ArrayList<>();
        List<KnowledgeBaseProperties> knowledgeBaseProperties = dataSourcesProperties.getKnowledgeBases();
        for (KnowledgeBaseProperties kbProps : knowledgeBaseProperties) {
            List<InstanceProperties> instanceProperties = kbProps.getInstances();
            for (InstanceProperties instProps : instanceProperties) {
                List<ServerAddress> serverAddresses = serverAddresses(instProps);
                List<DatabaseProperties> databaseProperties = instProps.getDatabases();
                for (DatabaseProperties dbProps : databaseProperties) {
                    String dbName = dbProps.getName();
                    MongoClient mongoClient = mongoClient(dbProps, serverAddresses);
                    MongoDatabaseFactory dbFactory = mongoDatabaseFactory(mongoClient, dbName);
                    MongoTemplate mongoTemplate = mongoTemplate(dbFactory);
                    mongoTemplates.add(mongoTemplate);
                }
            }
        }
        return mongoTemplates;
    }

    private List<ServerAddress> serverAddresses(InstanceProperties instProps) {
        return instProps.getServers().stream()
                .map(s -> s.split(":"))
                .map(s -> new ServerAddress(s[0], Integer.parseInt(s[1])))
                .collect(Collectors.toList());
    }

    private MongoClient mongoClient(DatabaseProperties dbProps, List<ServerAddress> serverAddresses) {
        String username = dbProps.getUsername();
        String password = dbProps.getPassword();
        String authDb = dbProps.getAuthDatabase();
        MongoCredential credential = MongoCredential.createCredential(username, authDb, password.toCharArray());
        return MongoClients.create(MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(serverAddresses))
                .credential(credential)
                .build());
    }

    private MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient, String dbName) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, dbName);
    }

    private MongoTemplate mongoTemplate(MongoDatabaseFactory dbFactory) {
        return new MongoTemplate(dbFactory);
    }
}
