package com.academic_system.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final DataSource dataSource;
    private final StringRedisTemplate redisTemplate;

    public HealthController(DataSource dataSource, StringRedisTemplate redisTemplate) {
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auth/health")
    public ResponseEntity<Map<String, Object>> healthWithDetails() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> services = new HashMap<>();

        boolean allHealthy = true;

        try {
            dataSource.getConnection().close();
            services.put("database", "UP");
        } catch (Exception e) {
            services.put("database", "DOWN");
            allHealthy = false;
        }

        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            services.put("redis", "UP");
        } catch (Exception e) {
            services.put("redis", "DOWN");
            allHealthy = false;
        }

        response.put("status", allHealthy ? "UP" : "DEGRADED");
        response.put("timestamp", Instant.now().toString());
        response.put("services", services);

        return allHealthy ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);
    }
}
