package com.parking.core.payment.services;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.enums.Currencies;
import com.parking.core.payment.Requests.CustomerRequest;
import com.parking.core.payment.Requests.UserAddress;
import com.parking.core.payment.response.CustomerResponse;
import com.parking.core.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;

@Service
public class CustomerService {
    private final UserRepository userRepository;

    public CustomerService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public CustomerResponse addNewCustomer(CustomerRequest user) throws StripeException{
        CustomerCreateParams newCustomer = CustomerCreateParams
        .builder()
        .setEmail(user.email())
        .setName(user.name())
        .setAddress(CustomerCreateParams.Address
            .builder()
            .setCity(user.address().city())
            .setCountry(user.address().country())
            .setLine1(user.address().line1())
            .build())
        .build();

        if (newCustomer == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer can't be null");

        var created = Customer.create(newCustomer);

        return new CustomerResponse(
            created.getId(), 
            created.getObject(), 
            new UserAddress(
                created.getAddress().getCountry(),
                created.getAddress().getCity(),
                created.getAddress().getLine1()
            ), 
            created.getBalance(), 
            Currencies.COP,
            created.getCreated(), 
            created.getEmail(), 
            new HashMap<>(created.getMetadata())
            );
    }
}
