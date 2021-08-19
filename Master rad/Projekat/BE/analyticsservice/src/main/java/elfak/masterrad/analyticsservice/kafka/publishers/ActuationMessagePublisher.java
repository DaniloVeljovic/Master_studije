package elfak.masterrad.analyticsservice.kafka.publishers;

import elfak.masterrad.analyticsservice.kafka.models.ActuationMessage;
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
    private KafkaTemplate<String, ActuationMessage> actuationMessageKafkaTemplate;

    @Value(value = "${kafka.actuationTopic}")
    private String actuationTopic;

    public void publishMessage(ActuationMessage message) {
        ListenableFuture<SendResult<String, ActuationMessage>> future = actuationMessageKafkaTemplate.send(actuationTopic, message);

        future.addCallback(new ListenableFutureCallback<SendResult<String, ActuationMessage>>() {
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Unable to send message=[" + message + "] due to : " + ex.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, ActuationMessage> result) {
                logger.info("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }
}
