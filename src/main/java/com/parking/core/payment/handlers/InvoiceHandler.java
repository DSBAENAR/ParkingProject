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


@RestController
@RequestMapping("api/invoices")
public class InvoiceHandler {
    private final InvoiceService invoiceService;

    public InvoiceHandler(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Handles the creation of an invoice for a user.
     *
     * @param request the {@link InvoiceRequest} object containing the details needed to create the invoice.
     * @return a {@link ResponseEntity} containing a success message and the created invoice if successful,
     *         or an error message if an exception occurs.
     * @throws StripeException if there is an error during the invoice creation process.
     */
    @PostMapping("/invoice")
    public ResponseEntity<?> addInvoice(@RequestBody InvoiceRequest request) {
        try {
            return ResponseEntity.ok(Map.of(
                "message", "Invoice created correctly",
                "invoice", invoiceService.createAnInvoceForAUser(request)
            ));
        } catch (StripeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    
}
