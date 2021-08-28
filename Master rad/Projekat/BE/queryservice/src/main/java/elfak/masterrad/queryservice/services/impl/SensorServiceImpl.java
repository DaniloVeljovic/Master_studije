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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class SensorServiceImpl implements SensorService {

    @Value("${influx.host}")
    private String host;

    @Value("${influx.username}")
    private String username;

    @Value("${influx.password}")
    private String password;

    @Autowired
    SimpMessagingTemplate template;

    private final Logger logger = LoggerFactory.getLogger(SensorServiceImpl.class);

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
        logger.info("Saved measurement: " + sensorMeasurement);
        influxDB.close();

        template.convertAndSend("/topic/message", sensorMeasurement);

        return sensorMeasurement;
    }

    @Override
    public SensorMeasurementDTO readLatestSensorMeasurement() {
        InfluxDB influxDB = InfluxDBFactory.connect(host, username, password);

        QueryResult queryResult = influxDB
                .query(new Query("SELECT * FROM sensorMeasurement ORDER BY time DESC LIMIT 1", "sensorMeasurement"));

        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<SensorMeasurement> sensorMeasurements = resultMapper
                .toPOJO(queryResult, SensorMeasurement.class);

        influxDB.close();

        return mapPOJOToDTO(sensorMeasurements.get(0));
    }

    @Override
    public List<SensorMeasurementDTO> readSensorMeasurementsInBetweenDates(Instant from, Instant to) {
        InfluxDB influxDB = InfluxDBFactory.connect(host, username, password);

        QueryResult queryResult = influxDB
                .query(new Query("SELECT * FROM sensorMeasurement WHERE time >= '" + from + "' AND time <= '" + to + "' LIMIT 5", "sensorMeasurement"));

        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<SensorMeasurement> sensorMeasurements = resultMapper
                .toPOJO(queryResult, SensorMeasurement.class);

        influxDB.close();

        return mapPOJOsToDTOs(sensorMeasurements);
    }

    @Override
    public List<SensorMeasurementDTO> readSensorMeasurementsInTheLastMinutes(Long minutes) {
        Instant to = Instant.now();
        Instant from = to.minusMillis(minutes);
        return readSensorMeasurementsInBetweenDates(from, to);
    }

    @Override
    public List<SensorMeasurementDTO> readSensorMeasurementOverLightIntensityThreshold(Double lightIntensityThreshold) {
        InfluxDB influxDB = InfluxDBFactory.connect(host, username, password);

        QueryResult queryResult = influxDB
                .query(new Query("SELECT * FROM sensorMeasurement WHERE lightIntensity >= " + lightIntensityThreshold + " LIMIT 5", "sensorMeasurement"));

        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<SensorMeasurement> sensorMeasurements = resultMapper
                .toPOJO(queryResult, SensorMeasurement.class);

        influxDB.close();

        return mapPOJOsToDTOs(sensorMeasurements);
    }

    private SensorMeasurementDTO mapPOJOToDTO(SensorMeasurement sensorMeasurement) {
        SensorMeasurementDTO sensorMeasurementDTO = new SensorMeasurementDTO();
        sensorMeasurementDTO.setGroundMoisture(sensorMeasurement.getGroundMoisture());
        sensorMeasurementDTO.setLightIntensity(sensorMeasurement.getLightIntensity());
        sensorMeasurementDTO.setSoilHumidity(sensorMeasurement.getSoilHumidity());
        sensorMeasurementDTO.setWindIntensity(sensorMeasurement.getWindIntensity());
        return sensorMeasurementDTO;
    }

    private List<SensorMeasurementDTO> mapPOJOsToDTOs(List<SensorMeasurement> measurements) {
        return measurements.stream().map(this::mapPOJOToDTO).collect(Collectors.toList());
    }
}
