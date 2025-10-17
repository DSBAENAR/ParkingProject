package com.parking.core.payment.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.payment.Requests.CustomerRequest;
import com.parking.core.payment.services.CustomerService;
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
    }

    /**
     * Handles the creation of a new customer.
     *
     * @param toAdd the customer request object containing the details of the customer to be added.
     * @return a ResponseEntity containing a success message and the created customer object if the operation is successful,
     *         or an error message with the appropriate HTTP status code in case of an exception.
     * @throws ResponseStatusException if there is an issue with the request, such as invalid input.
     * @throws StripeException if there is an issue with the Stripe payment processing.
     */
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
