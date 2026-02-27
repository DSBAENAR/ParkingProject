package com.parking.core.auth.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.stripe.Stripe;

import jakarta.annotation.PostConstruct;


/**
 * Configuration component that initialises the Stripe API key on application startup.
 * <p>
 * Reads the {@code sk_stripe} property (typically from a {@code .env} file) and
 * assigns it to {@link com.stripe.Stripe#apiKey} via a {@link PostConstruct} hook.
 * </p>
 */
@Component
public class ApiConfig {

    @Value("${sk_stripe}")
    private String key;

    @PostConstruct
    public void init(){
        Stripe.apiKey = key;
        
    }
    

}
