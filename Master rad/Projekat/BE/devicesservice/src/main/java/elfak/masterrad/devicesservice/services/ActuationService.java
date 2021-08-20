package elfak.masterrad.devicesservice.services;

import elfak.masterrad.devicesservice.models.ActuationStatus;
import elfak.masterrad.devicesservice.models.dtos.ActuationDTO;
import elfak.masterrad.devicesservice.models.entities.Actuation;

import java.util.Date;
import java.util.List;

public interface ActuationService {

    Actuation saveActuation(Actuation actuation);

    Actuation getActuationById(Integer id);

    List<Actuation> getActuationsInStatusAndBeforeDate(ActuationStatus status, Date date);

    void sendActuationToDevice(Actuation actuation);
}
