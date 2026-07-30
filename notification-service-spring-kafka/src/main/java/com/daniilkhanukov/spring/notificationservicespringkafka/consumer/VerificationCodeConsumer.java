package com.daniilkhanukov.spring.notificationservicespringkafka.consumer;

import com.daniilkhanukov.spring.notificationservicespringkafka.dto.VerificationCodeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCodeConsumer {

    private final NotificationPrinter notificationPrinter;

    @KafkaListener(
            topics = "${app.kafka.topic.verification-codes}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(VerificationCodeMessage message) {
        notificationPrinter.print(message);
        log.info("Обработано сообщение с кодом подтверждения для {}", message.email());
    }
}