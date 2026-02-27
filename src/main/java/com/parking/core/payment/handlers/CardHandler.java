package com.parking.core.payment.handlers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.core.payment.response.CardResponse;
import com.parking.core.payment.services.CardService;
import com.parking.core.payment.utils.CardRequest;
import com.stripe.exception.StripeException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/cards")
public class CardHandler {

    private final CardService cardService;

    CardHandler(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/card")
    public ResponseEntity<CardResponse> addCard(@Valid @RequestBody CardRequest request) throws StripeException {
        return ResponseEntity.ok(cardService.attachCardToCustomer(request));
    }
}
