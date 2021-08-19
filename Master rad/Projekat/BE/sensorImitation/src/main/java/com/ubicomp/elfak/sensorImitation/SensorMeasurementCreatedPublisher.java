package com.ubicomp.elfak.sensorImitation;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.UUID;

@Component
public class SensorMeasurementCreatedPublisher {

    private Logger logger = LoggerFactory.getLogger(SensorMeasurementCreatedPublisher.class);

    private final String publisherId = "sensorImitation[" + UUID.randomUUID().toString() + "]";

    @Value("${mosquitto.host}")
    private String mosquittoHost;

    @Value("${mosquitto.port}")
    private String mosquittoPort;

    @Value("${mosquitto.topic}")
    private String mosquittoTopic;

    @Value("${data.path}")
    private String dataPath;

    @EventListener(ApplicationReadyEvent.class)
    public void publish() throws Exception {
        IMqttClient publisher = new MqttClient(returnConnectionString(mosquittoHost, mosquittoPort), publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(0);
        publisher.connect(options);

        if (!publisher.isConnected()) {
            return;
        }

        while (true) {
            try {
                File myObj = new File(dataPath);
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    String[] dataArray = data.split(",");
                    MqttMessage msg = createMqttMessage(dataArray);
                    publishMessage(publisher, msg);
                    Thread.sleep(5000L);
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }

    private void publishMessage(IMqttClient publisher, MqttMessage msg) throws InterruptedException, MqttException {
        Thread.sleep(1000);
        msg.setQos(0);
        msg.setRetained(true);
        publisher.publish(mosquittoTopic, msg);
    }

    private MqttMessage createMqttMessage(String[] dataArray) {
        SensorMeasurementDTO sensorMeasurementDTO = new SensorMeasurementDTO(dataArray[0], dataArray[1], dataArray[2], dataArray[3]);
        sensorMeasurementDTO.setRequestId(UUID.randomUUID().toString());
        String format = new Gson().toJson(sensorMeasurementDTO);
        logger.info("Sending data - " + format);
        byte[] payload =
                format.getBytes();
        return new MqttMessage(payload);
    }

    private String returnConnectionString(String mosquittoHost, String mosquittoPort) {
        return mosquittoHost + ":" + mosquittoPort;
    }
}
