package com.daniilkhanukov.log4j2.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Plugin(name = "KafkaAppender", category = "Core", elementType = "appender", printObject = true)
public class KafkaLog4j2Appender extends AbstractAppender {

    // Флаг, чтобы не было рекурсии логов при проблемах с брокером
    private static final ThreadLocal<Boolean> IN_PROGRESS = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final String bootstrapServers;
    private final String topic;
    private final String key;
    private final boolean syncSend;
    private final long sendTimeoutMs;
    private final Properties producerProperties;

    private volatile Producer<String, String> producer;
    private final AtomicBoolean producerInitFailed = new AtomicBoolean(false);

    protected KafkaLog4j2Appender(String name,
                                  Filter filter,
                                  Layout<? extends Serializable> layout,
                                  boolean ignoreExceptions,
                                  Property[] properties,
                                  String bootstrapServers,
                                  String topic,
                                  String key,
                                  boolean syncSend,
                                  long sendTimeoutMs,
                                  Properties producerProperties) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.key = key;
        this.syncSend = syncSend;
        this.sendTimeoutMs = sendTimeoutMs;
        this.producerProperties = producerProperties;
    }

    @PluginFactory
    public static KafkaLog4j2Appender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") Filter filter,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) boolean ignoreExceptions,
            @PluginAttribute("bootstrapServers") String bootstrapServers,
            @PluginAttribute("topic") String topic,
            @PluginAttribute("key") String key,
            @PluginAttribute(value = "syncSend", defaultBoolean = false) boolean syncSend,
            @PluginAttribute(value = "sendTimeoutMs", defaultLong = 5000L) long sendTimeoutMs,
            @PluginElement("Properties") Property[] properties) {

        if (name == null) {
            LOGGER.error("KafkaAppender: обязательный атрибут 'name' не задан");
            return null;
        }
        if (bootstrapServers == null || bootstrapServers.trim().isEmpty()) {
            LOGGER.error("KafkaAppender: обязательный атрибут 'bootstrapServers' не задан");
            return null;
        }
        if (topic == null || topic.trim().isEmpty()) {
            LOGGER.error("KafkaAppender: обязательный атрибут 'topic' не задан");
            return null;
        }

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        Properties producerProperties = buildBaseProducerProperties(bootstrapServers);

        if (properties != null) {
            for (Property property : properties) {
                producerProperties.setProperty(property.getName(), property.getValue());
            }
        }

        return new KafkaLog4j2Appender(name, filter, layout, ignoreExceptions, null,
                bootstrapServers, topic, key, syncSend, sendTimeoutMs, producerProperties);
    }

    private static Properties buildBaseProducerProperties(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "5000");
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "10000");
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        return props;
    }

    /**
     * Лениво инициализирует кафка продюсер при первой записи в лог, чтобы
     * конфиг log4j2 не зависел от доступности кафки в момент запуска
     */
    private Producer<String, String> getOrCreateProducer() {
        Producer<String, String> localRef = producer;
        if (localRef != null) {
            return localRef;
        }
        synchronized (this) {
            if (producer == null) {
                if (producerInitFailed.get()) {
                    return null;
                }
                try {
                    producer = new KafkaProducer<>(producerProperties);
                } catch (Exception e) {
                    producerInitFailed.set(true);
                    LOGGER.error("KafkaAppender '{}': не удалось создать KafkaProducer: {}",
                            getName(), e.getMessage(), e);
                    if (!ignoreExceptions()) {
                        throw e;
                    }
                }
            }
            return producer;
        }
    }

    @Override
    public void append(LogEvent event) {

        if (Boolean.TRUE.equals(IN_PROGRESS.get())) {
            return;
        }

        Producer<String, String> currentProducer = getOrCreateProducer();
        if (currentProducer == null) {
            return;
        }

        try {
            IN_PROGRESS.set(Boolean.TRUE);

            String message = new String(getLayout().toByteArray(event), java.nio.charset.StandardCharsets.UTF_8);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);

            if (syncSend) {
                sendSync(currentProducer, record);
            } else {
                sendAsync(currentProducer, record);
            }
        } catch (Exception e) {
            if (!ignoreExceptions()) {
                throw new org.apache.logging.log4j.core.appender.AppenderLoggingException(
                        "Ошибка отправки лог-события в Kafka appender '" + getName() + "'", e);
            }
            LOGGER.error("KafkaAppender '{}': ошибка отправки сообщения в топик '{}': {}",
                    getName(), topic, e.getMessage(), e);
        } finally {
            IN_PROGRESS.set(Boolean.FALSE);
        }
    }

    private void sendSync(Producer<String, String> currentProducer, ProducerRecord<String, String> record)
            throws ExecutionException, InterruptedException, TimeoutException {
        Future<RecordMetadata> future = currentProducer.send(record);
        future.get(sendTimeoutMs, TimeUnit.MILLISECONDS);
    }

    private void sendAsync(Producer<String, String> currentProducer, ProducerRecord<String, String> record) {
        currentProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                LOGGER.error("KafkaAppender '{}': асинхронная ошибка отправки в топик '{}': {}",
                        getName(), topic, exception.getMessage(), exception);
            }
        });
    }

    @Override
    public boolean stop(long timeout, TimeUnit timeUnit) {
        setStopping();
        boolean stopped = super.stop(timeout, timeUnit, false);
        Producer<String, String> currentProducer = producer;
        if (currentProducer != null) {
            try {
                currentProducer.close(java.time.Duration.ofMillis(timeUnit.toMillis(timeout)));
            } catch (Exception e) {
                LOGGER.warn("KafkaAppender '{}': ошибка при закрытии KafkaProducer: {}",
                        getName(), e.getMessage(), e);
            }
        }
        setStopped();
        return stopped;
    }

    // Добавил для тестирования.
    String getBootstrapServers() {
        return bootstrapServers;
    }

    String getTopic() {
        return topic;
    }

    String getKey() {
        return key;
    }

    boolean isSyncSend() {
        return syncSend;
    }

    long getSendTimeoutMs() {
        return sendTimeoutMs;
    }
}

