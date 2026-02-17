package com.epam.infrastructure.config;

import com.epam.application.exceptions.InvalidAuthException;
import jakarta.validation.ConstraintViolationException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerErrorConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> template,
            @Value("${app.kafka.topics.workload-events-dlq}") String dlqTopic
    ) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(template, (record, ex) ->
                        new TopicPartition(dlqTopic, record.partition())
                );

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(5000L, 3L));

        handler.addNotRetryableExceptions(
                InvalidAuthException.class,
                ConstraintViolationException.class,
                MethodArgumentNotValidException.class,
                IllegalArgumentException.class
        );

        return handler;
    }
}
