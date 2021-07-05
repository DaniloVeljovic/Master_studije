package com.ubicomp.elfak.sensorImitation;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SensorMeasurementCreatedPublisher {

    Logger logger = LoggerFactory.getLogger(SensorMeasurementCreatedPublisher.class);

    @EventListener(ApplicationReadyEvent.class)
    public void testConnection() throws Exception {
        String publisherId = "com.ubicomp.elfak.sensorData";
        IMqttClient publisher = new MqttClient("tcp://localhost:1883",publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(0);
        publisher.connect(options);

        if (!publisher.isConnected()) {
            return;
        }

        while(true) {
            MqttMessage msg = createMqttMessage("temp", "celsius", 1129);
            publishMessage(publisher, msg);

            msg = createMqttMessage("light", "lumen", 1130);
            publishMessage(publisher, msg);

            msg = createMqttMessage("gm", "cm3/m2", 1131);
            publishMessage(publisher, msg);
        }
    }

    private void publishMessage(IMqttClient publisher, MqttMessage msg) throws InterruptedException, MqttException {
        Thread.sleep(1000);
        msg.setQos(0);
        msg.setRetained(true);
        publisher.publish("devices/sensors",msg);
    }

    private MqttMessage createMqttMessage(String sensorType, String unit, Integer sensorId) {
        Random rnd = new Random();
        double value = rnd.nextDouble() * 100.0;
        String format = String.format("{\"value\" : %04.2f , \"sensorId\" : %d, \"unit\" : \"%s\", \"sensorType\" : \"%s\"}", value, sensorId, unit, sensorType);
        logger.info("[Producer] - " + format);
        byte[] payload =
                format.getBytes();
        return new MqttMessage(payload);
    }
}
