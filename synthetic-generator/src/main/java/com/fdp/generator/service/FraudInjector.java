package com.fdp.generator.service;

import com.fdp.common.dto.FraudLabel;
import com.fdp.common.dto.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FraudInjector {

    private static final Logger log = LoggerFactory.getLogger(FraudInjector.class);
    private double fraudRatePct = 23.5; // default, will be set by GeneratorService

    public void setFraudRatePct(double pct) {
        this.fraudRatePct = pct;
    }

    public Transaction maybeInject(Transaction tx) {
        boolean fraud = Math.random() * 100.0 < fraudRatePct;
        if (!fraud) {
            return tx;
        }
        FraudLabel label = new FraudLabel(true, "SYNTH_DETERMINISTIC");
        Transaction t = new Transaction(tx.transactionId(), tx.timestamp(), tx.amount(), tx.currency(),
                tx.payerId(), tx.payeeId(), tx.merchantId(), tx.merchantCategory(),
                tx.paymentMethod(), tx.location(), tx.ipAddress(), tx.deviceId(), tx.geo(),
                tx.metadata(), label);
        log.info("🔥 [injector] injected fraud for txId={} reason={}", t.transactionId(), label.reason());
        return t;
    }
}
