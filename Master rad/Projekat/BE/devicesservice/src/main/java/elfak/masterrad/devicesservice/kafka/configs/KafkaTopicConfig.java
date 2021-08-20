package elfak.masterrad.devicesservice.kafka.configs;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    @Value(value = "${kafka.actuationMessageTopic}")
    private String actuationMessageTopic;

    @Value(value = "${kafka.actuationToDeviceTopic}")
    private String actuationTopic;

    @Value(value = "${kafka.actuationFinished}")
    private String actuationFinished;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic createMessageActuationTopic() {
        return new NewTopic(actuationMessageTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic createActuationDeviceTopic() {
        return new NewTopic(actuationTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic createActuationFinished() { return new NewTopic(actuationFinished, 1, (short) 1); }

}
