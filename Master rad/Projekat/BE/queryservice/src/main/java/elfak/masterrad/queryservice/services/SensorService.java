package elfak.masterrad.queryservice.services;

import elfak.masterrad.queryservice.models.dto.SensorMeasurementDTO;

public interface SensorService {

    boolean storeMeasurement(SensorMeasurementDTO sensorMeasurement);

    SensorMeasurementDTO readSensorMeasurement(String sensorId);

}
