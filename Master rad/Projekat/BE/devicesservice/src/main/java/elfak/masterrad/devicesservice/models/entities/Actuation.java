package elfak.masterrad.devicesservice.models.entities;

import elfak.masterrad.devicesservice.models.ActuationStatus;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Actuation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;

    private Date activationDate;

    private Long length;

    private String pinToActivate;

    private ActuationStatus status;

    private String deviceId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Date activationDate) {
        this.activationDate = activationDate;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getPinToActivate() {
        return pinToActivate;
    }

    public void setPinToActivate(String pinToActivate) {
        this.pinToActivate = pinToActivate;
    }

    public ActuationStatus getStatus() {
        return status;
    }

    public void setStatus(ActuationStatus status) {
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "Actuation{" +
                "id=" + id +
                ", activationDate=" + activationDate +
                ", length=" + length +
                ", pinToActivate='" + pinToActivate + '\'' +
                ", status=" + status +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}
