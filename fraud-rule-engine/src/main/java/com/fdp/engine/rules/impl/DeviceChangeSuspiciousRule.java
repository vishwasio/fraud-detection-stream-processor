package com.fdp.engine.rules.impl;

import com.fdp.common.dto.Transaction;
import com.fdp.engine.model.TransactionContext;
import com.fdp.engine.model.RuleResult;
import com.fdp.engine.rules.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Changes device within a short time window => suspect.
@Component
@Order(10)
public class DeviceChangeSuspiciousRule implements Rule {
    private static final Logger log = LoggerFactory.getLogger(DeviceChangeSuspiciousRule.class);

    // thresholds
    private static final long WINDOW_MINUTES = 60;
    private static final double SCORE_ON_CHANGE = 40.0;

    @Override
    public String name() { return "DeviceChangeSuspiciousRule"; }

    @Override
    public RuleResult apply(Transaction tx, TransactionContext ctx) {
        String lastDevice = ctx.lastDevice(tx.payerId());
        String curDevice = tx.deviceId();

        if (curDevice == null) {
            return RuleResult.builder()
                    .fraud(false)
                    .score(0.0)
                    .ruleName(name())
                    .message("no-device")
                    .build();
        }

        if (lastDevice == null) {
            return RuleResult.builder()
                    .fraud(false)
                    .score(0.0)
                    .ruleName(name())
                    .message("no-last-device")
                    .build();
        }

        long mins = ctx.minutesSinceLastTx(tx.payerId());
        boolean changedRecently = !curDevice.equals(lastDevice) && mins <= WINDOW_MINUTES;

        double score = changedRecently ? SCORE_ON_CHANGE : 0.0;
        boolean fraud = changedRecently;

        log.debug("📱 [{}] payer={} lastDevice={} curDevice={} minsSinceLast={} changed={}",
                name(), tx.payerId(), lastDevice, curDevice, mins, changedRecently);

        return RuleResult.builder()
                .fraud(fraud)
                .score(score)
                .ruleName(name())
                .message(changedRecently ? "device-changed-recently" : "ok")
                .build();
    }
}
