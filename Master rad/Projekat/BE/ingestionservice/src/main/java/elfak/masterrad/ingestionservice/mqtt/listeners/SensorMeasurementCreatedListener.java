package elfak.masterrad.ingestionservice.mqtt.listeners;

import com.google.gson.Gson;
import elfak.masterrad.ingestionservice.models.dto.SensorMeasurementDTO;
import elfak.masterrad.ingestionservice.services.SensorService;
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
public class SensorMeasurementCreatedListener {

    private Logger log = LoggerFactory.getLogger(SensorMeasurementCreatedListener.class);

    @Autowired
    private SensorService sensorService;

    @EventListener(ApplicationReadyEvent.class)
    private void initializeSubscriber() throws MqttException {

        String publisherId = "com.ubicomp.elfak.sensorMeasurementListener";
        String topicName = "devices/sensors";
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
            SensorMeasurementDTO sensorMeasurementDTO = gson.fromJson(s, SensorMeasurementDTO.class);
            log.info("Received measurement: " + sensorMeasurementDTO);
            sensorService.storeMeasurement(sensorMeasurementDTO);
        });

    }
}
