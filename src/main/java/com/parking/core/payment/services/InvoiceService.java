package com.parking.core.payment.services;


import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.parking.core.enums.Currencies;
import com.parking.core.payment.Requests.InvoiceRequest;
import com.parking.core.payment.Requests.ProductInfo;
import com.parking.core.payment.Requests.UserTax;
import com.parking.core.payment.response.InvoiceResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceItem;
import com.stripe.model.TaxRate;
import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.InvoiceItemCreateParams;
import com.stripe.param.TaxRateCreateParams;

@Service
public class InvoiceService {

    
    /**
     * Creates an invoice for a user based on the provided invoice request.
     *
     * @param invoiceRequest The request object containing details about the customer, 
     *                       product, and currency for the invoice.
     * @return An {@link InvoiceResponse} object containing the details of the created invoice, 
     *         including the invoice ID, currency, customer, tax information, and product details.
     * @throws StripeException If an error occurs while interacting with the Stripe API.
     */
    public InvoiceResponse createAnInvoceForAUser(InvoiceRequest invoiceRequest) throws StripeException{
        TaxRateCreateParams taxRate = TaxRateCreateParams.builder()
        .setActive(true)
        .setCountry("CO")
        .setPercentage(BigDecimal.valueOf(19.0))
        .setState("DC")
        .setDisplayName("IVA")
        .setInclusive(true)
        .build();

        TaxRate rate = TaxRate.create(taxRate);

        InvoiceCreateParams invoiceParams = InvoiceCreateParams
        .builder()
        .setCustomer(invoiceRequest.customer())
        .setCurrency(invoiceRequest.currency().name())
        .setAutoAdvance(false)
        .build();

        Invoice invoice = Invoice.create(invoiceParams);

        InvoiceItemCreateParams itemParam =
        InvoiceItemCreateParams.builder()
        .setAmount(invoiceRequest.product().getHours() * invoiceRequest.product().getPrice() * 100)
        .setCustomer(invoiceRequest.customer())
        .setCurrency(invoiceRequest.currency().name())
        .setDescription("Time in parking")
        .addTaxRate(rate.getId())
        .setInvoice(invoice.getId())
        .build();

        var item = InvoiceItem.create(itemParam);


        UserTax tax = new UserTax(
            taxRate.getActive(),
            taxRate.getCountry(),
            taxRate.getPercentage(),
            taxRate.getState()
        );

        var product = new ProductInfo(item.getDescription(), item.getAmount(), Currencies.valueOf(item.getCurrency().toUpperCase()), rate.getId());
        return new InvoiceResponse(
            invoice.getId(),
            Currencies.valueOf(invoice.getCurrency().toUpperCase()), 
            invoice.getCustomer(), 
            tax, 
            Map.of("product",product));

    }
}
