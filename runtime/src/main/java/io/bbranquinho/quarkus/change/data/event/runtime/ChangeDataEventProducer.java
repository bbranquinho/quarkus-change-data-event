package io.bbranquinho.quarkus.change.data.event.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bbranquinho.change.data.event.ChangeDataEventConfig;
import io.bbranquinho.change.data.event.ChangeDataEventManager;
import io.bbranquinho.change.data.event.idempotency.IdempotencyStrategy;
import io.bbranquinho.change.data.event.idempotency.NoIdempotencyStrategy;
import io.bbranquinho.change.data.event.repository.EventStoreDynamoRepository;
import io.bbranquinho.change.data.event.repository.EventStoreRepository;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@Dependent
public class ChangeDataEventProducer {

    @Produces
    @Singleton
    @DefaultBean
    IdempotencyStrategy defaultIdempotencyStrategy() {
        return new NoIdempotencyStrategy();
    }

    @Produces
    @Singleton
    @DefaultBean
    ChangeDataEventManager changeDataEventManager(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        return new ChangeDataEventManager(dynamoDbEnhancedClient);
    }

    @Produces
    @ApplicationScoped
    @DefaultBean
    EventStoreRepository eventStoreRepository(ObjectMapper objectMapper,
                                              DynamoDbEnhancedClient dynamoDbEnhancedClient,
                                              ChangeDataEventConfig changeDataEventConfig,
                                              IdempotencyStrategy idempotencyStrategy) {
        return new EventStoreDynamoRepository(objectMapper, dynamoDbEnhancedClient, changeDataEventConfig, idempotencyStrategy);
    }

}
