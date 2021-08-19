package elfak.masterrad.queryservice.services;

import elfak.masterrad.queryservice.models.dto.SensorMeasurementDTO;

public interface SensorService {

    SensorMeasurementDTO storeMeasurement(SensorMeasurementDTO sensorMeasurement);

    SensorMeasurementDTO readSensorMeasurement(Long sensorId);

}
