package com.fdp.processor.client;

import com.fdp.common.dto.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class RuleEngineClient {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineClient.class);

    private final WebClient webClient;

    public RuleEngineClient(@Value("${rule-engine.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // Calls rule-engine /rules/evaluate and returns EvaluationResult.
    public EvaluationResult evaluate(Transaction tx) {
        try {
            return webClient.post()
                    .uri("/rules/evaluate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(tx)
                    .retrieve()
                    .bodyToMono(EvaluationResult.class)
                    .block();
        } catch (Exception ex) {
            log.error("Error calling rule-engine: {}", ex.getMessage(), ex);
            // return a safe default (no fraud) when rule-engine is unreachable.
            EvaluationResult r = new EvaluationResult();
            r.setFraud(false);
            r.setTotalScore(0.0);
            r.setThreshold(Double.MAX_VALUE);
            r.setRulesFired(null);
            return r;
        }
    }
}
