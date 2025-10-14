package com.parking.core.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.enums.VehicleType;
import com.parking.core.model.Register;
import com.parking.core.model.Vehicle;
import com.parking.core.repository.RegisterRepository;

@Service
public class ReportService {
    private final RegisterRepository registerRepository;
    private final ParkingService parkingService;

    public ReportService(RegisterRepository registerRepository, ParkingService parkingService){
        this.registerRepository = registerRepository;
        this.parkingService = parkingService;
    }

    
    
    /**
     * Generates a monthly report for all resident vehicles and writes it to a text file.
     * <p>
     * The report includes the license plate number, total parked time in minutes, and the amount to pay
     * for each resident vehicle. The report is saved in the "./reports/informe_estacionamiento.txt" file.
     * </p>
     *
     * @return the generated report file
     * @throws ResponseStatusException if there are no resident vehicles or if an I/O error occurs
     */
    public File generateReportMonthly() {
        List<Register> registers = registerRepository.findAllByVehicle_Type(VehicleType.RESIDENT);

        if(registers.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"There are not resident vehicles");

        File file = new File("./reports/informe_estacionamiento.txt");
        file.getParentFile().mkdirs();

        try(PrintWriter writer = new PrintWriter(new FileWriter(file))){
            writer.printf("%-12s %-25s %-20s%n","Num placa", "Tiempo estacionado (min)", "Cantidad a pagar");
            writer.println("----------------------------------------------");

            Map<Vehicle,Integer> totalminutesByvehicle = new HashMap<>();
            for (Register register : registers) {
                totalminutesByvehicle.merge(register.getVehicle(), register.getMinutes(), Integer::sum);
            }
            for (Map.Entry<Vehicle,Integer> entry : totalminutesByvehicle.entrySet()) {
                Vehicle vehicle = entry.getKey();
                Integer minutes = entry.getValue();
                double payment = parkingService.calculatePayment(vehicle);

                writer.printf("%-12s %-25d %-20.2f%n", 
                    vehicle.getId(), minutes, payment);
            }

        }
        
        catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
        }
        

        return file;
    }
}
