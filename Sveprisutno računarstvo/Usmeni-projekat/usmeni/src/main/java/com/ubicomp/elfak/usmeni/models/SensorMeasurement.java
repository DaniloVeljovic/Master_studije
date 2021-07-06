package com.ubicomp.elfak.usmeni.models;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

@Measurement(name = "sensorMeasurement")
public class SensorMeasurement {

    @Column(name = "time")
    private Instant time;

    @Column(name = "sensorType")
    private String sensorType;

    @Column(name = "value")
    private Double value;

    @Column(name = "sensorId")
    private Integer sensorId;

    @Column(name = "unit")
    private String unit;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Integer getSensorId() {
        return sensorId;
    }

    public void setSensorId(Integer sensorId) {
        this.sensorId = sensorId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
