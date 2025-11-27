package com.fdp.engine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "fraud.engine")
@Data
public class RulesProperties {
    private double threshold = 50.0;
    private String aggregateMode = "sum";
    private Map<String, RuleConfig> rules = new HashMap<>();

    @Data
    public static class RuleConfig {
        private boolean enabled = true;
        private double weight = 1.0;
    }
}
