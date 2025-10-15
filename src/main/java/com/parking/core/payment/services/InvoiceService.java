package com.parking.core.payment.services;

import org.springframework.stereotype.Service;

import com.parking.core.enums.Currencies;
import com.parking.core.payment.Requests.InvoiceRequest;
import com.parking.core.payment.response.InvoiceResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.param.InvoiceCreateParams;

@Service
public class InvoiceService {

    /**
     * Creates an invoice for a user based on the provided invoice request.
     *
     * @param customer The invoice request containing customer details and currency information.
     * @return An InvoiceResponse object containing the invoice ID, currency, and customer information.
     * @throws StripeException If an error occurs while creating the invoice using the Stripe API.
     */
    public InvoiceResponse createAnInvoceForAUser(InvoiceRequest customer) throws StripeException{
        InvoiceCreateParams invoiceParams = InvoiceCreateParams
        .builder()
        .setCurrency(Currencies.COP.name())
        .setCustomer(customer.customer())
        .setCurrency(customer.currency().name())
        .build();

        Invoice invoice = Invoice.create(invoiceParams);

        return new InvoiceResponse(invoice.getId(),Currencies.valueOf(invoice.getCurrency().toUpperCase()), invoice.getCustomer());

    }
}
