package com.daniilkhanukov.spring.authservice.kafka;

import com.daniilkhanukov.spring.authservice.dto.VerificationCodeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCodeProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.verification-codes}")
    private String topic;

    public void sendVerificationCode(VerificationCodeMessage message) {
        kafkaTemplate.send(topic, message.email(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Не удалось отправить код подтверждения для {} в Kafka: {}",
                                message.email(), ex.getMessage(), ex);
                    } else {
                        log.info("Код подтверждения для {} отправлен в топик {} (partition={}, offset={})",
                                message.email(),
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
