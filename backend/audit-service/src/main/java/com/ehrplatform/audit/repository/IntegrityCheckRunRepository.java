package com.ehrplatform.audit.repository;

import com.ehrplatform.audit.entity.IntegrityCheckRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegrityCheckRunRepository extends JpaRepository<IntegrityCheckRun, Long> {

    /**
     * Find by status
     */
    Page<IntegrityCheckRun> findByStatus(String status, Pageable pageable);

    /**
     * Find failed checks
     */
    List<IntegrityCheckRun> findByStatusOrderByStartedAtDesc(String status);

    /**
     * Find latest check runs
     */
    Page<IntegrityCheckRun> findAllByOrderByStartedAtDesc(Pageable pageable);
}
