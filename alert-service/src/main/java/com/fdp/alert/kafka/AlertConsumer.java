package com.fdp.alert.kafka;

import com.fdp.alert.service.AlertStoreService;
import com.fdp.common.dto.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertConsumer.class);

    private final AlertStoreService store;

    public AlertConsumer(AlertStoreService store) {
        this.store = store;
    }

    @KafkaListener(topics = "${alert.kafka.input-topic:transactions.out}", groupId = "alert-service")
    public void onMessage(Transaction tx) {
        // Count but only store frauds
        if (tx.label() != null && tx.label().isFraud()) {
            log.warn("🚨 Fraud detected: tx={} amount={} reason={}",
                    tx.transactionId(), tx.amount(), tx.label().reason());
        }

        store.handle(tx);
    }
}
