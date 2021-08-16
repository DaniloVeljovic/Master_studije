package elfak.masterrad.queryservice.services.impl;

import elfak.masterrad.queryservice.models.SensorMeasurement;
import elfak.masterrad.queryservice.models.dto.SensorMeasurementDTO;
import elfak.masterrad.queryservice.services.SensorService;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public boolean storeMeasurement(SensorMeasurementDTO sensorMeasurement) {

        InfluxDB influxDB = InfluxDBFactory.connect(host, username, password);

        BatchPoints batchPoints = BatchPoints
                .database("sensorMeasurement")
                .retentionPolicy("defaultPolicy")
                .build();

        Point point1 = Point.measurement("sensorMeasurement")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("sensorType", sensorMeasurement.getSensorType())
                .addField("value", sensorMeasurement.getValue())
                .addField("sensorId", sensorMeasurement.getSensorId())
                .addField("unit", sensorMeasurement.getUnit())
                .build();

        batchPoints.point(point1);
        influxDB.write(batchPoints);

        influxDB.close();

        return true;
    }

    @Override
    public SensorMeasurementDTO readSensorMeasurement(String sensorId) {
        InfluxDB influxDB = InfluxDBFactory.connect(host, username, password);

        QueryResult queryResult = influxDB
                .query(new Query("Select * from sensorMeasurement where sensorId =" + sensorId + " order by time desc", "sensorMeasurement"));

        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<SensorMeasurement> sensorMeasurements = resultMapper
                .toPOJO(queryResult, SensorMeasurement.class);

        influxDB.close();

        return mapPOJOToDTO(sensorMeasurements.get(0));

    }

    private SensorMeasurementDTO mapPOJOToDTO(SensorMeasurement sensorMeasurement) {

        SensorMeasurementDTO sensorMeasurementDTO = new SensorMeasurementDTO();
        sensorMeasurementDTO.setSensorId(sensorMeasurement.getSensorId());
        sensorMeasurementDTO.setSensorType(sensorMeasurement.getSensorType());
        sensorMeasurementDTO.setUnit(sensorMeasurement.getUnit());
        sensorMeasurementDTO.setValue(sensorMeasurement.getValue());
        return sensorMeasurementDTO;

    }
}
