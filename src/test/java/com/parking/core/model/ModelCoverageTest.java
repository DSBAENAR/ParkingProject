package com.parking.core.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.parking.core.enums.PaymentStatus;
import com.parking.core.enums.Roles;
import com.parking.core.enums.VehicleType;
import com.parking.core.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ModelCoverageTest {

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("Payment - lifecycle covers getters/setters and callbacks")
    void payment_lifecycle() {
        Payment payment = new Payment();
        payment.setStripePaymentIntentId("pi_123");
        payment.setAmount(5000L);
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.CREATED);
        payment.setVehicleId("ABC-123");
        payment.setCustomerId("cus_123");
        payment.setDescription("Test");
        payment.setId(1L);

        assertEquals("pi_123", payment.getStripePaymentIntentId());
        assertEquals(5000L, payment.getAmount());
        assertEquals("USD", payment.getCurrency());
        assertEquals(PaymentStatus.CREATED, payment.getStatus());
        assertEquals("ABC-123", payment.getVehicleId());
        assertEquals("cus_123", payment.getCustomerId());
        assertEquals("Test", payment.getDescription());
        assertEquals(1L, payment.getId());

        // Test lifecycle callbacks
        payment.onCreate();
        assertNotNull(payment.getCreatedAt());
        assertNotNull(payment.getUpdatedAt());

        payment.onUpdate();
        assertNotNull(payment.getUpdatedAt());
    }

    @Test
    @DisplayName("Product - getters and setters")
    void product_gettersSetters() {
        Product product = new Product(2, 25000L);
        assertEquals(2, product.getHours());
        assertEquals(25000L, product.getPrice());

        product.setId("prod_1");
        assertEquals("prod_1", product.getId());

        product.setHours(5);
        product.setPrice(50000L);
        assertEquals(5, product.getHours());
        assertEquals(50000L, product.getPrice());

        Product empty = new Product();
        assertNull(empty.getHours());
        assertNull(empty.getPrice());
    }

    @Test
    @DisplayName("UserInvoice - getters and setters")
    void userInvoice_gettersSetters() {
        Product product = new Product(1, 10000L);
        UserInvoice invoice = new UserInvoice("inv_1", product);

        assertEquals("inv_1", invoice.getId());
        assertEquals(product, invoice.getProduct());

        invoice.setId("inv_2");
        assertEquals("inv_2", invoice.getId());

        Product newProduct = new Product(3, 30000L);
        invoice.setProduct(newProduct);
        assertEquals(newProduct, invoice.getProduct());

        UserInvoice empty = new UserInvoice();
        assertNull(empty.getId());
        assertNull(empty.getProduct());
    }

    @Test
    @DisplayName("UserId - equals same object")
    void userId_equalsSameObject() {
        UserId id = new UserId();
        assertEquals(id, id);
    }

    @Test
    @DisplayName("UserId - equals different type returns false")
    void userId_equalsDifferentType() {
        UserId id = new UserId();
        assertNotEquals(id, "not a UserId");
    }

    @Test
    @DisplayName("UserId - hashCode consistency")
    void userId_hashCode() {
        UserId id1 = new UserId();
        UserId id2 = new UserId();
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    @DisplayName("UserDetailsDB - loadUserByUsername success")
    void userDetailsDB_success() {
        User user = new User("John", "john", Roles.USER, "john@test.com", null);
        user.setPassword("encoded");
        when(userRepository.findByUsernameOrEmail("john", "john")).thenReturn(Optional.of(user));

        UserDetailsDB service = new UserDetailsDB(userRepository);
        UserDetails details = service.loadUserByUsername("john");

        assertEquals("john", details.getUsername());
    }

    @Test
    @DisplayName("UserDetailsDB - loadUserByUsername not found")
    void userDetailsDB_notFound() {
        when(userRepository.findByUsernameOrEmail("unknown", "unknown")).thenReturn(Optional.empty());

        UserDetailsDB service = new UserDetailsDB(userRepository);
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("unknown"));
    }
}
