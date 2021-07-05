package com.ubicomp.elfak.usmeni.events;


import org.springframework.context.ApplicationEvent;

public class SensorValueReadEvent extends ApplicationEvent {

    private String sensorType;
    private Double value;

    public SensorValueReadEvent(Object source, String sensorType, Double value) {
        super(source);
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
}
