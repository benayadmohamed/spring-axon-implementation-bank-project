package com.bank.user.core.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.extensions.mongo.DefaultMongoTemplate;
import org.axonframework.extensions.mongo.MongoTemplate;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {
    @Value("${spring.data.mongodb.host:localhost}")
    private String mongoHost;
    @Value("${spring.data.mongodb.port:27017}")
    private int mongoPort;
    @Value("${spring.data.mongodb.database:user}")
    private String mongoDatabase;

    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString =
                new ConnectionString("mongodb://" + mongoHost + ":" + mongoPort + "/" + mongoDatabase);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(mongoClientSettings);
        return mongoClient;
    }

    @Bean
    public MongoTemplate mongo() {
        return DefaultMongoTemplate
                .builder()
                .mongoDatabase(mongoClient(), mongoDatabase)
                .build();
    }

    @Bean
    public TokenStore tokenStore(Serializer serializer) {
        return MongoTokenStore
                .builder()
                .mongoTemplate(mongo())
                .serializer(serializer)
                .build();
    }

    @Bean
    public EventStorageEngine eventStorageEngine(MongoClient mongoClient) {
        DefaultMongoTemplate build = DefaultMongoTemplate
                .builder()
                .mongoDatabase(mongoClient)
                .build();
        return MongoEventStorageEngine
                .builder()
                .mongoTemplate(build)
                .build();
    }

    @Bean
    public EmbeddedEventStore embeddedEventStore(EventStorageEngine eventStorageEngine,
                                                 AxonConfiguration axonConfiguration) {
        return EmbeddedEventStore
                .builder()
                .storageEngine(eventStorageEngine)
                .messageMonitor(axonConfiguration.messageMonitor(EventStore.class, "eventStore"))
                .build();
    }
}
