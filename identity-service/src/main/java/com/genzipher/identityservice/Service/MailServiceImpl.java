package com.genzipher.identityservice.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService{

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetCode(String toEmail, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(toEmail);
        msg.setSubject("Password Reset Code");
        msg.setText("""
                Your password reset code is: %s

                This code expires in 5 minutes.
                If you didn't request this, ignore this email.
                """.formatted(code));

        mailSender.send(msg);
    }

}
