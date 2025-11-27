package com.fdp.engine.rules.impl;

import com.fdp.common.dto.Transaction;
import com.fdp.engine.model.TransactionContext;
import com.fdp.engine.model.RuleResult;
import com.fdp.engine.rules.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Deque;

// Detects bursts of transactions. Best-effort using minutesSinceLastTx and average comparison.
@Component
@Order(5)
public class RapidFireTransactionsRule implements Rule {
    private static final Logger log = LoggerFactory.getLogger(RapidFireTransactionsRule.class);

    // parameters
    private static final long BURST_WINDOW_MINUTES = 1;
    private static final int BURST_TXS_THRESHOLD = 5;
    private static final double SCORE = 60.0;

    @Override
    public String name() { return "RapidFireTransactionsRule"; }

    @Override
    public RuleResult apply(Transaction tx, TransactionContext ctx) {
        long minsSince = ctx.minutesSinceLastTx(tx.payerId());

        // quick heuristic: if last tx was within 1 minute AND many recent txs (in avg computation)
        double avg = ctx.averageRecentAmount(tx.payerId());
        double last = ctx.lastAmount(tx.payerId());
        boolean burst = minsSince <= BURST_WINDOW_MINUTES && last > 0 && avg > 0 && avg > 0;

        // refine burst detection: if avg is similar to last and last is small we still might be in a burst.
        // This heuristic flags activity when last tx is within 1 minute — it's pragmatic.
        if (!burst) {
            return RuleResult.builder()
                    .fraud(false)
                    .score(0.0)
                    .ruleName(name())
                    .message("no-burst")
                    .build();
        }

        String msg = String.format("minsSinceLast=%d avg=%.2f last=%.2f", minsSince, avg, last);
        log.debug("⚡ [{}] {}", name(), msg);

        return RuleResult.builder()
                .fraud(true)
                .score(SCORE)
                .ruleName(name())
                .message("rapid-fire " + msg)
                .build();
    }
}
