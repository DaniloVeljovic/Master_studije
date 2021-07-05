package com.ubicomp.elfak.usmeni.services.impl;

import com.ubicomp.elfak.usmeni.events.publisher.SensorValueReadEventPublisher;
import com.ubicomp.elfak.usmeni.models.SensorMeasurement;
import com.ubicomp.elfak.usmeni.models.dto.SensorMeasurementDTO;
import com.ubicomp.elfak.usmeni.services.SensorService;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
public class SensorServiceImpl implements SensorService {

    @Autowired
    private SensorValueReadEventPublisher sensorValueReadEventPublisher;

    @Value("${influx.host}")
    private String host;

    @Value("${influx.username}")
    private String username;

    @Value("${influx.password}")
    private String password;

    //ovo bi trebalo da bude reddis - ovo je olaksana primena YOLO
    public static ConcurrentMap<String, Double> currentValuesReadFromSensors = new ConcurrentHashMap<>();

    static {
        currentValuesReadFromSensors.put("temp", 0.0);
        currentValuesReadFromSensors.put("gm", 0.0);
        currentValuesReadFromSensors.put("light", 0.0);
    }

    @Override
    public SensorMeasurementDTO createSensorMeasurement(SensorMeasurementDTO sensorMeasurement) {
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

        currentValuesReadFromSensors.put(sensorMeasurement.getSensorType(), sensorMeasurement.getValue());

        sensorValueReadEventPublisher.publishEvent(sensorMeasurement.getSensorType(), sensorMeasurement.getValue());

        return sensorMeasurement;
    }

    @Override
    public SensorMeasurementDTO readSensorMeasurement(Long sensorId) {
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
