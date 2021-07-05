package com.ubicomp.elfak.usmeni.events.publisher;

import com.ubicomp.elfak.usmeni.events.SensorValueReadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SensorValueReadEventPublisher {

    private static Logger logger = LoggerFactory.getLogger(SensorValueReadEventPublisher.class);

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(final String sensorType, final Double value) {
        logger.info("Publishing event.");
        SensorValueReadEvent event = new SensorValueReadEvent(this, sensorType, value);
        applicationEventPublisher.publishEvent(event);
    }

}
