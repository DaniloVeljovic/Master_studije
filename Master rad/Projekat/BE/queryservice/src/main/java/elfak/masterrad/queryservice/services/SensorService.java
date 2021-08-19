package elfak.masterrad.queryservice.services;

import elfak.masterrad.queryservice.models.dto.SensorMeasurementDTO;

import java.time.Instant;
import java.util.List;

public interface SensorService {

    SensorMeasurementDTO storeMeasurement(SensorMeasurementDTO sensorMeasurement);

    SensorMeasurementDTO readLatestSensorMeasurement();

    List<SensorMeasurementDTO> readSensorMeasurementsInBetweenDates(Instant from, Instant to);

    List<SensorMeasurementDTO> readSensorMeasurementsInTheLastMinutes(Long minutes);
}
