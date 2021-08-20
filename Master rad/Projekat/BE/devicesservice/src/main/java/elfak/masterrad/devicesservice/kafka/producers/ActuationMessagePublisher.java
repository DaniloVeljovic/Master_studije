package elfak.masterrad.devicesservice.kafka.producers;

import elfak.masterrad.analyticsservice.kafka.models.ActuationMessage;
import elfak.masterrad.devicesservice.models.entities.Actuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class ActuationMessagePublisher {

    private static final Logger logger = LoggerFactory.getLogger(ActuationMessagePublisher.class);

    @Autowired
    private KafkaTemplate<String, Actuation> actuationKafkaTemplate;

    @Value(value = "${kafka.actuationToDeviceTopic}")
    private String actuationTopic;

    public void publishMessage(Actuation message) {
        ListenableFuture<SendResult<String, Actuation>> future = actuationKafkaTemplate.send(actuationTopic, message);

        future.addCallback(new ListenableFutureCallback<SendResult<String, Actuation>>() {
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Unable to send message=[" + message + "] due to : " + ex.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, Actuation> result) {
                logger.info("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }
}
