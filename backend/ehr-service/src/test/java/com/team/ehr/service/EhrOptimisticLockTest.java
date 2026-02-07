package com.team.ehr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.team.ehr.crypto.CryptoResult;
import com.team.ehr.crypto.CryptoService;
import com.team.ehr.dto.EhrUpdateRequest;
import com.team.ehr.entity.EhrCategory;
import com.team.ehr.entity.EhrRecordCurrent;
import com.team.ehr.exception.ConflictException;
import com.team.ehr.repository.EhrRecordCurrentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class EhrOptimisticLockTest {

    @Autowired
    private EhrRecordService ehrRecordService;

    @Autowired
    private EhrRecordCurrentRepository currentRepository;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        currentRepository.deleteAll();
    }

    @Test
    void updateRejectsStaleVersion() throws Exception {
        setAuth(99L, "ADMIN");
        CryptoResult encrypted = cryptoService.encryptJson("{\"allergies\":\"none\"}");
        EhrRecordCurrent current = new EhrRecordCurrent();
        current.setPatientId(10L);
        current.setCategory(EhrCategory.CLINICAL);
        current.setCurrentVersion(1);
        current.setCiphertext(encrypted.getCiphertext());
        current.setKeyId(cryptoService.getKeyId());
        current.setContentHash(encrypted.getHashHex());
        currentRepository.save(current);

        ObjectNode patch = objectMapper.createObjectNode();
        patch.put("allergies", "pollen");
        EhrUpdateRequest request = new EhrUpdateRequest();
        request.setCategory(EhrCategory.CLINICAL);
        request.setExpectedVersion(1);
        request.setPatch(patch);

        assertEquals(2, ehrRecordService.updateRecord(10L, request).getNewVersion());

        EhrUpdateRequest stale = new EhrUpdateRequest();
        stale.setCategory(EhrCategory.CLINICAL);
        stale.setExpectedVersion(1);
        stale.setPatch(patch);

        assertThrows(ConflictException.class, () -> ehrRecordService.updateRecord(10L, stale));
    }

    private void setAuth(Long userId, String role) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("userId", userId)
                .claim("role", role)
                .audience(java.util.List.of("test"))
                .issuer("identity-service")
                .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
