package com.daniilkhanukov.spring.notificationservicenativekafka.consumer;

import com.daniilkhanukov.spring.notificationservicenativekafka.dto.VerificationCodeMessage;

public interface NotificationPrinter {
    void print(VerificationCodeMessage message);
}
