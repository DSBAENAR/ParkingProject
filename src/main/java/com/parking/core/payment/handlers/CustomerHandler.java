package com.parking.core.payment.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.payment.Requests.CustomerRequest;
import com.parking.core.payment.services.CustomerService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("api/customers")
public class CustomerHandler {
    private final CustomerService customerService;

    public CustomerHandler(CustomerService customerService) {
        this.customerService = customerService;
        Stripe.apiKey = "sk_test_51SIH8RDp6pIK4lk7GYKHBBjVxc1VqVNY4aksZQKW8UutDXCLWkt0HkUxp9pIjxgkaEnuTfHWSU3v4D6HVFjuEcdJ00TZKnEY2i";
    }

    @PostMapping("/customer")
    public ResponseEntity<?> addCustomer(@RequestBody CustomerRequest toAdd) {
        try {
            return ResponseEntity.ok(Map.of(
                "message","Customer created correctly",
                "customer",customerService.addNewCustomer(toAdd)
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity
            .status(e.getStatusCode())
            .body(Map.of("message",e.getReason()));
        } catch (StripeException e) {
            return ResponseEntity
            .status(e.getStatusCode())
            .body(e.getMessage());
        }
    }
    
    
}
