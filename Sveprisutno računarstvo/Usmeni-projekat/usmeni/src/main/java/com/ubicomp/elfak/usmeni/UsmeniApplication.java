package com.ubicomp.elfak.usmeni;

import com.ubicomp.elfak.usmeni.test.TestMosquitto;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sun.rmi.runtime.Log;

@SpringBootApplication
public class UsmeniApplication {

	private Logger logger = LoggerFactory.getLogger(UsmeniApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(UsmeniApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void createDbs() {
		logger.info("Started creating dbs.");

		InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "admin", "admin");

		Pong response = influxDB.ping();
		if (response.getVersion().equalsIgnoreCase("unknown")) {
			logger.error("Error pinging server.");
			return;
		}

		if(!influxDB.databaseExists("irrigation")) {
			influxDB.createDatabase("irrigation");
			influxDB.createRetentionPolicy(
					"defaultPolicy", "irrigation", "30d", 1, true);
		}

		if(!influxDB.databaseExists("sensorMeasurement")) {
			influxDB.createDatabase("sensorMeasurement");
			influxDB.createRetentionPolicy(
					"defaultPolicy", "sensorMeasurement", "30d", 1, true);
		}
		influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);

		assert influxDB.databaseExists("irrigation");
		assert influxDB.databaseExists("sensorMeasurement");

		logger.info("Finished creating dbs.");

		influxDB.close();
	}

}
