package com.fdp.engine.rules;

import com.fdp.engine.model.TransactionContext;

@FunctionalInterface
public interface Condition {
    boolean matches(TransactionContext ctx);
}
