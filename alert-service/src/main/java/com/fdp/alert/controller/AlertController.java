package com.fdp.alert.controller;

import com.fdp.alert.model.FraudAlert;
import com.fdp.alert.service.AlertStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertStoreService service;

    public AlertController(AlertStoreService service) {
        this.service = service;
    }

    @GetMapping("/recent")
    public List<FraudAlert> recent() {
        return service.recent();
    }

    @GetMapping("/{txId}")
    public ResponseEntity<?> get(@PathVariable String txId) {
        FraudAlert a = service.get(txId);
        return a == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(a);
    }

    @GetMapping("/payer/{payerId}")
    public List<FraudAlert> byPayer(@PathVariable String payerId) {
        return service.byPayer(payerId);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(
                "totalAlerts=" + service.totalAlerts()
        );
    }
}
