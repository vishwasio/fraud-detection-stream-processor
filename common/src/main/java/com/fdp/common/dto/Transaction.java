package com.fdp.common.dto;

import java.time.Instant;
import java.util.Map;

public record Transaction(
        String transactionId,
        Instant timestamp,
        double amount,
        String currency,
        String payerId,
        String payeeId,
        String merchantId,
        String merchantCategory,
        String paymentMethod,
        String location,
        String ipAddress,
        String deviceId,
        GeoLocation geo,
        Map<String, Object> metadata,
        FraudLabel label
) {}
