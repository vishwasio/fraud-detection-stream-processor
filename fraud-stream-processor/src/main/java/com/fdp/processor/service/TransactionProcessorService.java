package com.fdp.processor.service;

import com.fdp.common.dto.FraudLabel;
import com.fdp.common.dto.Transaction;
import com.fdp.processor.client.EvaluationResult;
import com.fdp.processor.client.RuleEngineClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionProcessorService {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessorService.class);

    private final RuleEngineClient ruleEngineClient;
    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private final String outTopic;

    public TransactionProcessorService(RuleEngineClient ruleEngineClient,
                                       KafkaTemplate<String, Transaction> kafkaTemplate,
                                       org.springframework.core.env.Environment env) {
        this.ruleEngineClient = ruleEngineClient;
        this.kafkaTemplate = kafkaTemplate;
        this.outTopic = env.getProperty("kafka.topic.out", "transactions.out");
    }

    // Listens to transactions.in topic and processes transactions.
    // The message payload is expected to be deserialized into com.fdp.common.dto.Transaction.
    @KafkaListener(topics = "${kafka.topic.in}", containerFactory = "kafkaListenerContainerFactory")
    public void onTransaction(Transaction tx) {
        try {
            log.debug("⬇ Received tx id={} amount={}", tx.transactionId(), tx.amount());

            EvaluationResult ev = ruleEngineClient.evaluate(tx);

            String reason = String.format("score=%.2f threshold=%.2f", ev.getTotalScore(), ev.getThreshold());
            FraudLabel label = new FraudLabel(ev.isFraud(), reason);

            Transaction enriched = new Transaction(
                    tx.transactionId(),
                    tx.timestamp(),
                    tx.amount(),
                    tx.currency(),
                    tx.payerId(),
                    tx.payeeId(),
                    tx.merchantId(),
                    tx.merchantCategory(),
                    tx.paymentMethod(),
                    tx.location(),
                    tx.ipAddress(),
                    tx.deviceId(),
                    tx.geo(),
                    tx.metadata(),
                    label
            );

            kafkaTemplate.send(outTopic, enriched.transactionId(), enriched);
            log.info("⬆ Published enriched tx id={} fraud={}", enriched.transactionId(), enriched.label().isFraud());
        } catch (Exception ex) {
            log.error("Processing failed for tx {}: {}", (tx == null ? "<null>" : tx.transactionId()), ex.getMessage(), ex);
        }
    }
}
