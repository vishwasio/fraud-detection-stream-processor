package com.fdp.processor.client;

import java.util.List;

// Minimal DTO that matches the JSON returned by fraud-rule-engine /rules/evaluate

public class EvaluationResult {
    private boolean fraud;
    private double totalScore;
    private double threshold;
    private List<RuleFired> rulesFired;

    public boolean isFraud() { return fraud; }
    public void setFraud(boolean fraud) { this.fraud = fraud; }

    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }

    public List<RuleFired> getRulesFired() { return rulesFired; }
    public void setRulesFired(List<RuleFired> rulesFired) { this.rulesFired = rulesFired; }

    public static class RuleFired {
        private String rule;
        private boolean fraud;
        private double score;
        private double weight;
        private double weightedScore;
        private String message;

        public String getRule() { return rule; }
        public void setRule(String rule) { this.rule = rule; }

        public boolean isFraud() { return fraud; }
        public void setFraud(boolean fraud) { this.fraud = fraud; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }

        public double getWeightedScore() { return weightedScore; }
        public void setWeightedScore(double weightedScore) { this.weightedScore = weightedScore; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
