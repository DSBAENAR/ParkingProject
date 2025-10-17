package com.parking.core.payment.services;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.parking.core.payment.response.CardResponse;
import com.parking.core.payment.utils.CardInfo;
import com.parking.core.payment.utils.CardRequest;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.PaymentMethodCreateParams;

@Service
public class CardService {

    public CardResponse attachCardToCustomer(CardRequest request) throws StripeException {
        PaymentMethodCreateParams params = PaymentMethodCreateParams.builder()
            .setType(PaymentMethodCreateParams.Type.CARD)
            .setCard(PaymentMethodCreateParams.CardDetails.builder()
                .setNumber(request.number())
                .setExpMonth(request.expMonth())
                .setExpYear(request.expYear())
                .setNumber(request.number())
                .setCvc(request.cvc())
                .build()
            )
            .build();

        PaymentMethod payment = PaymentMethod.create(params);

        payment.attach(PaymentMethodAttachParams.builder()
            .setCustomer(request.customer().id())
            .build());

        var cardInfo = new CardInfo(
            payment.getCard().getBrand(), 
            payment.getType(), 
            payment.getCard().getCountry(), 
            payment.getCard().getExpMonth().intValue(), 
            payment.getCard().getExpYear().intValue(), 
            payment.getCard().getFunding(), 
            payment.getCard().getLast4(),
            payment.getCard().getChecks()
            );

        return new CardResponse(request.customer(), cardInfo);
    }
}
