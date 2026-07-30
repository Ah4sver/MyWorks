package com.daniilkhanukov.spring.notificationservicespringkafka.consumer;

import com.daniilkhanukov.spring.notificationservicespringkafka.dto.VerificationCodeMessage;

public interface NotificationPrinter {
    void print(VerificationCodeMessage message);
}
