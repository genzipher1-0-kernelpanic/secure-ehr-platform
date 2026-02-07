package com.genzipher.identityservice.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Console-logging mail service for development.
 * Logs the password reset code to stdout instead of sending email.
 */
@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    @Override
    public void sendPasswordResetCode(String toEmail, String code) {
        log.info("═══════════════════════════════════════════════════════");
        log.info("  PASSWORD RESET CODE for {}", toEmail);
        log.info("  Code: {}", code);
        log.info("  (This code expires in 5 minutes)");
        log.info("═══════════════════════════════════════════════════════");
    }

}
