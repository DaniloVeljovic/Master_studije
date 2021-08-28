package elfak.masterrad.queryservice.controllers;


import elfak.masterrad.queryservice.models.dto.SensorMeasurementDTO;
import elfak.masterrad.queryservice.services.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;


@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    @Autowired
    private SensorService sensorService;

    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = {"*"})
    @GetMapping("/minutes/{minutes}")
    public @ResponseBody ResponseEntity<List<SensorMeasurementDTO>> readSensorMeasurementFromNow(@PathVariable Long minutes) {
        return ResponseEntity.ok(sensorService.readSensorMeasurementsInTheLastMinutes(minutes));
    }

    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = {"*"})
    @GetMapping("/from/{from}/to/{to}")
    public @ResponseBody ResponseEntity<List<SensorMeasurementDTO>> readSensorMeasurementBetweenTwoDates(@PathVariable Instant from, @PathVariable Instant to) {
        return ResponseEntity.ok(sensorService.readSensorMeasurementsInBetweenDates(from, to));
    }

    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = {"*"})
    @GetMapping("/latest")
    public @ResponseBody ResponseEntity<SensorMeasurementDTO> readLatestMeasurement() {
        return ResponseEntity.ok(sensorService.readLatestSensorMeasurement());
    }

    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = {"*"})
    @GetMapping("/lightIntensity/{threshold}")
    public @ResponseBody ResponseEntity<List<SensorMeasurementDTO>> readSensorMeasurementOverLightIntensityThreshold(Double lightIntensityThreshold) {
        return ResponseEntity.ok(sensorService.readSensorMeasurementOverLightIntensityThreshold(lightIntensityThreshold));
    }
}
