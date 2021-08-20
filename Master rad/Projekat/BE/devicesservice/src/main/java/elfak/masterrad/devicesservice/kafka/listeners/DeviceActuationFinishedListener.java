package elfak.masterrad.devicesservice.kafka.listeners;

import elfak.masterrad.analyticsservice.kafka.models.ActuationMessage;
import elfak.masterrad.devicesservice.mapper.Mapper;
import elfak.masterrad.devicesservice.models.ActuationStatus;
import elfak.masterrad.devicesservice.models.entities.Actuation;
import elfak.masterrad.devicesservice.services.ActuationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

public class DeviceActuationFinishedListener {
    private final Logger logger = LoggerFactory.getLogger(DeviceActuationFinishedListener.class);

    @Autowired
    private ActuationService actuationService;

    @Autowired
    private Mapper mapper;

    @KafkaListener(topics = "${kafka.actuationFinished}", containerFactory = "greetingKafkaListenerContainerFactory")
    public void messageListener(Actuation actuation) {
        logger.info("Finished with actuation: " + actuation);
        actuation.setStatus(ActuationStatus.FINISHED);
        actuationService.saveActuation(actuation);
    }

}
