package com.fdp.engine.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

@Configuration
public class CaffeineCacheConfig {

    @Value("${caffeine.small-window-minutes:10}")
    private int smallWindowMinutes;

    @Value("${caffeine.rapid-fire-window-seconds:30}")
    private int rapidWindowSeconds;

    // Store recent timestamps per payer for rapid-fire detection
    // key = payerId, value = Deque<Long> epoch millis
    @Bean
    public Cache<String, Deque<Long>> recentTimestampsCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(30))
                .maximumSize(50_000)
                .build();
    }

    // Last device per payer
    @Bean
    public Cache<String, String> lastDeviceCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(smallWindowMinutes))
                .maximumSize(200_000)
                .build();
    }

    // Last location per payer
    @Bean
    public Cache<String, com.fdp.common.dto.GeoLocation> lastLocationCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(smallWindowMinutes))
                .maximumSize(200_000)
                .build();
    }

    // Recent amounts per payer (sliding buffer)
    @Bean
    public Cache<String, Deque<Double>> recentAmountsCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(1))
                .maximumSize(200_000)
                .build();
    }
}
