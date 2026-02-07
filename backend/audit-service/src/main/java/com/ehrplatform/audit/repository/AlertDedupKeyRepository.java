package com.ehrplatform.audit.repository;

import com.ehrplatform.audit.entity.AlertDedupKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AlertDedupKeyRepository extends JpaRepository<AlertDedupKey, String> {

    /**
     * Check if dedup key exists and is not expired
     */
    boolean existsByDedupKeyAndExpiresAtAfter(String dedupKey, Instant now);

    /**
     * Delete expired dedup keys
     */
    @Modifying
    @Query("DELETE FROM AlertDedupKey adk WHERE adk.expiresAt < :now")
    int deleteExpiredKeys(@Param("now") Instant now);
}
