package com.genzipher.identityservice.Repository;

import com.genzipher.identityservice.Model.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    @Query("""
        select prc
          from PasswordResetCode prc
         where prc.user.id = :userId
           and prc.codeHash = :codeHash
           and prc.usedAt is null
           and prc.expiresAt > :now
         order by prc.id desc
    """)
    Optional<PasswordResetCode> findValidCode(Long userId, byte[] codeHash, Instant now);

    @Query("""
        update PasswordResetCode prc
           set prc.usedAt = :now
         where prc.user.id = :userId
           and prc.usedAt is null
    """)
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    int markAllUnusedAsUsed(Long userId, Instant now);

}
