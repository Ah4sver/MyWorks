package com.daniilkhanukov.spring.authservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic.verification-codes}")
    private String verificationCodesTopic;

    @Bean
    public NewTopic verificationCodesTopic() {
        return TopicBuilder.name(verificationCodesTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
