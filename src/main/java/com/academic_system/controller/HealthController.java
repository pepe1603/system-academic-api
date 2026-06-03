package com.academic_system.controller;

import com.academic_system.dto.auth.ApiResponse;
import com.academic_system.dto.system.HealthDTO;
import com.academic_system.dto.system.MonitorDTO;
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
@RequestMapping("/api/server")
public class HealthController {

    private final DataSource dataSource;
    private final StringRedisTemplate redisTemplate;

    public HealthController(DataSource dataSource, StringRedisTemplate redisTemplate) {
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthDTO>> health() {
        HealthDTO healthDTO = HealthDTO.builder()
                .status("UP")
                .timestamp(Instant.now().toString())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Servicio saludable", healthDTO));
    }

    @GetMapping("/monitor")
    public ResponseEntity<ApiResponse<MonitorDTO>> healthWithDetails() {
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

        MonitorDTO monitorDTO = MonitorDTO.builder()
                .status(allHealthy ? "UP" : "DEGRADED")
                .timestamp(Instant.now().toString())
                .services(services)
                .build();

        String message = allHealthy ? "Todos los servicios operativos" : "Algunos servicios degradados";
        
        return allHealthy 
                ? ResponseEntity.ok(ApiResponse.success(message, monitorDTO)) 
                : ResponseEntity.status(503).body(ApiResponse.error(message));
    }
}
