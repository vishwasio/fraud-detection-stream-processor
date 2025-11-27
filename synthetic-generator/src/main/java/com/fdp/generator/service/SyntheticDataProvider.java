package com.fdp.generator.service;

import com.fdp.common.dto.FraudLabel;
import com.fdp.common.dto.GeoLocation;
import com.fdp.common.dto.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

// Lightweight synthetic transaction generator.
// Keeps data reproducible-ish and small memory footprint.
@Component
public class SyntheticDataProvider {

    private static final Logger log = LoggerFactory.getLogger(SyntheticDataProvider.class);
    private final Random rng = new Random();

    public Transaction nextTransaction() {
        String txnId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        double amount = sampleAmount();
        String payer = "payer-" + (1 + rng.nextInt(5000));
        String payee = "payee-" + (1 + rng.nextInt(2000));
        String merchant = "merchant-" + (1 + rng.nextInt(500));
        String mcc = sampleMcc();
        String method = sampleMethod();
        String city = sampleCity();
        GeoLocation geo = new GeoLocation(sampleLat(), sampleLon(), "IN", city);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cardPresent", rng.nextBoolean());
        metadata.put("threeDS", rng.nextBoolean() ? "passed" : "failed");

        // label null by default; FraudInjector will set label when needed
        Transaction tx = new Transaction(txnId, now, amount, "INR", payer, payee,
                merchant, mcc, method, city, sampleIp(), "device-" + rng.nextInt(20000),
                geo, metadata, null);

        log.debug("🐛 [provider] generated tx id={} amount={}", tx.transactionId(), tx.amount());
        return tx;
    }

    private double sampleAmount() {
        double val = Math.exp(rng.nextGaussian() * 1.1 + 4.0);
        return Math.round(val * 100.0) / 100.0;
    }

    private double sampleLat() { return 19.0 + rng.nextDouble() * 10.0; }
    private double sampleLon() { return 72.0 + rng.nextDouble() * 10.0; }

    private String sampleCity() {
        String[] cities = {"Mumbai","Delhi","Bengaluru","Chennai","Kolkata"};
        return cities[rng.nextInt(cities.length)];
    }

    private String sampleMcc() {
        String[] arr = {"Retail","Grocery","Travel","Electronics","DigitalGoods"};
        return arr[rng.nextInt(arr.length)];
    }

    private String sampleMethod() {
        String[] m = {"CARD","UPI","NETBANKING","WALLET"};
        return m[rng.nextInt(m.length)];
    }

    private String sampleIp() {
        // simple random IP in private ranges
        return String.format("192.168.%d.%d", rng.nextInt(255), rng.nextInt(255));
    }
}
