package com.fdp.engine.rules.impl;

import com.fdp.common.dto.Transaction;
import com.fdp.engine.model.TransactionContext;
import com.fdp.engine.model.RuleResult;
import com.fdp.engine.rules.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Large sudden increase compared to the user's recent average.
@Component
@Order(30)
public class SuddenAmountSpikeRule implements Rule {
    private static final Logger log = LoggerFactory.getLogger(SuddenAmountSpikeRule.class);

    private static final double SPIKE_FACTOR = 3.0;
    private static final double HIGH_SPIKE_FACTOR = 6.0;

    @Override
    public String name() { return "SuddenAmountSpikeRule"; }

    @Override
    public RuleResult apply(Transaction tx, TransactionContext ctx) {
        double avg = ctx.averageRecentAmount(tx.payerId());
        double cur = tx.amount();
        
        if (avg <= 0.0) {
            return RuleResult.builder()
                    .fraud(false)
                    .score(0.0)
                    .ruleName(name())
                    .message("no-history")
                    .build();
        }

        double ratio = cur / avg;
        boolean fraud = false;
        double score = 0.0;
        String msg = String.format("cur=%.2f avg=%.2f ratio=%.2f", cur, avg, ratio);

        if (ratio >= HIGH_SPIKE_FACTOR) {
            fraud = true;
            score = 80.0;
            msg = "high-spike " + msg;
        } else if (ratio >= SPIKE_FACTOR) {
            fraud = true;
            score = 40.0;
            msg = "spike " + msg;
        }

        log.debug("💸 [{}] {}", name(), msg);

        return RuleResult.builder()
                .fraud(fraud)
                .score(score)
                .ruleName(name())
                .message(msg)
                .build();
    }
}
