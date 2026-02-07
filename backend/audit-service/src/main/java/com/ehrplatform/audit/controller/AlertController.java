package com.ehrplatform.audit.controller;

import com.ehrplatform.audit.entity.Alert;
import com.ehrplatform.audit.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * REST controller for alert management.
 */
@RestController
@RequestMapping("/admin/alerts")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'SYS_ADMIN', 'SUPER_ADMIN', 'ADMIN0', 'ADMIN1', 'ADMIN2')")
public class AlertController {

    private final AlertRepository alertRepository;

    /**
     * Get alerts with pagination and filtering.
     */
    @GetMapping
    public ResponseEntity<Page<Alert>> getAlerts(
            @RequestParam(required = false, defaultValue = "OPEN") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.DESC, "severity", "createdAt"));
        
        Page<Alert> alerts = alertRepository.findByStatus(status, pageRequest);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get a single alert by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Alert> getAlert(@PathVariable Long id) {
        return alertRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Acknowledge an alert.
     */
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable Long id) {
        return alertRepository.findById(id)
                .map(alert -> {
                    alert.setStatus("ACKED");
                    alert.setUpdatedAt(Instant.now());
                    return ResponseEntity.ok(alertRepository.save(alert));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Resolve an alert.
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id) {
        return alertRepository.findById(id)
                .map(alert -> {
                    alert.setStatus("RESOLVED");
                    alert.setUpdatedAt(Instant.now());
                    return ResponseEntity.ok(alertRepository.save(alert));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get alert statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAlertStats() {
        long openCount = alertRepository.findByStatus("OPEN", PageRequest.of(0, 1)).getTotalElements();
        long ackedCount = alertRepository.findByStatus("ACKED", PageRequest.of(0, 1)).getTotalElements();
        long resolvedCount = alertRepository.findByStatus("RESOLVED", PageRequest.of(0, 1)).getTotalElements();

        Map<String, Object> stats = Map.of(
                "open", openCount,
                "acknowledged", ackedCount,
                "resolved", resolvedCount,
                "total", openCount + ackedCount + resolvedCount
        );

        return ResponseEntity.ok(stats);
    }
}
