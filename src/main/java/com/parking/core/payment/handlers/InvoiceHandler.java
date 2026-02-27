package com.parking.core.payment.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.core.payment.Requests.InvoiceRequest;
import com.parking.core.payment.services.InvoiceService;
import com.stripe.exception.StripeException;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/invoices")
public class InvoiceHandler {
    private final InvoiceService invoiceService;

    public InvoiceHandler(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/invoice")
    public ResponseEntity<Map<String, Object>> addInvoice(@Valid @RequestBody InvoiceRequest request) throws StripeException {
        return ResponseEntity.ok(Map.of(
                "message", "Invoice created correctly",
                "invoice", invoiceService.createAnInvoceForAUser(request)));
    }
}
