package com.fdp.generator.config;

import com.fdp.common.dto.Transaction;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;

import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, Transaction> producerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
        // sensible defaults
        props.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // batching / compression tuned
        props.putIfAbsent(ProducerConfig.LINGER_MS_CONFIG, 20);
        props.putIfAbsent(ProducerConfig.BATCH_SIZE_CONFIG, 32 * 1024);
        props.putIfAbsent(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Transaction> kafkaTemplate(ProducerFactory<String, Transaction> pf) {
        return new KafkaTemplate<>(pf);
    }
}
