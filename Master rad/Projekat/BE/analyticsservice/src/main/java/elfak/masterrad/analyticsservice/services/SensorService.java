package elfak.masterrad.analyticsservice.services;


import elfak.masterrad.analyticsservice.models.dto.SensorMeasurementDTO;

import java.util.Date;

public interface SensorService {
    SensorMeasurementDTO createSensorMeasurement(SensorMeasurementDTO sensorMeasurement);

    Object analyzeMeasurement(SensorMeasurementDTO sensorMeasurementDTO);

    void analyzeMeasurementAndActuateIfNeeded(SensorMeasurementDTO sensorMeasurementDTO);

    void sendActuationMessage(Date date, long l, String pin, String deviceId);
}
