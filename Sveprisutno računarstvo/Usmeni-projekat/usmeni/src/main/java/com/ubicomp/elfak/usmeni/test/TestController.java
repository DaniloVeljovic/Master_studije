package com.ubicomp.elfak.usmeni.test;

import com.pi4j.io.gpio.*;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.*;
import org.influxdb.impl.InfluxDBResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class TestController {

    public static Logger log = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/test")
    public ResponseEntity test() throws InterruptedException {
        log.info("<--Pi4J--> GPIO Control Example ... started.");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.HIGH);

        // set shutdown state for this pin
        pin.setShutdownOptions(true, PinState.LOW);

        System.out.println("--> GPIO state should be: ON");

        Thread.sleep(5000);

        // turn off gpio pin #01
        pin.low();
        System.out.println("--> GPIO state should be: OFF");

        Thread.sleep(5000);

        // toggle the current state of gpio pin #01 (should turn on)
        pin.toggle();
        System.out.println("--> GPIO state should be: ON");

        Thread.sleep(5000);

        // toggle the current state of gpio pin #01  (should turn off)
        pin.toggle();
        System.out.println("--> GPIO state should be: OFF");

        Thread.sleep(5000);

        // turn on gpio pin #01 for 1 second and then off
        System.out.println("--> GPIO state should be: ON for only 1 second");
        pin.pulse(1000, true); // set second argument to 'true' use a blocking call

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();

        System.out.println("Exiting ControlGpioExample");

        return ResponseEntity.ok().build();
    }

    @GetMapping("/db")
    public void connectionTest() {
        InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "admin", "admin");

        Pong response = influxDB.ping();
        if (response.getVersion().equalsIgnoreCase("unknown")) {
            log.error("Error pinging server.");
            return;
        }

        influxDB.createDatabase("baeldung");
        influxDB.createRetentionPolicy(
                "defaultPolicy", "baeldung", "30d", 1, true);

        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);


        Point point = Point.measurement("memory")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("name", "server1")
                .addField("free", 4743656L)
                .addField("used", 1015096L)
                .addField("buffer", 1010467L)
                .build();

        //influxDB.write(point);

        BatchPoints batchPoints = BatchPoints
                .database("baeldung")
                .retentionPolicy("defaultPolicy")
                .build();

        Point point1 = Point.measurement("memory")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("name", "server1")
                .addField("free", 4743656L)
                .addField("used", 1015096L)
                .addField("buffer", 1010467L)
                .build();

        Point point2 = Point.measurement("memory")
                .time(System.currentTimeMillis() - 100, TimeUnit.MILLISECONDS)
                .addField("name", "server1")
                .addField("free", 4743696L)
                .addField("used", 1016096L)
                .addField("buffer", 1008467L)
                .build();

        batchPoints.point(point1);
        batchPoints.point(point2);
        influxDB.write(batchPoints);

        influxDB.close();

    }

    @GetMapping("/query")
    public ResponseEntity<List<MemoryPoint>> testing() {

        InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "admin", "admin");

        QueryResult queryResult = influxDB
                .query(new Query("Select * from memory", "baeldung"));

        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<MemoryPoint> memoryPointList = resultMapper
                .toPOJO(queryResult, MemoryPoint.class);

        return ResponseEntity.ok(memoryPointList);

    }

}
