package com.fdp.generator.service;

import com.fdp.common.dto.Transaction;
import com.fdp.generator.config.GeneratorProperties;
import com.fdp.generator.kafka.KafkaPublisher;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

// Orchestrates generation loop. Supports start/stop and runtime rate changes.
// Uses a ScheduledExecutorService and a small worker pool to avoid blocking.
@Service
public class GeneratorService {

    private static final Logger log = LoggerFactory.getLogger(GeneratorService.class);

    private final SyntheticDataProvider provider;
    private final FraudInjector injector;
    private final KafkaPublisher publisher;
    private final GeneratorProperties props;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "generator-scheduler");
        t.setDaemon(true);
        return t;
    });
    private final ExecutorService workers = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "generator-worker");
        t.setDaemon(true);
        return t;
    });

    private volatile ScheduledFuture<?> scheduledFuture;
    private volatile int eventsPerSecond;

    public GeneratorService(SyntheticDataProvider provider,
                            FraudInjector injector,
                            KafkaPublisher publisher,
                            GeneratorProperties props) {
        this.provider = provider;
        this.injector = injector;
        this.publisher = publisher;
        this.props = props;
        this.eventsPerSecond = props.getRate().getEventsPerSecond();
        this.injector.setFraudRatePct(props.getFraud().getOverallRatePercent());
    }

    public synchronized void start() {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            log.info("⏯ Generator already running at {} eps", eventsPerSecond);
            return;
        }
        log.info("▶ Starting generator at {} eps", eventsPerSecond);
        // schedule worker that emits bursts every second distributed across workers
        scheduledFuture = scheduler.scheduleAtFixedRate(this::emitBurst, 0, 1, TimeUnit.SECONDS);
    }

    public synchronized void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
            log.info("⏹ Stopped generator");
        } else {
            log.info("⏹ Generator was not running");
        }
    }

    public synchronized void setRate(int eps) {
        if (eps <= 0) {
            log.warn("⚠️ invalid rate {}, ignoring", eps);
            return;
        }
        this.eventsPerSecond = eps;
        this.injector.setFraudRatePct(props.getFraud().getOverallRatePercent());
        log.info("🔧 Updated rate to {} eps", eps);
        // restart schedule to pick up new rate
        if (scheduledFuture != null) {
            stop();
            start();
        }
    }

    private void emitBurst() {
        int burst = Math.max(1, eventsPerSecond);
        // distribute work across a few tasks to avoid single-thread send loop
        int batch = Math.max(1, burst / 4);
        for (int i = 0; i < 4; i++) {
            final int toSend = (i == 3) ? (burst - batch * 3) : batch;
            workers.submit(() -> {
                for (int j = 0; j < toSend; j++) {
                    try {
                        Transaction tx = provider.nextTransaction();
                        tx = injector.maybeInject(tx);
                        publisher.publish(tx);
                    } catch (Exception ex) {
                        log.error("🔥 generation error: {}", ex.getMessage(), ex);
                    }
                }
            });
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("⏳ Shutting down generator service");
        stop();
        workers.shutdown();
        scheduler.shutdown();
        try {
            workers.awaitTermination(3, TimeUnit.SECONDS);
            scheduler.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) { /* no-op */ }
    }
}
