package elfak.masterrad.queryservice.controllers;


import elfak.masterrad.queryservice.models.dto.SensorMeasurementDTO;
import elfak.masterrad.queryservice.services.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    @Autowired
    private SensorService sensorService;

    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = {"*"})
    @GetMapping("/{sensorId}")
    public @ResponseBody ResponseEntity<SensorMeasurementDTO> readSensorMeasurement(@PathVariable String sensorId) {
        return ResponseEntity.ok(sensorService.readSensorMeasurement(sensorId));
    }

//    @CrossOrigin(origins = "http://localhost:3000")
//    @GetMapping("/{sensorId}")
//    public @ResponseBody ResponseEntity<SensorMeasurementDTO> readSensorMeasurement(@PathVariable Long sensorId) {
//        return ResponseEntity.ok(sensorService.readSensorMeasurement(sensorId));
//    }

}
