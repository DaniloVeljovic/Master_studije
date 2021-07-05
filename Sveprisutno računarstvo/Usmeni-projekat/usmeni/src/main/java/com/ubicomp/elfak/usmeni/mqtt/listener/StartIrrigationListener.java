package com.ubicomp.elfak.usmeni.mqtt.listener;

import com.google.gson.Gson;
import com.ubicomp.elfak.usmeni.models.dto.SensorMeasurementDTO;
import com.ubicomp.elfak.usmeni.services.IrrigationService;
import com.ubicomp.elfak.usmeni.services.SensorService;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartIrrigationListener {

    private Logger log = LoggerFactory.getLogger(StartIrrigationListener.class);

    @Autowired
    private SensorService sensorService;

    @Autowired
    private IrrigationService irrigationService;

    @EventListener(ApplicationReadyEvent.class)
    private void initializeSubscriber() throws InterruptedException, MqttException {

        String publisherId = "com.ubicomp.elfak.irrigationListener";
        String topicName = "devices/action";
        IMqttClient publisher = new MqttClient("tcp://localhost:1883", publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(0);
        publisher.connect(options);

        if (!publisher.isConnected()) {
            return;
        }

        publisher.subscribe(topicName, (topic, msg1) -> {
            byte[] payload = msg1.getPayload();
            String s = new String(payload);
            Gson gson = new Gson();
            log.info("Received data: " + s);
            log.info("Starting irrigation");
            //irrigate
            try {
                irrigationService.irrigate();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Finishing irrigation.");
        });

    }
}
