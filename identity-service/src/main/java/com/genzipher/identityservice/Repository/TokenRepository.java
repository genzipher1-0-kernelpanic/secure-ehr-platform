package com.genzipher.identityservice.Repository;

import com.genzipher.identityservice.Model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
        select t
          from Token t
         where t.user.id = :userId
           and t.accessTokenHash = :accessHash
           and t.accessRevokedAt is null
           and t.accessExpiresAt > :now
    """)
    Optional<Token> findActiveAccessToken(Long userId, byte[] accessHash, Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Token t
           set t.accessRevokedAt = :now,
               t.refreshRevokedAt = :now
         where t.user.id = :userId
           and (
                (t.accessRevokedAt is null and t.accessExpiresAt > :now)
             or (t.refreshRevokedAt is null and t.refreshExpiresAt > :now)
           )
    """)
    int revokeAllActiveTokenPairs(Long userId, Instant now);

}
