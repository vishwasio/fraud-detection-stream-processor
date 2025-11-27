package com.fdp.engine.controller;

import com.fdp.common.dto.Transaction;
import com.fdp.engine.rules.Rule;
import com.fdp.engine.service.RuleEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rules")
public class RuleEngineController {
    private static final Logger log = LoggerFactory.getLogger(RuleEngineController.class);

    private final RuleEngineService engine;

    public RuleEngineController(RuleEngineService engine) {
        this.engine = engine;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluate(@RequestBody Transaction tx) {
        var res = engine.evaluate(tx);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/test-batch")
    public ResponseEntity<?> testBatch(@RequestBody List<Transaction> batch) {
        var r = batch.stream().map(engine::evaluate).toList();
        return ResponseEntity.ok(r);
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        var res = engine.getRules().stream()
                .map(Rule::name)
                .toList();
        return ResponseEntity.ok(res);
    }
}
