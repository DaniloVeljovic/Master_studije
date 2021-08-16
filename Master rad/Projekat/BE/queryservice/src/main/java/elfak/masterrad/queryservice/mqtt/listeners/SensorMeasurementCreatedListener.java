package elfak.masterrad.queryservice.mqtt.listeners;

import com.google.gson.Gson;
import elfak.masterrad.queryservice.models.dto.SensorMeasurementDTO;
import elfak.masterrad.queryservice.services.SensorService;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SensorMeasurementCreatedListener {

    private Logger log = LoggerFactory.getLogger(SensorMeasurementCreatedListener.class);

    @Autowired
    private SensorService sensorService;

    @Value("${mosquitto.host}")
    private String host;

    @Value("${mosquitto.port}")
    private String port;

    @Value("${mosquitto.topic}")
    private String topic;

    @EventListener(ApplicationReadyEvent.class)
    private void initializeSubscriber() throws MqttException {

        String publisherId = "com.ubicomp.elfak.ingestion.sensor.measurement";
        String connectionString = buildConnectionString(host, port);
        IMqttClient publisher = new MqttClient(connectionString, publisherId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(0);
        publisher.connect(options);

        if (!publisher.isConnected()) {
            return;
        }

        publisher.subscribe(topic, (topic, msg1) -> {
            byte[] payload = msg1.getPayload();
            String s = new String(payload);
            Gson gson = new Gson();
            SensorMeasurementDTO sensorMeasurementDTO = gson.fromJson(s, SensorMeasurementDTO.class);
            log.info("Received measurement: " + sensorMeasurementDTO);
            sensorService.storeMeasurement(sensorMeasurementDTO);
        });

    }

    private String buildConnectionString(String host, String port) {
        return host +
                ":" +
                port;
    }
}
