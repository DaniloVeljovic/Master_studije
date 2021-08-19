package elfak.masterrad.devicesservice.kafka.listeners;

import elfak.masterrad.analyticsservice.kafka.models.ActuationMessage;
import elfak.masterrad.devicesservice.entities.Actuation;
import elfak.masterrad.devicesservice.services.ActuationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ActuationMessageListener {

    private final Logger logger = LoggerFactory.getLogger(ActuationMessageListener.class);

    @Value(value = "${kafka.actuationTopic}")
    private String actuationTopic;

    @Autowired
    private ActuationService actuationService;

    @KafkaListener(topics = "${kafka.actuationTopic}", containerFactory = "greetingKafkaListenerContainerFactory")
    public void messageListener(ActuationMessage message) {
        logger.info("Received actuation message: " + message);
        //servis da sacuva
        actuationService.saveActuation(mapMessageToEntity(message));
    }

    private Actuation mapMessageToEntity(ActuationMessage message) {
        Actuation actuation = new Actuation();
        actuation.setLength(message.getLength());
        actuation.setActivationDate(message.getActivationDate());
        actuation.setPinToActivate(message.getPinToActivate());
        return actuation;
    }
}
