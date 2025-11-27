package com.fdp.engine.rules.impl;

import com.fdp.common.dto.Transaction;
import com.fdp.engine.model.TransactionContext;
import com.fdp.engine.model.RuleResult;
import com.fdp.engine.rules.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

// Suspicious high-value spending during late-night hours.
// Uses UTC as default.
@Component
@Order(40)
public class NightOutSpendingRule implements Rule {
    private static final Logger log = LoggerFactory.getLogger(NightOutSpendingRule.class);

    private static final int START_HOUR = 23;
    private static final int END_HOUR = 5; // wraps midnight
    private static final double MULTIPLIER = 2.5;
    private static final double SCORE = 35.0;

    @Override
    public String name() { return "NightOutSpendingRule"; }

    @Override
    public RuleResult apply(Transaction tx, TransactionContext ctx) {
        var z = ZonedDateTime.ofInstant(tx.timestamp(), ZoneId.of("UTC"));
        int h = z.getHour();

        boolean late = (h >= START_HOUR) || (h <= END_HOUR);
        if (!late) {
            return RuleResult.builder()
                    .fraud(false)
                    .score(0.0)
                    .ruleName(name())
                    .message("not-night")
                    .build();
        }

        double avg = ctx.averageRecentAmount(tx.payerId());
        if (avg <= 0) {
            return RuleResult.builder()
                    .fraud(false)
                    .score(0.0)
                    .ruleName(name())
                    .message("no-history")
                    .build();
        }

        double ratio = tx.amount() / avg;
        boolean fraud = ratio >= MULTIPLIER;
        double score = fraud ? SCORE : 0.0;
        String msg = String.format("hour=%d cur=%.2f avg=%.2f ratio=%.2f", h, tx.amount(), avg, ratio);

        log.debug("🌙 [{}] {}", name(), msg);

        return RuleResult.builder()
                .fraud(fraud)
                .score(score)
                .ruleName(name())
                .message(fraud ? "night-spend-suspicious " + msg : "ok")
                .build();
    }
}
