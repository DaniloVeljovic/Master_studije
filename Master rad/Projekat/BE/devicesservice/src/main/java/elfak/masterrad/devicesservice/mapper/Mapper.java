package elfak.masterrad.devicesservice.mapper;

import elfak.masterrad.analyticsservice.kafka.models.ActuationMessage;
import elfak.masterrad.devicesservice.models.dtos.ActuationDTO;
import elfak.masterrad.devicesservice.models.entities.Actuation;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public ActuationMessage mapDomainObjectToActuationMessage(Actuation actuation){
        ActuationMessage actuationMessage = new ActuationMessage();
        actuationMessage.setDeviceId(actuation.getDeviceId());
        actuationMessage.setActivationDate(actuation.getActivationDate());
        actuationMessage.setLength(actuation.getLength());
        actuationMessage.setPinToActivate(actuation.getPinToActivate());
        return actuationMessage;
    }

    public Actuation mapDTOToDomainObject(ActuationDTO actuationDTO) {
        Actuation actuation = new Actuation();
        actuation.setActivationDate(actuationDTO.getActivationDate());
        actuation.setLength(actuationDTO.getLength());
        actuation.setPinToActivate(actuationDTO.getPinToActivate());
        return actuation;
    }

    public Actuation mapMessageToEntity(ActuationMessage message) {
        Actuation actuation = new Actuation();
        actuation.setLength(message.getLength());
        actuation.setActivationDate(message.getActivationDate());
        actuation.setPinToActivate(message.getPinToActivate());
        return actuation;
    }

}
