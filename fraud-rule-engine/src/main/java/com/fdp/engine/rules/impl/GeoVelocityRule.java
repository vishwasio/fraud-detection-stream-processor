package com.fdp.engine.rules.impl;

import com.fdp.common.dto.GeoLocation;
import com.fdp.common.dto.Transaction;
import com.fdp.engine.model.TransactionContext;
import com.fdp.engine.model.RuleResult;
import com.fdp.engine.rules.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// If user appears to have traveled impossibly far since last tx -> risk.
// Uses km/h heuristic: distance (km) / hours since last tx.
@Component
@Order(20)
public class GeoVelocityRule implements Rule {
    private static final Logger log = LoggerFactory.getLogger(GeoVelocityRule.class);

    // thresholds are conservative
    private static final double SUSPICIOUS_SPEED_KMH = 500.0; // suspicious threshold
    private static final double HIGHLY_SUSPICIOUS_SPEED_KMH = 1200.0; // near-impossible

    @Override
    public String name() {
        return "GeoVelocityRule";
    }

    @Override
    public RuleResult apply(Transaction tx, TransactionContext ctx) {
        GeoLocation cur = tx.geo();
        if (cur == null) {
            return RuleResult.builder()
                    .fraud(false)
                    .score(0.0)
                    .ruleName(name())
                    .message("no-geo")
                    .build();
        }

        GeoLocation last = ctx.lastLocation(tx.payerId());
        if (last == null) {
            return RuleResult.builder()
                    .fraud(false)
                    .score(0.0)
                    .ruleName(name())
                    .message("no-previous-geo")
                    .build();
        }

        long hoursSince = ctx.hoursSinceLastTx(tx.payerId());
        if (hoursSince <= 0) hoursSince = 1;

        double km = ctx.kmTravelledSinceLast(tx.payerId(), cur);
        double kmh = km / (double) hoursSince;

        double score = 0.0;
        boolean fraud = false;
        String msg = String.format("km=%.2f hours=%d km/h=%.2f", km, hoursSince, kmh);

        if (kmh >= HIGHLY_SUSPICIOUS_SPEED_KMH) {
            score = 100.0;
            fraud = true;
            msg = "impossible-travel " + msg;
        } else if (kmh >= SUSPICIOUS_SPEED_KMH) {
            score = 50.0;
            fraud = true;
            msg = "suspicious-travel " + msg;
        }

        log.debug("🐾 [{}] {} -> {}", name(), tx.transactionId(), msg);

        return RuleResult.builder()
                .fraud(fraud)
                .score(score)
                .ruleName(name())
                .message(msg)
                .build();
    }
}
