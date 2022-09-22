package com.example.multiplemongoconnections;

import com.example.multiplemongoconnections.config.properties.DataSourcesProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableConfigurationProperties(DataSourcesProperties.class)
class MultipleMongoConnectionsApplicationTests {

    @Autowired
    DataSourcesProperties dataSourcesProperties;

    @Test
    void contextLoads() {
    }

    @Test
    void configurationPropertiesLoad() {
        Assertions.assertEquals("db1", dataSourcesProperties.getSystem().getDatabase());
    }
}
