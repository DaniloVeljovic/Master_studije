package elfak.masterrad.analyticsservice.models.dto;

public class AnalyzedMeasurementDTO {

    private Double lightIntensity;

    private Double groundMoisture;

    private Double windIntensity;

    private Double soilHumidity;

    private String predictedClass;

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
        return "AnalyzedMeasurementDTO{" +
                "lightIntensity=" + lightIntensity +
                ", groundMoisture=" + groundMoisture +
                ", windIntensity=" + windIntensity +
                ", soilHumidity=" + soilHumidity +
                ", predictedClass='" + predictedClass + '\'' +
                '}';
    }
}
