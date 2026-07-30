package com.daniilkhanukov.spring.notificationservicespringkafka.consumer;

import com.daniilkhanukov.spring.notificationservicespringkafka.dto.VerificationCodeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Поднимаю embedded Kafka,
 * публикую сообщение через KafkaTemplate, проверяю,
 * что @KafkaListener в этом сервисе его получил и передал в NotificationPrinter
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"verification-codes-test"})
@ActiveProfiles("test")
class VerificationCodeConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private NotificationPrinter notificationPrinter;

    @Value("${app.kafka.topic.verification-codes}")
    private String topic;

    @Test
    void shouldConsumeMessagePublishedToKafkaTopic() {
        VerificationCodeMessage message = new VerificationCodeMessage(
                "kafka-integration-test@example.com",
                "654321",
                Instant.now(),
                Instant.now().plusSeconds(300)
        );

        kafkaTemplate.send(topic, message.email(), message);

        verify(notificationPrinter, timeout(Duration.ofSeconds(10).toMillis()))
                .print(eq(message));
    }

    @Test
    void shouldConsumeMultipleMessagesInOrder() {
        String email = "order-test@example.com";

        VerificationCodeMessage first = new VerificationCodeMessage(
                email, "111111", Instant.now(), Instant.now().plusSeconds(300));
        VerificationCodeMessage second = new VerificationCodeMessage(
                email, "222222", Instant.now(), Instant.now().plusSeconds(300));

        kafkaTemplate.send(topic, email, first);
        kafkaTemplate.send(topic, email, second);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> verify(notificationPrinter).print(eq(second)));

        verify(notificationPrinter).print(eq(first));
    }
}
