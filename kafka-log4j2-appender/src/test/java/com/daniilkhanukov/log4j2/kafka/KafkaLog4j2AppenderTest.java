package com.daniilkhanukov.log4j2.kafka;

import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Проверяется корректность конфигурации/валидации
 */
class KafkaLog4j2AppenderTest {

    @Test
    @DisplayName("createAppender возвращает null, если не задан обязательный атрибут name")
    void createAppender_missingName_returnsNull() {
        KafkaLog4j2Appender appender = KafkaLog4j2Appender.createAppender(
                null,
                PatternLayout.createDefaultLayout(),
                null,
                true,
                "localhost:9092",
                "test-topic",
                "test-key",
                false,
                5000L,
                null);

        assertNull(appender, "Аппендер не должен создаваться без имени");
    }

    @Test
    @DisplayName("createAppender возвращает null, если не задан обязательный атрибут bootstrapServers")
    void createAppender_missingBootstrapServers_returnsNull() {
        KafkaLog4j2Appender appender = KafkaLog4j2Appender.createAppender(
                "TestAppender",
                PatternLayout.createDefaultLayout(),
                null,
                true,
                null,
                "test-topic",
                "test-key",
                false,
                5000L,
                null);

        assertNull(appender, "Аппендер не должен создаваться без bootstrapServers");
    }

    @Test
    @DisplayName("createAppender возвращает null, если не задан обязательный атрибут topic")
    void createAppender_missingTopic_returnsNull() {
        KafkaLog4j2Appender appender = KafkaLog4j2Appender.createAppender(
                "TestAppender",
                PatternLayout.createDefaultLayout(),
                null,
                true,
                "localhost:9092",
                null,
                "test-key",
                false,
                5000L,
                null);

        assertNull(appender, "Аппендер не должен создаваться без topic");
    }

    @Test
    @DisplayName("createAppender успешно создаёт аппендер с валидными параметрами")
    void createAppender_validParams_createsAppender() {
        KafkaLog4j2Appender appender = KafkaLog4j2Appender.createAppender(
                "TestAppender",
                PatternLayout.createDefaultLayout(),
                null,
                true,
                "localhost:9092",
                "test-topic",
                "test-key",
                true,
                3000L,
                null);

        assertNotNull(appender);
        assertEquals("TestAppender", appender.getName());
        assertEquals("localhost:9092", appender.getBootstrapServers());
        assertEquals("test-topic", appender.getTopic());
        assertEquals("test-key", appender.getKey());
        assertTrue(appender.isSyncSend());
        assertEquals(3000L, appender.getSendTimeoutMs());
    }

    @Test
    @DisplayName("createAppender подставляет layout по умолчанию, если он не задан")
    void createAppender_nullLayout_usesDefaultLayout() {
        KafkaLog4j2Appender appender = KafkaLog4j2Appender.createAppender(
                "TestAppender",
                null,
                null,
                true,
                "localhost:9092",
                "test-topic",
                null,
                false,
                5000L,
                null);

        assertNotNull(appender);
        assertNotNull(appender.getLayout(), "Layout должен подставляться по умолчанию");
    }

    @Test
    @DisplayName("syncSend по умолчанию равен false (асинхронная отправка)")
    void createAppender_defaultSyncSend_isFalse() {
        KafkaLog4j2Appender appender = KafkaLog4j2Appender.createAppender(
                "TestAppender",
                PatternLayout.createDefaultLayout(),
                null,
                true,
                "localhost:9092",
                "test-topic",
                null,
                false,
                5000L,
                null);

        assertNotNull(appender);
        assertFalse(appender.isSyncSend());
    }

    @Test
    @DisplayName("stop() корректно завершает работу аппендера без созданного продюсера")
    void stop_withoutProducerCreated_completesSuccessfully() {
        KafkaLog4j2Appender appender = KafkaLog4j2Appender.createAppender(
                "TestAppender",
                PatternLayout.createDefaultLayout(),
                null,
                true,
                "localhost:9092",
                "test-topic",
                null,
                false,
                5000L,
                null);

        assertNotNull(appender);
        appender.start();
        assertTrue(appender.isStarted());

        boolean stopped = appender.stop(1, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue(stopped);
        assertTrue(appender.isStopped());
    }
}
