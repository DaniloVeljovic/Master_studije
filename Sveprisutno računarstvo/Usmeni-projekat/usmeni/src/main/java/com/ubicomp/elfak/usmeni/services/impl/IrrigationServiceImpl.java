package com.ubicomp.elfak.usmeni.services.impl;

import com.pi4j.io.gpio.*;
import com.ubicomp.elfak.usmeni.models.Irrigation;
import com.ubicomp.elfak.usmeni.services.IrrigationService;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class IrrigationServiceImpl implements IrrigationService {

    public volatile Boolean isCurrentlyIrrigating = false;

    private static Logger log = LoggerFactory.getLogger(IrrigationServiceImpl.class);

    @Value("${influx.host}")
    private String host;

    @Value("${influx.username}")
    private String username;

    @Value("${influx.password}")
    private String password;

    @Override
    public Irrigation tryToIrrigate(Long userId) throws InterruptedException {
        if(isCurrentlyIrrigating) {
            return null;
        }
        irrigate();

        Irrigation irrigation = new Irrigation();
        irrigation.setLength(5000L);
        irrigation.setTime(Instant.now());
        InfluxDB influxDB = InfluxDBFactory.connect(host, username, password);

        BatchPoints batchPoints = BatchPoints
                .database("irrigation")
                .retentionPolicy("defaultPolicy")
                .build();

        Point point1 = Point.measurement("irrigation")
                .time(irrigation.getTime().toEpochMilli(), TimeUnit.MILLISECONDS)
                .addField("length", irrigation.getLength())
                .build();

        batchPoints.point(point1);
        influxDB.write(batchPoints);

        influxDB.close();

        return irrigation;
    }

    @Override
    public void irrigate() throws InterruptedException {
        synchronized (isCurrentlyIrrigating) {
            //activatePinsForIrrigation(userId);
            if(!isCurrentlyIrrigating) {
                isCurrentlyIrrigating = true;

                activatePinsForIrrigation();

                isCurrentlyIrrigating = false;
            }
        }
    }

    private void activatePinsForIrrigation() throws InterruptedException {
        log.info("<--Pi4J--> GPIO Control Example ... started.");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.HIGH);
        //final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(preferences.getSignalPin()), "MyLED", PinState.HIGH);

        // set shutdown state for this pin
        pin.setShutdownOptions(true, PinState.LOW);

        log.info("--> GPIO state should be: ON");

        Thread.sleep(5000L);

        // turn off gpio pin #01
        pin.low();
        log.info("--> GPIO state should be: OFF");

        //Thread.sleep(5000);

        // toggle the current state of gpio pin #01 (should turn on)
        //pin.toggle();
       //System.out.println("--> GPIO state should be: ON");

        //Thread.sleep(5000);

        // toggle the current state of gpio pin #01  (should turn off)
        //pin.toggle();
        //System.out.println("--> GPIO state should be: OFF");

        //Thread.sleep(5000);

        // turn on gpio pin #01 for 1 second and then off
        //System.out.println("--> GPIO state should be: ON for only 1 second");
        //pin.pulse(1000, true); // set second argument to 'true' use a blocking call

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();

        log.info("Exiting ControlGpioExample");
    }
}
