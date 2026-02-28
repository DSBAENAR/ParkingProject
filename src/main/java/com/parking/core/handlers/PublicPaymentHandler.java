package com.parking.core.handlers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.model.Register;
import com.parking.core.payment.services.PaymentService;
import com.parking.core.payment.Requests.PaymentRequest;
import com.parking.core.repository.RegisterRepository;
import com.parking.core.service.ParkingService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("api/v1/public/pay")
public class PublicPaymentHandler {

    private final RegisterRepository registerRepository;
    private final ParkingService parkingService;
    private final PaymentService paymentService;

    public PublicPaymentHandler(RegisterRepository registerRepository, ParkingService parkingService,
                                PaymentService paymentService) {
        this.registerRepository = registerRepository;
        this.parkingService = parkingService;
        this.paymentService = paymentService;
    }

    @GetMapping("/{registerId}")
    public ResponseEntity<Map<String, Object>> getPaymentDetails(@PathVariable long registerId) {
        Register register = registerRepository.findById(registerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Register not found"));

        if (register.getExitdate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle has not exited yet");
        }

        double amount = parkingService.calculatePaymentForRegister(register);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("registerId", register.getId());
        response.put("plate", register.getVehicle().getId());
        response.put("vehicleType", register.getVehicle().getType());
        response.put("entryDate", register.getEntrydate());
        response.put("exitDate", register.getExitdate());
        response.put("minutes", register.getMinutes());
        response.put("amount", amount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{registerId}/create-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@PathVariable long registerId)
            throws StripeException {
        Register register = registerRepository.findById(registerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Register not found"));

        if (register.getExitdate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle has not exited yet");
        }

        double amount = parkingService.calculatePaymentForRegister(register);
        long amountInCents = Math.round(amount * 100);

        PaymentRequest paymentRequest = new PaymentRequest(
                amountInCents,
                "USD",
                null,
                register.getVehicle().getId(),
                "Parking fee - " + register.getVehicle().getId());

        return ResponseEntity.ok(paymentService.createPaymentIntent(paymentRequest));
    }
}
