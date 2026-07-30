package com.daniilkhanukov.spring.notificationservicenativekafka.consumer;

import com.daniilkhanukov.spring.notificationservicenativekafka.dto.VerificationCodeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Слушатель топика verification-codes на нативном Apache Kafka клиенте.
 * Реализует классический poll-цикл
 */
@Slf4j
public class NativeKafkaVerificationCodeConsumer {

    private final Consumer<String, String> consumer;
    private final String topic;
    private final Duration pollTimeout;
    private final MessageDeserializer messageDeserializer;
    private final NotificationPrinter notificationPrinter;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public NativeKafkaVerificationCodeConsumer(Consumer<String, String> consumer,
                                               String topic,
                                               Duration pollTimeout,
                                               MessageDeserializer messageDeserializer,
                                               NotificationPrinter notificationPrinter) {
        this.consumer = consumer;
        this.topic = topic;
        this.pollTimeout = pollTimeout;
        this.messageDeserializer = messageDeserializer;
        this.notificationPrinter = notificationPrinter;
    }

    /**
     * Запускает бесконечный poll-цикл
     */
    public void run() {
        try {
            consumer.subscribe(List.of(topic));
            log.info("Подписались на топик {}", topic);

            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
                processRecords(records);
            }
        } catch (WakeupException e) {
            if (running.get()) {
                throw e;
            }
            log.info("Consumer получил сигнал остановки, завершаем работу");
        } finally {
            consumer.close();
            log.info("Consumer закрыт");
        }
    }

    /**
     * Обрабатывает одну пачку записей, полученную из poll()
     */
    void processRecords(ConsumerRecords<String, String> records) {
        for (ConsumerRecord<String, String> record : records) {
            try {
                VerificationCodeMessage message = messageDeserializer.deserialize(record.value());
                notificationPrinter.print(message);
                log.info("Обработано сообщение для {} (partition={}, offset={})",
                        message.email(), record.partition(), record.offset());
            } catch (MessageDeserializationException e) {
                log.error("Пропускаем некорректное сообщение (partition={}, offset={}): {}",
                        record.partition(), record.offset(), e.getMessage());
            }
        }
    }

    public void shutdown() {
        running.set(false);
        consumer.wakeup();
    }
}
