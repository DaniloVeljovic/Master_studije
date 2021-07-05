package com.ubicomp.elfak.usmeni.controllers;

import com.ubicomp.elfak.usmeni.models.dto.SensorMeasurementDTO;
import com.ubicomp.elfak.usmeni.services.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    @Autowired
    private SensorService sensorService;

    @PostMapping()
    public @ResponseBody
    ResponseEntity<SensorMeasurementDTO> createSensorMeasurement(@RequestBody SensorMeasurementDTO sensorMeasurement) {
        return ResponseEntity.ok(sensorService.createSensorMeasurement(sensorMeasurement));
    }

    @GetMapping("/{sensorId}")
    public @ResponseBody ResponseEntity<SensorMeasurementDTO> readSensorMeasurement(@PathVariable Long sensorId) {
        return ResponseEntity.ok(sensorService.readSensorMeasurement(sensorId));
    }

}
