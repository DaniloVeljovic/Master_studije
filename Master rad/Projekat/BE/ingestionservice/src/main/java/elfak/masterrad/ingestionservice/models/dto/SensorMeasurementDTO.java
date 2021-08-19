package elfak.masterrad.ingestionservice.models.dto;

import java.io.Serializable;

public class SensorMeasurementDTO implements Serializable {

    private String requestId;

    private Double lightIntensity;

    private Double groundMoisture;

    private Double windIntensity;

    private Double soilHumidity;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Double getLightIntensity() {
        return lightIntensity;
    }

    public void setLightIntensity(Double lightIntensity) {
        this.lightIntensity = lightIntensity;
    }

    public Double getGroundMoisture() {
        return groundMoisture;
    }

    public void setGroundMoisture(Double groundMoisture) {
        this.groundMoisture = groundMoisture;
    }

    public Double getWindIntensity() {
        return windIntensity;
    }

    public void setWindIntensity(Double windIntensity) {
        this.windIntensity = windIntensity;
    }

    public Double getSoilHumidity() {
        return soilHumidity;
    }

    public void setSoilHumidity(Double soilHumidity) {
        this.soilHumidity = soilHumidity;
    }

    @Override
    public String toString() {
        return "SensorMeasurementDTO{" +
                "requestId='" + requestId + '\'' +
                ", lightIntensity=" + lightIntensity +
                ", groundMoisture=" + groundMoisture +
                ", windIntensity=" + windIntensity +
                ", soilHumidity=" + soilHumidity +
                '}';
    }
}
