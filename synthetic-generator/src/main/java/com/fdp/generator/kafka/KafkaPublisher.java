package com.fdp.generator.kafka;

import com.fdp.common.dto.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaPublisher.class);
    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private final String topic;

    public KafkaPublisher(KafkaTemplate<String, Transaction> kafkaTemplate,
                          org.springframework.core.env.Environment env) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = env.getProperty("generator.topic", "transactions.in");
    }

    public void publish(Transaction tx) {
        try {
            kafkaTemplate.send(topic, tx.transactionId(), tx);
            log.info("✅ Sent tx id={} amount={} fraud={}", tx.transactionId(), tx.amount(), (tx.label() != null));
        } catch (Exception ex) {
            log.error("🔥 Failed to publish tx {} : {}", tx.transactionId(), ex.getMessage(), ex);
        }
    }
}
