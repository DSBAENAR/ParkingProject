package com.parking.core.config;

import com.twilio.Twilio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "twilio", name = "account-sid", matchIfMissing = false)
public class TwilioConfig {

    private static final Logger log = LoggerFactory.getLogger(TwilioConfig.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Bean
    public TwilioInitializer twilioInitializer() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio initialized with account {}", accountSid);
        return new TwilioInitializer();
    }

    public static class TwilioInitializer {}
}
