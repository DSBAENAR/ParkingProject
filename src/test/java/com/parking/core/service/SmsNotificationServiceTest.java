package com.parking.core.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@ExtendWith(MockitoExtension.class)
class SmsNotificationServiceTest {

    @Mock
    private SnsClient snsClient;

    private SmsNotificationService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new SmsNotificationService(snsClient);
        setField(service, "parkingName", "Test Parking");
        setField(service, "frontendUrl", "http://localhost:3000");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("null client - SMS not sent")
    void nullClient_smsNotSent() throws Exception {
        SmsNotificationService nullService = new SmsNotificationService(null);
        setField(nullService, "parkingName", "Test Parking");
        setField(nullService, "frontendUrl", "http://localhost:3000");

        // Should not throw
        assertDoesNotThrow(() ->
                nullService.sendEntrySms("+573001234567", "ABC-123", LocalDateTime.now()));
    }

    @Test
    @DisplayName("sendEntrySms - success")
    void sendEntrySms_success() {
        when(snsClient.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().messageId("msg_1").build());

        service.sendEntrySms("+573001234567", "ABC-123", LocalDateTime.of(2025, 1, 15, 10, 30));

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(captor.capture());

        PublishRequest request = captor.getValue();
        assertEquals("+573001234567", request.phoneNumber());
        assertTrue(request.message().contains("ABC-123"));
        assertTrue(request.message().contains("10:30"));
        assertTrue(request.message().contains("Test Parking"));
    }

    @Test
    @DisplayName("sendExitSms - success with correct format")
    void sendExitSms_success() {
        when(snsClient.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().messageId("msg_2").build());

        service.sendExitSms("+573001234567", "ABC-123", 120, 50.00, 42L);

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(captor.capture());

        PublishRequest request = captor.getValue();
        assertTrue(request.message().contains("120min"));
        assertTrue(request.message().contains("$50.00"));
        assertTrue(request.message().contains("http://localhost:3000/pay/42"));
    }

    @Test
    @DisplayName("sendEntrySms - SNS error handled gracefully")
    void sendEntrySms_error() {
        when(snsClient.publish(any(PublishRequest.class)))
                .thenThrow(new RuntimeException("SNS down"));

        assertDoesNotThrow(() ->
                service.sendEntrySms("+573001234567", "ABC-123", LocalDateTime.now()));
    }
}
