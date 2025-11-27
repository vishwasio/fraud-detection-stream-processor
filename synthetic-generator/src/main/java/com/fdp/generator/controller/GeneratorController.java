package com.fdp.generator.controller;

import com.fdp.generator.service.GeneratorService;
import com.fdp.generator.config.GeneratorProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generator")
public class GeneratorController {

    private final GeneratorService generator;
    private final GeneratorProperties props;

    public GeneratorController(GeneratorService generator, GeneratorProperties props) {
        this.generator = generator;
        this.props = props;
    }

    @PostMapping("/start")
    public ResponseEntity<String> start() {
        generator.start();
        return ResponseEntity.ok("started");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stop() {
        generator.stop();
        return ResponseEntity.ok("stopped");
    }

    @PostMapping("/setRate")
    public ResponseEntity<String> setRate(@RequestParam("eps") int eps) {
        generator.setRate(eps);
        return ResponseEntity.ok("rate set to " + eps);
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("configured rate: " + props.getRate().getEventsPerSecond());
    }
}
