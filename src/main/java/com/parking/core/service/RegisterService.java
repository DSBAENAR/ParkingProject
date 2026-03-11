package com.parking.core.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.model.Register;
import com.parking.core.model.Vehicle;
import com.parking.core.model.dto.RegisterEntryRequest;
import com.parking.core.payment.Requests.SendPaymentLinkRequest;
import com.parking.core.payment.services.StripePaymentLinkService;
import com.parking.core.repository.RegisterRepository;
import com.parking.core.repository.VehicleRepository;

/**
 * Service layer for managing parking register operations.
 * <p>
 * Handles vehicle entry and exit from the parking lot, tracking timestamps
 * and calculating the duration of each parking session in minutes.
 * </p>
 *
 * @see Register
 * @see Vehicle
 */
@Service
public class RegisterService {

    private static final Logger log = LoggerFactory.getLogger(RegisterService.class);

    @Value("${parking.frontend-url}")
    private String frontendUrl;

    private final RegisterRepository registerRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingService parkingService;
    private final SmsNotificationService smsNotificationService;
    private final StripePaymentLinkService stripePaymentLinkService;

    public RegisterService(RegisterRepository registerRepository, VehicleRepository vehicleRepository,
                           ParkingService parkingService, SmsNotificationService smsNotificationService,
                           StripePaymentLinkService stripePaymentLinkService) {
        this.registerRepository = registerRepository;
        this.vehicleRepository = vehicleRepository;
        this.parkingService = parkingService;
        this.smsNotificationService = smsNotificationService;
        this.stripePaymentLinkService = stripePaymentLinkService;
    }

    /**
     * Retrieves all parking registers.
     *
     * @return a list of all {@link Register} entries
     * @throws ResponseStatusException with {@code 404 NOT_FOUND} if no registers exist
     */
    public List<Register> getAllRegisters() {
        List<Register> registers = registerRepository.findAll();
        if (registers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No registers found");
        }
        log.info("Retrieved {} registers", registers.size());
        return registers;
    }

    /**
     * Registers a vehicle's entrance into the parking lot.
     * <p>
     * Verifies the vehicle exists in the system and does not already have an active
     * (non-exited) register. Sets the entry timestamp to the current time.
     * </p>
     *
     * @param vehicleToRegister the vehicle entering the parking lot
     * @return the created {@link Register} with entry date set
     * @throws ResponseStatusException with {@code 404 NOT_FOUND} if the vehicle is not registered
     * @throws ResponseStatusException with {@code 400 BAD_REQUEST} if the vehicle already has an active register
     */
    @Transactional
    public Register registerVehicleEntrance(RegisterEntryRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vehicle " + request.vehicleId() + " not found"));

        boolean existingRegister = registerRepository.existsByVehicleAndExitdateIsNull(vehicle);
        if (existingRegister) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Register already exists for this vehicle");
        }

        Register register = new Register(vehicle);
        register.setEntrydate(LocalDateTime.now());
        register.setPhoneNumber(request.phoneNumber());
        register.setNotificationChannel(request.notificationChannel());

        Register saved = registerRepository.save(register);
        log.info("Vehicle {} entered parking - Register #{}", vehicle.getId(), saved.getId());

        if (saved.getPhoneNumber() != null && !saved.getPhoneNumber().isBlank()) {
            smsNotificationService.sendEntrySms(saved.getPhoneNumber(), vehicle.getId(), saved.getEntrydate());
        }

        return saved;
    }

    /**
     * Processes a vehicle's departure from the parking lot.
     * <p>
     * Finds the active register (no exit date) for the vehicle, sets the exit timestamp,
     * and calculates the total parked time in minutes.
     * </p>
     *
     * @param vehicle the vehicle leaving the parking lot
     * @return the updated {@link Register} with exit date and minutes calculated
     * @throws ResponseStatusException with {@code 400 BAD_REQUEST} if no active register exists for the vehicle
     */
    public Register leaveVehicle(Vehicle vehicle) {
        Register existing = registerRepository.findByVehicleAndExitdateIsNull(vehicle)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No active register found for vehicle"));

        existing.setExitdate(LocalDateTime.now());
        int minutes = (int) ChronoUnit.MINUTES.between(existing.getEntrydate(), existing.getExitdate());
        existing.setMinutes(minutes);

        Register saved = registerRepository.save(existing);
        log.info("Vehicle {} left parking after {} minutes - Register #{}", vehicle.getId(), minutes, saved.getId());

        if (saved.getPhoneNumber() != null && !saved.getPhoneNumber().isBlank()) {
            double amount = parkingService.calculatePaymentForRegister(saved);
            String payUrl = stripePaymentLinkService.createPaymentLink(amount, saved.getId());
            if (payUrl == null) {
                payUrl = frontendUrl + "/pay/" + saved.getId();
            }
            if ("whatsapp".equals(saved.getNotificationChannel())) {
                smsNotificationService.sendExitWhatsApp(
                        saved.getPhoneNumber(), vehicle.getId(), minutes, amount, payUrl);
            } else {
                smsNotificationService.sendExitSms(
                        saved.getPhoneNumber(), vehicle.getId(), minutes, amount, payUrl);
            }
        }

        return saved;
    }

    /**
     * Closes the active register for a vehicle and sends a Stripe Payment Link
     * to the provided phone number via the specified channel (sms/whatsapp).
     */
    public void leaveVehicleAndSendLink(SendPaymentLinkRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vehicle " + request.vehicleId() + " not found"));

        Register saved;
        java.util.Optional<Register> activeOpt = registerRepository.findByVehicleAndExitdateIsNull(vehicle);

        if (activeOpt.isPresent()) {
            // Vehicle still inside — close the register
            Register active = activeOpt.get();
            active.setExitdate(LocalDateTime.now());
            int minutes = (int) ChronoUnit.MINUTES.between(active.getEntrydate(), active.getExitdate());
            active.setMinutes(minutes);
            saved = registerRepository.save(active);
        } else {
            // Vehicle already exited — use most recent completed register
            saved = registerRepository.findTopByVehicleOrderByExitdateDesc(vehicle)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "No registers found for vehicle"));
        }

        double amount = parkingService.calculatePaymentForRegister(saved);
        String payUrl = stripePaymentLinkService.createPaymentLink(amount, saved.getId());
        if (payUrl == null) {
            payUrl = frontendUrl + "/pay/" + saved.getId();
        }

        int minutes = saved.getMinutes();
        log.info("Sending payment link for register #{} to {} via {}", saved.getId(), request.phoneNumber(), request.channel());
        if ("whatsapp".equalsIgnoreCase(request.channel())) {
            smsNotificationService.sendExitWhatsApp(request.phoneNumber(), vehicle.getId(), minutes, amount, payUrl);
        } else {
            smsNotificationService.sendExitSms(request.phoneNumber(), vehicle.getId(), minutes, amount, payUrl);
        }
    }

    /**
     * Closes the active register for a vehicle without sending any notification (cash payment).
     */
    public void leaveVehicleCash(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vehicle " + vehicleId + " not found"));

        Register existing = registerRepository.findByVehicleAndExitdateIsNull(vehicle)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No active register found for vehicle"));

        existing.setExitdate(LocalDateTime.now());
        int minutes = (int) ChronoUnit.MINUTES.between(existing.getEntrydate(), existing.getExitdate());
        existing.setMinutes(minutes);
        registerRepository.save(existing);

        log.info("Cash payment registered for vehicle {} - {} minutes", vehicleId, minutes);
    }
}
