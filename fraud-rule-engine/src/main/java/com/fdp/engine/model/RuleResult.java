package com.fdp.engine.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class RuleResult {
    private final boolean fraud;
    private final double score;
    private final String ruleName;
    private final String message;
}
