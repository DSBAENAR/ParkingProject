package com.parking.core.payment.services;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentLink;
import com.stripe.model.Price;
import com.stripe.param.PaymentLinkCreateParams;
import com.stripe.param.PriceCreateParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StripePaymentLinkService {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentLinkService.class);

    /**
     * Creates a Stripe Payment Link for the given parking session.
     *
     * @param amount     total to charge in COP (Colombian pesos, zero-decimal currency in Stripe)
     * @param registerId parking register ID, used as description
     * @return the Stripe-hosted payment URL, or {@code null} if creation fails
     */
    public String createPaymentLink(double amount, long registerId) {
        try {
            Price price = Price.create(PriceCreateParams.builder()
                    .setCurrency("cop")
                    .setUnitAmount((long) amount)
                    .setProductData(PriceCreateParams.ProductData.builder()
                            .setName("Parqueadero - Registro #" + registerId)
                            .build())
                    .build());

            PaymentLink link = PaymentLink.create(PaymentLinkCreateParams.builder()
                    .addLineItem(PaymentLinkCreateParams.LineItem.builder()
                            .setPrice(price.getId())
                            .setQuantity(1L)
                            .build())
                    .build());

            log.info("Stripe payment link created for register #{}: {}", registerId, link.getUrl());
            return link.getUrl();
        } catch (StripeException e) {
            log.error("Failed to create Stripe payment link for register #{}: {}", registerId, e.getMessage());
            return null;
        }
    }
}
