package com.parking.core.auth.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.stripe.Stripe;

import jakarta.annotation.PostConstruct;


@Component
public class ApiConfig {

    @Value("${sk_stripe}")
    private String key;

    @PostConstruct
    public void init(){
        Stripe.apiKey = key;
        
    }
    

}
