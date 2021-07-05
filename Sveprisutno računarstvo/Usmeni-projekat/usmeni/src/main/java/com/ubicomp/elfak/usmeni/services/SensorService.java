package com.ubicomp.elfak.usmeni.services;

import com.ubicomp.elfak.usmeni.models.dto.SensorMeasurementDTO;

public interface SensorService {
    SensorMeasurementDTO createSensorMeasurement(SensorMeasurementDTO sensorMeasurement);

    SensorMeasurementDTO readSensorMeasurement(Long sensorId);
}
