package com.ehrplatform.audit.repository;

import com.ehrplatform.audit.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Find alerts by status
     */
    Page<Alert> findByStatus(String status, Pageable pageable);

    /**
     * Find alerts by type and status
     */
    List<Alert> findByAlertTypeAndStatus(String alertType, String status);

    /**
     * Find alerts by severity and status
     */
    Page<Alert> findBySeverityAndStatus(String severity, String status, Pageable pageable);

    /**
     * Find open alerts ordered by severity and creation time
     */
    Page<Alert> findByStatusOrderBySeverityDescCreatedAtDesc(String status, Pageable pageable);

    /**
     * Find alerts created after a certain time
     */
    List<Alert> findByCreatedAtAfter(Instant since);

    /**
     * Find alerts by actor user ID
     */
    Page<Alert> findByActorUserId(Long actorUserId, Pageable pageable);

    /**
     * Find alerts by actor email
     */
    Page<Alert> findByActorEmail(String actorEmail, Pageable pageable);
}
