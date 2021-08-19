package elfak.masterrad.ingestionservice.services;

import elfak.masterrad.ingestionservice.models.dto.SensorMeasurementDTO;

public interface SensorService {

    SensorMeasurementDTO storeMeasurement(SensorMeasurementDTO sensorMeasurement);

}
