package elfak.masterrad.devicesservice.models.dtos;

import java.util.Date;

public class ActuationDTO {
    private Date activationDate;
    private Long length;
    private String pinToActivate;

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
        return "ActuationDTO{" +
                "activationDate=" + activationDate +
                ", length=" + length +
                ", pinToActivate='" + pinToActivate + '\'' +
                '}';
    }
}
