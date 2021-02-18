package com.example.write.kafka;

import com.example.write.entity.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, Parameters> kafkaTemplate;

    @Value(value = "${kafka.topic}")
    private String topicName;

    public void sendMessage(Parameters message) {

        ListenableFuture<SendResult<String, Parameters>> future =
                kafkaTemplate.send(topicName, message);

        future.addCallback(new ListenableFutureCallback<SendResult<String, Parameters>>() {

            @Override
            public void onSuccess(SendResult<String, Parameters> result) {
                System.out.println("Sent message=[" + message +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("Unable to send message=["
                        + message + "] due to : " + ex.getMessage());
            }
        });
    }
}
