package com.parking.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parking.core.config.TwilioConfig.TwilioInitializer;

@ExtendWith(MockitoExtension.class)
class SmsNotificationServiceTest {

    private SmsNotificationService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new SmsNotificationService(new TwilioInitializer());
        setField(service, "parkingName", "Test Parking");
        setField(service, "twilioPhoneNumber", "+10000000000");
        setField(service, "twilioWhatsappNumber", "+10000000001");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("null initializer - SMS not sent")
    void nullInitializer_smsNotSent() throws Exception {
        SmsNotificationService nullService = new SmsNotificationService(null);
        setField(nullService, "parkingName", "Test Parking");
        setField(nullService, "twilioPhoneNumber", "+10000000000");
        setField(nullService, "twilioWhatsappNumber", "+10000000001");

        assertDoesNotThrow(() ->
                nullService.sendEntrySms("+573001234567", "ABC-123", LocalDateTime.now()));
    }

    @Test
    @DisplayName("null initializer - WhatsApp not sent")
    void nullInitializer_whatsAppNotSent() throws Exception {
        SmsNotificationService nullService = new SmsNotificationService(null);
        setField(nullService, "parkingName", "Test Parking");
        setField(nullService, "twilioPhoneNumber", "+10000000000");
        setField(nullService, "twilioWhatsappNumber", "+10000000001");

        assertDoesNotThrow(() ->
                nullService.sendExitWhatsApp("+573001234567", "ABC-123", 60, 25.00, "https://buy.stripe.com/test"));
    }

    @Test
    @DisplayName("sendEntrySms - success")
    void sendEntrySms_success() {
        try (MockedStatic<Message> messageMock = Mockito.mockStatic(Message.class)) {
            MessageCreator creator = mock(MessageCreator.class);
            Message message = mock(Message.class);
            when(creator.create()).thenReturn(message);
            messageMock.when(() -> Message.creator(
                    any(PhoneNumber.class), any(PhoneNumber.class), anyString()
            )).thenReturn(creator);

            service.sendEntrySms("+573001234567", "ABC-123", LocalDateTime.of(2025, 1, 15, 10, 30));

            messageMock.verify(() -> Message.creator(
                    any(PhoneNumber.class), any(PhoneNumber.class), contains("ABC-123")
            ));
        }
    }

    @Test
    @DisplayName("sendExitSms - success with correct format")
    void sendExitSms_success() {
        try (MockedStatic<Message> messageMock = Mockito.mockStatic(Message.class)) {
            MessageCreator creator = mock(MessageCreator.class);
            Message message = mock(Message.class);
            when(creator.create()).thenReturn(message);
            messageMock.when(() -> Message.creator(
                    any(PhoneNumber.class), any(PhoneNumber.class), anyString()
            )).thenReturn(creator);

            service.sendExitSms("+573001234567", "ABC-123", 120, 50.00, "https://buy.stripe.com/test42");

            messageMock.verify(() -> Message.creator(
                    any(PhoneNumber.class), any(PhoneNumber.class), contains("buy.stripe.com/test42")
            ));
        }
    }

    @Test
    @DisplayName("sendExitWhatsApp - success with whatsapp prefix")
    void sendExitWhatsApp_success() {
        try (MockedStatic<Message> messageMock = Mockito.mockStatic(Message.class)) {
            MessageCreator creator = mock(MessageCreator.class);
            Message message = mock(Message.class);
            when(creator.create()).thenReturn(message);
            messageMock.when(() -> Message.creator(
                    any(PhoneNumber.class), any(PhoneNumber.class), anyString()
            )).thenReturn(creator);

            service.sendExitWhatsApp("+573001234567", "ABC-123", 60, 25.00, "https://buy.stripe.com/test7");

            messageMock.verify(() -> Message.creator(
                    eq(new PhoneNumber("whatsapp:+573001234567")),
                    eq(new PhoneNumber("whatsapp:+10000000001")),
                    contains("buy.stripe.com/test7")
            ));
        }
    }

    @Test
    @DisplayName("sendEntrySms - Twilio error handled gracefully")
    void sendEntrySms_error() {
        try (MockedStatic<Message> messageMock = Mockito.mockStatic(Message.class)) {
            MessageCreator creator = mock(MessageCreator.class);
            when(creator.create()).thenThrow(new RuntimeException("Twilio down"));
            messageMock.when(() -> Message.creator(
                    any(PhoneNumber.class), any(PhoneNumber.class), anyString()
            )).thenReturn(creator);

            assertDoesNotThrow(() ->
                    service.sendEntrySms("+573001234567", "ABC-123", LocalDateTime.now()));
        }
    }
}
