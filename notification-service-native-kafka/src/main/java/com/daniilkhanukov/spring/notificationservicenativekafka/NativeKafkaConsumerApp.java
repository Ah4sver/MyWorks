package com.daniilkhanukov.spring.notificationservicenativekafka;

import com.daniilkhanukov.spring.notificationservicenativekafka.config.AppConfig;
import com.daniilkhanukov.spring.notificationservicenativekafka.consumer.ConsoleNotificationPrinter;
import com.daniilkhanukov.spring.notificationservicenativekafka.consumer.MessageDeserializer;
import com.daniilkhanukov.spring.notificationservicenativekafka.consumer.NativeKafkaVerificationCodeConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

/**
 * Приложение подписывается на топик verification-codes и печатает в консоль
 * каждый полученный код подтверждения
 */
public class NativeKafkaConsumerApp {

    private static final Logger log = LoggerFactory.getLogger(NativeKafkaConsumerApp.class);

    public static void main(String[] args) {
        AppConfig config = new AppConfig();

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(buildConsumerProps(config));

        NativeKafkaVerificationCodeConsumer consumer = new NativeKafkaVerificationCodeConsumer(
                kafkaConsumer,
                config.getTopic(),
                Duration.ofMillis(config.getPollTimeoutMs()),
                new MessageDeserializer(),
                new ConsoleNotificationPrinter()
        );

        // Graceful shutdown
        Thread shutdownHookThread = new Thread(() -> {
            log.info("Получен сигнал остановки, завершаем consumer...");
            consumer.shutdown();
        }, "shutdown-hook");
        Runtime.getRuntime().addShutdownHook(shutdownHookThread);

        log.info("Запуск notification-service-native-kafka. Bootstrap servers: {}, topic: {}",
                config.getBootstrapServers(), config.getTopic());

        consumer.run();
    }

    private static Properties buildConsumerProps(AppConfig config) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getAutoOffsetReset());

        return props;
    }
}
