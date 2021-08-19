package com.ubicomp.elfak.sensorImitation;

import java.io.Serializable;

public class SensorMeasurementDTO implements Serializable {

    private String lightIntensity;

    private String groundMoisture;

    private String windIntensity;

    private String soilHumidity;

    public SensorMeasurementDTO() {}

    public SensorMeasurementDTO(String lightIntensity, String groundMoisture, String windIntensity, String soilHumidity) {
        this.lightIntensity = lightIntensity;
        this.groundMoisture = groundMoisture;
        this.windIntensity = windIntensity;
        this.soilHumidity = soilHumidity;
    }

    public String getLightIntensity() {
        return lightIntensity;
    }

    public void setLightIntensity(String lightIntensity) {
        this.lightIntensity = lightIntensity;
    }

    public String getGroundMoisture() {
        return groundMoisture;
    }

    public void setGroundMoisture(String groundMoisture) {
        this.groundMoisture = groundMoisture;
    }

    public String getWindIntensity() {
        return windIntensity;
    }

    public void setWindIntensity(String windIntensity) {
        this.windIntensity = windIntensity;
    }

    public String getSoilHumidity() {
        return soilHumidity;
    }

    public void setSoilHumidity(String soilHumidity) {
        this.soilHumidity = soilHumidity;
    }

    @Override
    public String toString() {
        return "SensorMeasurementDTO{" +
                "lightIntensity='" + lightIntensity + '\'' +
                ", groundMoisture='" + groundMoisture + '\'' +
                ", windIntensity='" + windIntensity + '\'' +
                ", soilHumidity='" + soilHumidity + '\'' +
                '}';
    }
}
