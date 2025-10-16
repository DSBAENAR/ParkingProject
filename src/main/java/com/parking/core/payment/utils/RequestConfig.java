package com.parking.core.payment.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.stripe.net.RequestOptions;

@Configuration
public class RequestConfig {
    @Value("${secret-key}")
    String apiKey;
    @Bean
    RequestOptions requestOptions (){
        return RequestOptions.builder().setApiKey(apiKey).build();
    }
}
