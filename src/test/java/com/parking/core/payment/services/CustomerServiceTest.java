package com.parking.core.payment.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.payment.Requests.CustomerRequest;
import com.parking.core.payment.Requests.UserAddress;
import com.parking.core.payment.response.CustomerResponse;
import com.stripe.model.Address;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;

class CustomerServiceTest {

    private CustomerService customerService;
    private MockedStatic<Customer> customerStatic;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService();
        customerStatic = Mockito.mockStatic(Customer.class);
    }

    @AfterEach
    void tearDown() {
        customerStatic.close();
    }

    private CustomerRequest buildRequest() {
        return new CustomerRequest("cus_123", "John", "john@test.com",
                new UserAddress("CO", "Bogota", "Calle 1"));
    }

    @Test
    @DisplayName("addNewCustomer - success")
    void addNewCustomer_success() throws Exception {
        CustomerCollection emptyCollection = mock(CustomerCollection.class);
        when(emptyCollection.getData()).thenReturn(Collections.emptyList());
        customerStatic.when(() -> Customer.list(any(CustomerListParams.class)))
                .thenReturn(emptyCollection);

        Customer created = mock(Customer.class);
        when(created.getId()).thenReturn("cus_new");
        when(created.getObject()).thenReturn("customer");
        Address addr = mock(Address.class);
        when(addr.getCountry()).thenReturn("CO");
        when(addr.getCity()).thenReturn("Bogota");
        when(addr.getLine1()).thenReturn("Calle 1");
        when(created.getAddress()).thenReturn(addr);
        when(created.getBalance()).thenReturn(0L);
        when(created.getCreated()).thenReturn(1000L);
        when(created.getEmail()).thenReturn("john@test.com");
        when(created.getMetadata()).thenReturn(new HashMap<>());

        customerStatic.when(() -> Customer.create(any(CustomerCreateParams.class)))
                .thenReturn(created);

        CustomerResponse response = customerService.addNewCustomer(buildRequest());

        assertNotNull(response);
        assertEquals("cus_new", response.id());
        assertEquals("john@test.com", response.email());
    }

    @Test
    @DisplayName("addNewCustomer - conflict 409 when customer exists")
    void addNewCustomer_conflict() throws Exception {
        CustomerCollection existingCollection = mock(CustomerCollection.class);
        Customer existing = mock(Customer.class);
        when(existingCollection.getData()).thenReturn(List.of(existing));
        customerStatic.when(() -> Customer.list(any(CustomerListParams.class)))
                .thenReturn(existingCollection);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> customerService.addNewCustomer(buildRequest()));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("addNewCustomer - Stripe API error")
    void addNewCustomer_stripeError() {
        customerStatic.when(() -> Customer.list(any(CustomerListParams.class)))
                .thenThrow(new RuntimeException("Stripe down"));

        assertThrows(RuntimeException.class, () -> customerService.addNewCustomer(buildRequest()));
    }

    @Test
    @DisplayName("addNewCustomer - address mapping works correctly")
    void addNewCustomer_addressMapping() throws Exception {
        CustomerCollection emptyCollection = mock(CustomerCollection.class);
        when(emptyCollection.getData()).thenReturn(Collections.emptyList());
        customerStatic.when(() -> Customer.list(any(CustomerListParams.class)))
                .thenReturn(emptyCollection);

        Customer created = mock(Customer.class);
        when(created.getId()).thenReturn("cus_addr");
        when(created.getObject()).thenReturn("customer");
        Address addr = mock(Address.class);
        when(addr.getCountry()).thenReturn("US");
        when(addr.getCity()).thenReturn("NYC");
        when(addr.getLine1()).thenReturn("5th Ave");
        when(created.getAddress()).thenReturn(addr);
        when(created.getBalance()).thenReturn(100L);
        when(created.getCreated()).thenReturn(2000L);
        when(created.getEmail()).thenReturn("addr@test.com");
        when(created.getMetadata()).thenReturn(new HashMap<>());

        customerStatic.when(() -> Customer.create(any(CustomerCreateParams.class)))
                .thenReturn(created);

        var req = new CustomerRequest("cus_x", "Addr User", "addr@test.com",
                new UserAddress("US", "NYC", "5th Ave"));
        CustomerResponse response = customerService.addNewCustomer(req);

        assertEquals("US", response.address().country());
        assertEquals("NYC", response.address().city());
        assertEquals("5th Ave", response.address().line1());
    }
}
