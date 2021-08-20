package elfak.masterrad.analyticsservice.kafka.models;

import java.util.Date;

public class ActuationMessage {

    private Date activationDate;
    private Long length;
    private String pinToActivate;
    private String deviceId;

    public ActuationMessage() {

    }

    public ActuationMessage(Date activationDate, Long length, String pinToActivate, String deviceId) {
        this.activationDate = activationDate;
        this.length = length;
        this.pinToActivate = pinToActivate;
        this.deviceId = deviceId;
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

    @Override
    public String toString() {
        return "ActuationMessage{" +
                "activationDate=" + activationDate +
                ", length=" + length +
                ", pinToActivate='" + pinToActivate + '\'' +
                '}';
    }
}
