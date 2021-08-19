package com.ubicomp.elfak.sensorImitation;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Component
public class SensorMeasurementCreatedPublisher {

    Logger logger = LoggerFactory.getLogger(SensorMeasurementCreatedPublisher.class);

    @EventListener(ApplicationReadyEvent.class)
    public void testConnection() throws Exception {
        String publisherId = "sensorImitation";
        IMqttClient publisher = new MqttClient("tcp://192.168.43.79:1883",publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(0);
        publisher.connect(options);

        if (!publisher.isConnected()) {
            return;
        }

        while(true) {
            try {
                File myObj = new File("filename.txt");
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    String[] dataArray = data.split(",");
                    MqttMessage msg = createMqttMessage(dataArray);
                    publishMessage(publisher, msg);
                    Thread.sleep(1000L);
                    System.out.println(data);
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
        publisher.publish("devices/sensors",msg);
    }

    private MqttMessage createMqttMessage(String[] dataArray) {
        SensorMeasurementDTO sensorMeasurementDTO = new SensorMeasurementDTO(dataArray[0], dataArray[1], dataArray[2], dataArray[3]);
        String format = new Gson().toJson(sensorMeasurementDTO);
        logger.info("[Producer] - " + format);
        byte[] payload =
                format.getBytes();
        return new MqttMessage(payload);
    }
}
