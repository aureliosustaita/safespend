package com.safespend.web;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {
  @GetMapping("/healthz") public Map<String,Object> ok(){ return Map.of("status","ok","ts", Instant.now()); }
}
