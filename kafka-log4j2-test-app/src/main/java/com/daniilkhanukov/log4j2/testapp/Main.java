package com.daniilkhanukov.log4j2.testapp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Пишет логи разных уровней. Конфигурация берётся из log4j2.xml
 */
public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final Logger ORDER_SERVICE_LOGGER = LogManager.getLogger("ru.example.log4j2.testapp.OrderService");

    public static void main(String[] args) throws InterruptedException {
        LOGGER.info("Тестовое приложение запущено, начинаем писать логи в Kafka");
        LOGGER.debug("Это DEBUG-сообщение (может быть отфильтровано в зависимости от уровня Root-логгера)");
        LOGGER.info("Приложение инициализировано, конфигурация загружена");
        LOGGER.warn("Пример предупреждения: демо-очередь заказов почти заполнена");

        for (int i = 1; i <= 5; i++) {
            ORDER_SERVICE_LOGGER.info("Обработка заказа #{}: статус=CREATED", i);
            Thread.sleep(300);
            ORDER_SERVICE_LOGGER.info("Обработка заказа #{}: статус=PAID", i);
            Thread.sleep(300);
        }

        try {
            simulateFailure();
        } catch (RuntimeException e) {
            LOGGER.error("Ошибка при обработке демонстрационного заказа: {}", e.getMessage(), e);
        }

        LOGGER.info("Демонстрация завершена, логи отправлены (или поставлены в очередь на отправку) в Kafka");

        Thread.sleep(1000);
    }

    private static void simulateFailure() {
        throw new IllegalStateException("Демонстрационная ошибка: не удалось списать оплату по заказу #3");
    }
}
