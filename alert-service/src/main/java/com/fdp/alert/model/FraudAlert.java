package com.fdp.alert.model;

import com.fdp.common.dto.Transaction;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class FraudAlert {
    private String transactionId;
    private String payerId;
    private double amount;
    private Instant timestamp;
    private String reason;
    private Transaction original;
}
