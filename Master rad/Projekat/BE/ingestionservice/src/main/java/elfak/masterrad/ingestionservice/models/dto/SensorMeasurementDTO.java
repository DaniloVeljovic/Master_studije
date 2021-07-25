package elfak.masterrad.ingestionservice.models.dto;

import java.io.Serializable;

public class SensorMeasurementDTO implements Serializable {

    private String sensorType;

    private Double value;

    private Integer sensorId;

    private String unit;

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

    @Override
    public String toString() {
        return "SensorMeasurementDTO{" +
                "sensorType='" + sensorType + '\'' +
                ", value=" + value +
                ", sensorId=" + sensorId +
                ", unit='" + unit + '\'' +
                '}';
    }
}
