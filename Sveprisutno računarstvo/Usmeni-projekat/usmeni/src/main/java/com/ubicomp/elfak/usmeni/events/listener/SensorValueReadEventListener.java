package com.ubicomp.elfak.usmeni.events.listener;

import com.ubicomp.elfak.usmeni.events.SensorValueReadEvent;
import com.ubicomp.elfak.usmeni.services.IrrigationService;
import com.ubicomp.elfak.usmeni.services.impl.SensorServiceImpl;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SensorValueReadEventListener implements ApplicationListener<SensorValueReadEvent> {

    private static Logger logger = LoggerFactory.getLogger(SensorValueReadEventListener.class);

    @Autowired
    private IrrigationService irrigationService;

    private IMqttClient publisher;

    @Override
    public void onApplicationEvent(SensorValueReadEvent sensorValueReadEvent) {
        logger.info("Sensor value read listener.");

        Double gmValue = SensorServiceImpl.currentValuesReadFromSensors.get("gm");
        Double temperatureValue = SensorServiceImpl.currentValuesReadFromSensors.get("temp");
        Double lightValue = SensorServiceImpl.currentValuesReadFromSensors.get("light");

        if (!publisher.isConnected()) {
            return;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MqttMessage msg = createMqttMessage(gmValue, temperatureValue, lightValue);
        msg.setQos(0);
        msg.setRetained(true);
        try {
            publisher.publish("devices/checkMeasurement", msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializePublisher() throws Exception {
        String publisherId = "com.ubicomp.elfak.publisher";
        IMqttClient publisher = new MqttClient("tcp://localhost:1883", publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(0);
        publisher.connect(options);
        this.publisher = publisher;
    }

    private MqttMessage createMqttMessage(Double gmValue, Double tempValue, Double lightValue) {

        byte[] payload = String.format("{\"gm\" : %04.2f , \"temp\" : %04.2f, \"light\" : %04.2f}", gmValue, tempValue, lightValue)
                .getBytes();
        return new MqttMessage(payload);
    }
}
