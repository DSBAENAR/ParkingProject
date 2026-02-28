package com.parking.core.service;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
public class SmsNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final SnsClient snsClient;

    @Value("${parking.name}")
    private String parkingName;

    @Value("${parking.frontend-url}")
    private String frontendUrl;

    public SmsNotificationService(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public void sendEntrySms(String phoneNumber, String plate, LocalDateTime entryTime) {
        String message = String.format(
                "%s - Bienvenido! Vehiculo %s ingreso a las %s.",
                parkingName, plate, entryTime.format(TIME_FMT));
        send(phoneNumber, message);
    }

    public void sendExitSms(String phoneNumber, String plate, int minutes, double amount, long registerId) {
        String payUrl = frontendUrl + "/pay/" + registerId;
        String message = String.format(
                "%s - Vehiculo %s. Duracion: %dmin. Total: $%.2f. Pague aqui: %s",
                parkingName, plate, minutes, amount, payUrl);
        send(phoneNumber, message);
    }

    private void send(String phoneNumber, String message) {
        try {
            snsClient.publish(PublishRequest.builder()
                    .phoneNumber(phoneNumber)
                    .message(message)
                    .build());
            log.info("SMS sent to {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
        }
    }
}
