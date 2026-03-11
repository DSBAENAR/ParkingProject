package com.parking.core.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.parking.core.config.TwilioConfig.TwilioInitializer;

@Service
public class SmsNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Nullable
    private final TwilioInitializer twilioInitializer;

    @Value("${twilio.phone-number:}")
    private String twilioPhoneNumber;

    @Value("${twilio.whatsapp-number:}")
    private String twilioWhatsappNumber;

    @Value("${twilio.messaging-service-sid:}")
    private String messagingServiceSid;

    @Value("${parking.name}")
    private String parkingName;

    public SmsNotificationService(@Nullable TwilioInitializer twilioInitializer) {
        this.twilioInitializer = twilioInitializer;
    }

    public void sendEntrySms(String phoneNumber, String plate, LocalDateTime entryTime) {
        String message = String.format(
                "%s - Bienvenido! Vehiculo %s ingreso a las %s.",
                parkingName, plate, entryTime.format(TIME_FMT));
        sendSms(phoneNumber, message);
    }

    public void sendExitSms(String phoneNumber, String plate, int minutes, double amount, String payUrl) {
        String message = String.format(
                "%s - Vehiculo %s. Duracion: %dmin. Total: $%.0f. Pague aqui: %s",
                parkingName, plate, minutes, amount, shortenUrl(payUrl));
        sendSms(phoneNumber, message);
    }

    public void sendExitWhatsApp(String phoneNumber, String plate, int minutes, double amount, String payUrl) {
        String message = String.format(
                "%s - Vehiculo %s. Duracion: %dmin. Total: $%.0f. Pague aqui: %s",
                parkingName, plate, minutes, amount, shortenUrl(payUrl));
        sendWhatsApp(phoneNumber, message);
    }

    private String shortenUrl(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://tinyurl.com/api-create.php?url=" + url))
                    .GET().build();
            String shortened = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString()).body();
            if (shortened != null && shortened.startsWith("https://")) {
                log.info("URL shortened: {}", shortened);
                return shortened;
            }
        } catch (IOException | InterruptedException e) {
            log.warn("URL shortening failed, using original: {}", e.getMessage());
        }
        return url;
    }

    private String normalizePhone(String phoneNumber) {
        if (phoneNumber == null) return null;
        String digits = phoneNumber.trim();
        return digits.startsWith("+") ? digits : "+57" + digits;
    }

    private void sendSms(String phoneNumber, String message) {
        phoneNumber = normalizePhone(phoneNumber);
        if (twilioInitializer == null) {
            log.warn("SMS not sent (Twilio not configured): {}", phoneNumber);
            return;
        }
        try {
            MessageCreator creator = (messagingServiceSid != null && !messagingServiceSid.isBlank())
                    ? Message.creator(new PhoneNumber(phoneNumber), messagingServiceSid, message)
                    : Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(twilioPhoneNumber), message);
            creator.create();
            log.info("SMS sent to {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
        }
    }

    private void sendWhatsApp(String phoneNumber, String message) {
        phoneNumber = normalizePhone(phoneNumber);
        if (twilioInitializer == null) {
            log.warn("WhatsApp not sent (Twilio not configured): {}", phoneNumber);
            return;
        }
        try {
            Message.creator(
                    new PhoneNumber("whatsapp:" + phoneNumber),
                    new PhoneNumber("whatsapp:" + twilioWhatsappNumber),
                    message
            ).create();
            log.info("WhatsApp sent to {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp to {}: {}", phoneNumber, e.getMessage());
        }
    }
}
