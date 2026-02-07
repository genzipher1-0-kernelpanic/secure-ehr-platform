package com.genzipher.identityservice.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Objects;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(
            org.springframework.core.env.Environment env) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(env.getProperty("spring.mail.host"));
        sender.setPort(Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.mail.port"))));
        sender.setUsername(env.getProperty("spring.mail.username"));
        sender.setPassword(env.getProperty("spring.mail.password"));

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return sender;
    }

}