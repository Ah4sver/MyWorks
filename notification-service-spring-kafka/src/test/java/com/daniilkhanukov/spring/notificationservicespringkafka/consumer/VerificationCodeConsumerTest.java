package com.daniilkhanukov.spring.notificationservicespringkafka.consumer;

import com.daniilkhanukov.spring.notificationservicespringkafka.dto.VerificationCodeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VerificationCodeConsumerTest {

    @Mock
    private NotificationPrinter notificationPrinter;

    @Test
    void consume_shouldDelegateToNotificationPrinter() {
        VerificationCodeConsumer consumer = new VerificationCodeConsumer(notificationPrinter);
        VerificationCodeMessage message = new VerificationCodeMessage(
                "user@example.com", "123456", Instant.now(), Instant.now().plusSeconds(300));

        consumer.consume(message);

        verify(notificationPrinter, times(1)).print(message);
    }
}
