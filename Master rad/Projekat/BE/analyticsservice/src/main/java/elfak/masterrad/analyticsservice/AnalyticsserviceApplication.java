package elfak.masterrad.analyticsservice;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class AnalyticsserviceApplication {

	Logger logger = LoggerFactory.getLogger(AnalyticsserviceApplication.class);

	@Value("${influx.host}")
	private String host;

	@Value("${influx.username}")
	private String username;

	@Value("${influx.password}")
	private String password;

	public static void main(String[] args) {
		SpringApplication.run(AnalyticsserviceApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	private void test() {
		logger.info("Started creating dbs.");

		InfluxDB influxDB = InfluxDBFactory.connect(host, username, password);

		Pong response = influxDB.ping();
		if (response.getVersion().equalsIgnoreCase("unknown")) {
			logger.error("Error pinging server.");
			return;
		}

		if(!influxDB.databaseExists("sensorMeasurement")) {
			influxDB.createDatabase("sensorMeasurement");
			influxDB.createRetentionPolicy(
					"defaultPolicy", "sensorMeasurement", "30d", 1, true);
		}

		if(!influxDB.databaseExists("analyzedMeasurement")) {
			influxDB.createDatabase("analyzedMeasurement");
			influxDB.createRetentionPolicy(
					"defaultPolicy", "analyzedMeasurement", "30d", 1, true);
		}
		influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);

		assert influxDB.databaseExists("sensorMeasurement");
		assert influxDB.databaseExists("analyzedMeasurement");

		logger.info("Finished creating dbs.");

		influxDB.close();
	}

}
