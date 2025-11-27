package com.fdp.engine.model;

import com.fdp.common.dto.GeoLocation;
import com.fdp.common.dto.Transaction;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

@Component
public class TransactionContext {

    private final Cache<String, Deque<Long>> recentTimestamps;
    private final Cache<String, String> lastDevice;
    private final Cache<String, GeoLocation> lastLocation;
    private final Cache<String, Deque<Double>> recentAmounts;

    public TransactionContext(Cache<String, Deque<Long>> recentTimestamps,
                              Cache<String, String> lastDevice,
                              Cache<String, GeoLocation> lastLocation,
                              Cache<String, Deque<Double>> recentAmounts) {
        this.recentTimestamps = recentTimestamps;
        this.lastDevice = lastDevice;
        this.lastLocation = lastLocation;
        this.recentAmounts = recentAmounts;
    }

    // ---------- Recording ----------

    public void record(Transaction tx) {
        String user = tx.payerId();
        long now = tx.timestamp().toEpochMilli();

        // timestamps sliding window
        Deque<Long> ts = recentTimestamps.get(user, k -> new ArrayDeque<>());
        ts.addLast(now);
        if (ts.size() > 20) ts.removeFirst();
        recentTimestamps.put(user, ts);

        // amount sliding window
        Deque<Double> amts = recentAmounts.get(user, k -> new ArrayDeque<>());
        amts.addLast(tx.amount());
        if (amts.size() > 20) amts.removeFirst();
        recentAmounts.put(user, amts);

        // last device
        lastDevice.put(user, tx.deviceId());

        // last geo
        if (tx.geo() != null) {
            lastLocation.put(user, tx.geo());
        }
    }

    // ---------- Time helpers ----------

    public long minutesSinceLastTx(String payerId) {
        Deque<Long> ts = recentTimestamps.getIfPresent(payerId);
        if (ts == null || ts.isEmpty()) return Long.MAX_VALUE;

        long last = ts.getLast();
        return Duration.between(
                Instant.ofEpochMilli(last),
                Instant.now()
        ).toMinutes();
    }

    public long hoursSinceLastTx(String payerId) {
        Deque<Long> ts = recentTimestamps.getIfPresent(payerId);
        if (ts == null || ts.isEmpty()) return Long.MAX_VALUE;

        long last = ts.getLast();
        return Duration.between(
                Instant.ofEpochMilli(last),
                Instant.now()
        ).toHours();
    }

    // ---------- Geo helpers ----------

    public GeoLocation lastLocation(String payerId) {
        return lastLocation.getIfPresent(payerId);
    }

    public double kmTravelledSinceLast(String payerId, GeoLocation current) {
        GeoLocation prev = lastLocation.getIfPresent(payerId);
        if (prev == null || current == null) return 0;

        return haversineKm(prev.lat(), prev.lon(), current.lat(), current.lon());
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(lat1)) *
                                Math.cos(Math.toRadians(lat2)) *
                                Math.sin(dLon / 2) *
                                Math.sin(dLon / 2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // ---------- Device helpers ----------

    public String lastDevice(String payerId) {
        return lastDevice.getIfPresent(payerId);
    }

    // ---------- Amount helpers ----------

    public double averageRecentAmount(String payerId) {
        Deque<Double> q = recentAmounts.getIfPresent(payerId);
        if (q == null || q.isEmpty()) return 0;

        return q.stream().mapToDouble(d -> d).average().orElse(0);
    }

    public double lastAmount(String payerId) {
        Deque<Double> q = recentAmounts.getIfPresent(payerId);
        if (q == null || q.isEmpty()) return 0;

        return q.getLast();
    }

    public int recentTxCount(String payerId) {
        Deque<Long> ts = recentTimestamps.getIfPresent(payerId);
        return ts == null ? 0 : ts.size();
    }

}
