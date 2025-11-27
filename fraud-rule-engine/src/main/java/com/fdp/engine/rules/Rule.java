package com.fdp.engine.rules;

import com.fdp.common.dto.Transaction;
import com.fdp.engine.model.RuleResult;
import com.fdp.engine.model.TransactionContext;

public interface Rule {
    String name();
    RuleResult apply(Transaction tx, TransactionContext ctx);
}
