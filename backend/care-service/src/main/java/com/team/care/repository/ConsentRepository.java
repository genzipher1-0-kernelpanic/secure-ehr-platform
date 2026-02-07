package com.team.care.repository;

import com.team.care.entity.Consent;
import com.team.care.entity.ConsentScope;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsentRepository extends JpaRepository<Consent, Long> {

    @Query("""
            select count(c) > 0 from Consent c
            where c.patientId = :patientId
              and c.granteeUserId = :granteeUserId
              and (c.scope = :scope or c.scope = com.team.care.entity.ConsentScope.ALL)
              and c.revokedAt is null
              and c.validFrom <= :now
              and (c.validTo is null or c.validTo >= :now)
            """)
    boolean existsActiveConsent(@Param("patientId") Long patientId,
                                @Param("granteeUserId") Long granteeUserId,
                                @Param("scope") ConsentScope scope,
                                @Param("now") Instant now);
}
