package com.genzipher.identityservice.Service;

public interface MailService {

    void sendPasswordResetCode(String toEmail, String code);

}
