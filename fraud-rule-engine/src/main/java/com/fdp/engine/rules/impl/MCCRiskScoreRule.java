package com.fdp.engine.rules.impl;

import com.fdp.common.dto.Transaction;
import com.fdp.engine.model.TransactionContext;
import com.fdp.engine.model.RuleResult;
import com.fdp.engine.rules.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

// Assigns risk by merchant category (MCC/merchantCategory).
@Component
@Order(50)
public class MCCRiskScoreRule implements Rule {
    private static final Logger log = LoggerFactory.getLogger(MCCRiskScoreRule.class);

    // small default risk table
    private static final Map<String, Double> DEFAULT_RISK = Map.of(
            "electronics", 25.0,
            "gambling", 90.0,
            "travel", 30.0,
            "luxury", 40.0,
            "food", 5.0,
            "groceries", 2.0
    );

    @Override
    public String name() { return "MCCRiskScoreRule"; }

    @Override
    public RuleResult apply(Transaction tx, TransactionContext ctx) {
        String mcc = tx.merchantCategory();
        if (mcc == null) {
            return RuleResult.builder()
                    .fraud(false)
                    .score(0.0)
                    .ruleName(name())
                    .message("no-mcc")
                    .build();
        }

        double score = DEFAULT_RISK.getOrDefault(mcc.toLowerCase(), 5.0);
        boolean fraud = score >= 75.0;

        log.debug("🏷️ [{}] mcc={} score={}", name(), mcc, score);

        return RuleResult.builder()
                .fraud(fraud)
                .score(score)
                .ruleName(name())
                .message("mcc-score")
                .build();
    }
}
