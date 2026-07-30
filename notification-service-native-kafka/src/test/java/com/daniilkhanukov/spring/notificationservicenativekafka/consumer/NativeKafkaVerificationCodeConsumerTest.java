package com.daniilkhanukov.spring.notificationservicenativekafka.consumer;

import com.daniilkhanukov.spring.notificationservicenativekafka.dto.VerificationCodeMessage;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class NativeKafkaVerificationCodeConsumerTest {

    private static final String TOPIC = "verification-codes";
    private static final TopicPartition PARTITION = new TopicPartition(TOPIC, 0);

    private MockConsumer<String, String> mockConsumer;

    @BeforeEach
    void setUp() {
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        mockConsumer.assign(java.util.List.of(PARTITION));

        Map<TopicPartition, Long> beginningOffsets = new HashMap<>();
        beginningOffsets.put(PARTITION, 0L);
        mockConsumer.updateBeginningOffsets(beginningOffsets);
    }

    @org.junit.jupiter.api.Nested
    @ExtendWith(MockitoExtension.class)
    class ProcessRecordsTests {

        @Mock
        private NotificationPrinter notificationPrinter;

        @Test
        void processRecords_shouldDeserializeAndPrintValidMessage() {
            MessageDeserializer realDeserializer = new MessageDeserializer();
            NativeKafkaVerificationCodeConsumer consumer = new NativeKafkaVerificationCodeConsumer(
                    mockConsumer, TOPIC, Duration.ofMillis(100), realDeserializer, notificationPrinter);

            String json = toJson("user@example.com", "123456");
            mockConsumer.addRecord(new org.apache.kafka.clients.consumer.ConsumerRecord<>(
                    TOPIC, 0, 0L, System.currentTimeMillis(), TimestampType.CREATE_TIME,
                    0, json.length(), "user@example.com", json,
                    new org.apache.kafka.common.header.internals.RecordHeaders(), java.util.Optional.empty()));

            ConsumerRecords<String, String> records = mockConsumer.poll(Duration.ofMillis(100));
            consumer.processRecords(records);

            verify(notificationPrinter, times(1)).print(any(VerificationCodeMessage.class));
        }

        @Test
        void processRecords_shouldSkipMalformedMessage_withoutThrowing() {
            MessageDeserializer realDeserializer = new MessageDeserializer();
            NativeKafkaVerificationCodeConsumer consumer = new NativeKafkaVerificationCodeConsumer(
                    mockConsumer, TOPIC, Duration.ofMillis(100), realDeserializer, notificationPrinter);

            String malformedJson = "{ broken json";
            mockConsumer.addRecord(new org.apache.kafka.clients.consumer.ConsumerRecord<>(
                    TOPIC, 0, 0L, System.currentTimeMillis(), TimestampType.CREATE_TIME,
                    0, malformedJson.length(), "key", malformedJson,
                    new org.apache.kafka.common.header.internals.RecordHeaders(), java.util.Optional.empty()));

            ConsumerRecords<String, String> records = mockConsumer.poll(Duration.ofMillis(100));

            consumer.processRecords(records);

            verify(notificationPrinter, times(0)).print(any());
        }

        @Test
        void processRecords_shouldProcessMultipleMessagesInBatch() {
            MessageDeserializer realDeserializer = new MessageDeserializer();
            NativeKafkaVerificationCodeConsumer consumer = new NativeKafkaVerificationCodeConsumer(
                    mockConsumer, TOPIC, Duration.ofMillis(100), realDeserializer, notificationPrinter);

            for (int i = 0; i < 3; i++) {
                String json = toJson("user" + i + "@example.com", "00000" + i);
                mockConsumer.addRecord(new org.apache.kafka.clients.consumer.ConsumerRecord<>(
                        TOPIC, 0, (long) i, System.currentTimeMillis(), TimestampType.CREATE_TIME,
                        0, json.length(), "key" + i, json,
                        new org.apache.kafka.common.header.internals.RecordHeaders(), java.util.Optional.empty()));
            }

            ConsumerRecords<String, String> records = mockConsumer.poll(Duration.ofMillis(100));
            consumer.processRecords(records);

            verify(notificationPrinter, times(3)).print(any(VerificationCodeMessage.class));
        }
    }

    @Test
    void shutdown_shouldCauseRunToExitCleanly() throws InterruptedException {
        MessageDeserializer realDeserializer = new MessageDeserializer();
        NotificationPrinter noopPrinter = message -> { };

        NativeKafkaVerificationCodeConsumer consumer = new NativeKafkaVerificationCodeConsumer(
                mockConsumer, TOPIC, Duration.ofMillis(50), realDeserializer, noopPrinter);

        Thread runnerThread = new Thread(consumer::run);
        runnerThread.start();

        Thread.sleep(200);
        consumer.shutdown();
        runnerThread.join(Duration.ofSeconds(5).toMillis());

        org.assertj.core.api.Assertions.assertThat(runnerThread.isAlive()).isFalse();
    }

    private static String toJson(String email, String code) {
        Instant now = Instant.parse("2026-07-15T10:00:00Z");
        return """
                {"email":"%s","code":"%s","createdAt":"%s","expiresAt":"%s"}
                """.formatted(email, code, now, now.plusSeconds(300));
    }
}
