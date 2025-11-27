package com.fdp.alert.service;

import com.fdp.alert.model.FraudAlert;
import com.fdp.common.dto.Transaction;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class AlertStoreService {

    private final Cache<String, FraudAlert> alertsByTx =
            Caffeine.newBuilder().maximumSize(200_000).build();

    private final Deque<FraudAlert> recentAlerts = new ConcurrentLinkedDeque<>();

    private final Cache<String, List<FraudAlert>> alertsByPayer =
            Caffeine.newBuilder().maximumSize(200_000).build();

    public void handle(Transaction tx) {
        if (tx.label() == null || !tx.label().isFraud()) return;

        FraudAlert alert = FraudAlert.builder()
                .transactionId(tx.transactionId())
                .payerId(tx.payerId())
                .amount(tx.amount())
                .timestamp(tx.timestamp())
                .reason(tx.label().reason())
                .original(tx)
                .build();

        alertsByTx.put(alert.getTransactionId(), alert);

        alertsByPayer.asMap().computeIfAbsent(alert.getPayerId(), k -> new ArrayList<>())
                .add(alert);

        synchronized (recentAlerts) {
            recentAlerts.addFirst(alert);
            while (recentAlerts.size() > 100) recentAlerts.removeLast();
        }
    }

    public FraudAlert get(String txId) {
        return alertsByTx.getIfPresent(txId);
    }

    public List<FraudAlert> recent() {
        return new ArrayList<>(recentAlerts);
    }

    public List<FraudAlert> byPayer(String payerId) {
        return alertsByPayer.getIfPresent(payerId);
    }

    public long totalAlerts() {
        return alertsByTx.estimatedSize();
    }
}
