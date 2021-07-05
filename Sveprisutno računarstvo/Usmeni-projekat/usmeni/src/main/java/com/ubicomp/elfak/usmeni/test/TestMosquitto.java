package com.ubicomp.elfak.usmeni.test;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class TestMosquitto {

    private static final Logger log = LoggerFactory.getLogger(TestMosquitto.class);
    Random rnd = new Random();

    public void testConnection() throws Exception {
        new Thread(() -> {
            try {
                initializeSubscriber("device/messages", "com.gonzalo123.esp14");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                initializeSubscriber1("device/action", "com.gonzalo123.esp19");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }).start();

        String publisherId = "com.gonzalo123.esp32";
        IMqttClient publisher = new MqttClient("tcp://localhost:1883",publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(0);
        publisher.connect(options);

        if ( !publisher.isConnected()) {
            return;
        }
        while(true) {
            Thread.sleep(1000);
            MqttMessage msg = readEngineTemp();
            msg.setQos(0);
            msg.setRetained(true);
            publisher.publish("devices/messages",msg);
        }



    }

    private void initializeSubscriber(String topicName, String publisherId) throws InterruptedException, MqttException {
        //String publisherId = "com.gonzalo123.esp87";
        IMqttClient publisher = new MqttClient("tcp://localhost:1883",publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(0);
        publisher.connect(options);

        if ( !publisher.isConnected()) {
            return;
        }

        //CountDownLatch receivedSignal = new CountDownLatch(10);
        publisher.subscribe(topicName, (topic, msg1) -> {

            byte[] payload = msg1.getPayload();
            String s = new String(payload);
            log.info(s);
            // ... payload handling omitted
            //receivedSignal.countDown();
        });
        //receivedSignal.await(1, TimeUnit.MINUTES);
    }

    private void initializeSubscriber1(String topicName, String publisherId) throws InterruptedException, MqttException {
        //String publisherId = "com.gonzalo123.esp87";
        IMqttClient publisher = new MqttClient("tcp://localhost:1883",publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(0);
        publisher.connect(options);

        if ( !publisher.isConnected()) {
            return;
        }

        //CountDownLatch receivedSignal = new CountDownLatch(10);
        publisher.subscribe(topicName, (topic, msg1) -> {

            byte[] payload = msg1.getPayload();
            String s = new String(payload);
            log.info(s);
            // ... payload handling omitted
            //receivedSignal.countDown();
        });
        //receivedSignal.await(1, TimeUnit.MINUTES);
    }

    private MqttMessage readEngineTemp() {
        double temp =  80 + rnd.nextDouble() * 20.0;
        byte[] payload = String.format("{\"temperature\" : %04.2f , \"humidity\" : 20}",temp)
                .getBytes();
        return new MqttMessage(payload);
    }
}
