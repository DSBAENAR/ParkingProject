package com.parking.core.payment.handlers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.parking.core.payment.services.CardService;
import com.parking.core.payment.utils.CardRequest;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("api/cards")
public class CardHandler {

    private final CardService cardService;

    CardHandler(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/card")
    public ResponseEntity<?> addCard(@RequestBody CardRequest request){
        try {
            return ResponseEntity.ok(cardService.attachCardToCustomer(request));
        } catch (StripeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
