package elfak.masterrad.ingestionservice.services.impl;

import elfak.masterrad.ingestionservice.models.dto.SensorMeasurementDTO;
import elfak.masterrad.ingestionservice.services.SensorService;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SensorServiceImpl implements SensorService {

    @Value("${influx.host}")
    private String host;

    @Value("${influx.username}")
    private String username;

    @Value("${influx.password}")
    private String password;

    @Override
    public SensorMeasurementDTO storeMeasurement(SensorMeasurementDTO sensorMeasurement) {
        InfluxDB influxDB = InfluxDBFactory.connect(host, username, password);

        BatchPoints batchPoints = BatchPoints
                .database("sensorMeasurement")
                .retentionPolicy("defaultPolicy")
                .build();

        Point point1 = Point.measurement("sensorMeasurement")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("soilHumidity", sensorMeasurement.getSoilHumidity())
                .addField("groundMoisture", sensorMeasurement.getGroundMoisture())
                .addField("lightIntensity", sensorMeasurement.getLightIntensity())
                .addField("windIntensity", sensorMeasurement.getWindIntensity())
                .build();

        batchPoints.point(point1);
        influxDB.write(batchPoints);

        influxDB.close();

        return sensorMeasurement;
    }
}
