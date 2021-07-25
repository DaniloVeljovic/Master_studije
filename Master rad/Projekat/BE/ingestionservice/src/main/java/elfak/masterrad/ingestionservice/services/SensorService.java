package elfak.masterrad.ingestionservice.services;

import elfak.masterrad.ingestionservice.models.dto.SensorMeasurementDTO;

public interface SensorService {

    boolean storeMeasurement(SensorMeasurementDTO sensorMeasurement);

}
