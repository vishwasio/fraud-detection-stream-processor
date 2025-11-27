package com.fdp.generator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "generator")
public class GeneratorProperties {

    private String topic = "transactions.in";
    private Rate rate = new Rate();
    private Fraud fraud = new Fraud();

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Rate getRate() { return rate; }
    public void setRate(Rate rate) { this.rate = rate; }

    public Fraud getFraud() { return fraud; }
    public void setFraud(Fraud fraud) { this.fraud = fraud; }

    public static class Rate {
        private int eventsPerSecond = 500;
        public int getEventsPerSecond() { return eventsPerSecond; }
        public void setEventsPerSecond(int eventsPerSecond) { this.eventsPerSecond = eventsPerSecond; }
    }

    public static class Fraud {
        private double overallRatePercent = 3.5;
        public double getOverallRatePercent() { return overallRatePercent; }
        public void setOverallRatePercent(double overallRatePercent) { this.overallRatePercent = overallRatePercent; }
    }
}
