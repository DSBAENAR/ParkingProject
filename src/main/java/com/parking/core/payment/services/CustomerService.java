package com.parking.core.payment.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.enums.Currencies;
import com.parking.core.payment.Requests.CustomerRequest;
import com.parking.core.payment.Requests.UserAddress;
import com.parking.core.payment.response.CustomerResponse;
import com.parking.core.payment.utils.CardInfo;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;

@Service
public class CustomerService {

    /**
     * Adds a new customer to the Stripe system if they do not already exist.
     *
     * @param user The {@link CustomerRequest} object containing the customer's details.
     * @return A {@link CustomerResponse} object containing the details of the newly created customer.
     * @throws StripeException If an error occurs while interacting with the Stripe API.
     * @throws ResponseStatusException If the customer already exists (HTTP 409 Conflict) 
     *                                 or if the customer creation request is invalid (HTTP 400 Bad Request).
     */
    public CustomerResponse addNewCustomer(CustomerRequest user) throws StripeException{
        CustomerListParams listParams =
            CustomerListParams
            .builder()
            .setEmail(user.email())
            .setLimit(1L)
            .build();

        CustomerCollection existingCustomers = Customer.list(listParams);

        if (!existingCustomers.getData().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Stripe customer already exists");
        }

        var address = addAdrresToUser(
            user.address().city(), 
            user.address().country(), 
            user.address().line1());
        
        CustomerCreateParams newCustomer = CustomerCreateParams
        .builder()
        .setEmail(user.email())
        .setName(user.name())
        .setAddress(address)
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
            new HashMap<>(created.getMetadata()));
            
    }


    public CardInfo getCardInfoCustomer(PaymentMethod paymentMethod){
       return new CardInfo(
            paymentMethod.getCard().getBrand(), 
            paymentMethod.getType(), 
            paymentMethod.getCard().getCountry(), 
            paymentMethod.getCard().getExpMonth().intValue(),
            paymentMethod.getCard().getExpYear().intValue(), 
            paymentMethod.getCard().getFunding(), 
            paymentMethod.getCard().getLast4(),
            Map.of("checks",paymentMethod.getCard().getChecks()));
    }

    
    public CustomerCreateParams.Address addAdrresToUser(String city, String country, String line1){
        var address = CustomerCreateParams.Address
        .builder()
        .setCity(city)
        .setCountry(country)
        .setLine1(line1)
        .build();
        return address;
    }


}
