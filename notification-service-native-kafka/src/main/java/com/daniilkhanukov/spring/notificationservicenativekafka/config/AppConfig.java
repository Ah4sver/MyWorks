package com.daniilkhanukov.spring.notificationservicenativekafka.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
@Getter
public class AppConfig {

    private final String bootstrapServers;
    private final String topic;
    private final String groupId;
    private final String autoOffsetReset;
    private final long pollTimeoutMs;

    public AppConfig() {
        Properties props = new Properties();
        try (InputStream is = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new IllegalStateException("application.properties не найден в classpath");
            }
            props.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить application.properties", e);
        }

        this.bootstrapServers = resolve("KAFKA_BOOTSTRAP_SERVERS", props, "kafka.bootstrap-servers");
        this.topic = resolve("KAFKA_TOPIC_VERIFICATION_CODES", props, "kafka.topic.verification-codes");
        this.groupId = resolve("KAFKA_CONSUMER_GROUP_ID", props, "kafka.consumer.group-id");
        this.autoOffsetReset = resolve("KAFKA_CONSUMER_AUTO_OFFSET_RESET", props, "kafka.consumer.auto-offset-reset");
        this.pollTimeoutMs = Long.parseLong(
                resolve("KAFKA_CONSUMER_POLL_TIMEOUT_MS", props, "kafka.consumer.poll-timeout-ms"));

        log.info("Конфигурация загружена: bootstrapServers={}, topic={}, groupId={}",
                bootstrapServers, topic, groupId);
    }

    private static String resolve(String envVarName, Properties props, String propertyKey) {
        String envValue = System.getenv(envVarName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        String propValue = props.getProperty(propertyKey);
        if (propValue == null) {
            throw new IllegalStateException("Отсутствует обязательное свойство: " + propertyKey);
        }
        return propValue;
    }
}
