package com.fdp.engine.service;

import com.fdp.common.dto.Transaction;
import com.fdp.engine.config.RulesProperties;
import com.fdp.engine.model.RuleResult;
import com.fdp.engine.model.TransactionContext;
import com.fdp.engine.rules.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RuleEngineService {
    private static final Logger log = LoggerFactory.getLogger(RuleEngineService.class);

    private final List<Rule> rules;
    private final TransactionContext txContext;
    private final RulesProperties props;

    public RuleEngineService(List<Rule> rules, TransactionContext txContext, RulesProperties props) {
        this.rules = rules;
        this.txContext = txContext;
        this.props = props;
    }

    public EvaluationResult evaluate(Transaction tx) {
        double total = 0.0;
        List<RuleFired> fired = new ArrayList<>();

        for (Rule r : rules) {
            String rname = r.name();
            RulesProperties.RuleConfig cfg = props.getRules()
                    .getOrDefault(rname, new RulesProperties.RuleConfig());

            if (!cfg.isEnabled()) {
                continue;
            }

            RuleResult rr = r.apply(tx, txContext);
            double weight = cfg.getWeight();
            double weighted = rr.getScore() * weight;
            total += weighted;

            fired.add(new RuleFired(
                    rname,
                    rr.isFraud(),
                    rr.getScore(),
                    weight,
                    weighted,
                    rr.getMessage()
            ));
        }

        boolean fraud = total >= props.getThreshold();
        return new EvaluationResult(fraud, total, props.getThreshold(), fired);
    }

    public record RuleFired(String rule, boolean fraud, double score,
                            double weight, double weightedScore, String message) {}

    public record EvaluationResult(boolean fraud, double totalScore,
                                   double threshold, List<RuleFired> rulesFired) {}

    public List<Rule> getRules() {
        return rules;
    }
}
