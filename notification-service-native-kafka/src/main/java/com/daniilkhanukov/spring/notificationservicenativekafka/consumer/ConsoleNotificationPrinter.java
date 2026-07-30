package com.daniilkhanukov.spring.notificationservicenativekafka.consumer;

import com.daniilkhanukov.spring.notificationservicenativekafka.dto.VerificationCodeMessage;

public class ConsoleNotificationPrinter implements NotificationPrinter {

    @Override
    public void print(VerificationCodeMessage message) {
        System.out.println("========================================");
        System.out.println("||NATIVE KAFKA|| Новое письмо с кодом подтверждения");
        System.out.println("Кому:  " + message.email());
        System.out.println("Код:   " + message.code());
        System.out.println("Дата:  " + message.createdAt());
        System.out.println("Годен до: " + message.expiresAt());
        System.out.println("========================================");
    }
}