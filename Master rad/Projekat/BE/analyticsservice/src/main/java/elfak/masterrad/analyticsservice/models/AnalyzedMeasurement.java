package elfak.masterrad.analyticsservice.models;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

@Measurement(name = "sensorMeasurement")
public class AnalyzedMeasurement {

    @Column(name = "time")
    private Instant time;

    @Column(name = "lightIntensity")
    private Double lightIntensity;

    @Column(name = "groundMoisture")
    private Double groundMoisture;

    @Column(name = "windIntensity")
    private Double windIntensity;

    @Column(name = "soilHumidity")
    private Double soilHumidity;

    @Column(name = "predictedClass")
    private String predictedClass;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
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

    public String getPredictedClass() {
        return predictedClass;
    }

    public void setPredictedClass(String predictedClass) {
        this.predictedClass = predictedClass;
    }

    @Override
    public String toString() {
        return "AnalyzedMeasurement{" +
                "time=" + time +
                ", lightIntensity=" + lightIntensity +
                ", groundMoisture=" + groundMoisture +
                ", windIntensity=" + windIntensity +
                ", soilHumidity=" + soilHumidity +
                ", predictedClass='" + predictedClass + '\'' +
                '}';
    }
}
