package com.chat.khat.kafka.consumer;

import com.chat.khat.kafka.KafkaObservers;
import com.chat.khat.kafka.KafkaProperties;
import com.google.protobuf.InvalidProtocolBufferException;
import com.proto.service.Message;
import com.proto.service.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsumerListener {

    private final KafkaObservers kafkaObservers;

    @KafkaListener(topics = "#{'${kafka.khat.topic}'}",
                    groupId = "#{'${kafka.khat.group_id}'}")
    public void listen(ConsumerRecord<String, byte[]> record){
        log.info("Message read from topic: {}", record);
        kafkaObservers.getObservers().forEach(observer -> {
            try {
                observer.onNext(MessageResponse.newBuilder()
                        .setMessage(Message.parseFrom(record.value()))
                        .setTimestamp(record.timestamp())
                        .build());
            } catch (InvalidProtocolBufferException e) {
                log.error("Invalid record received with key: {}", record.key());
            }

        });
    }


//    @Override
//    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
//        assignments.keySet().stream()
//                .filter(partition -> kafkaProperties.getTopic().equals(partition.topic()))
//                .forEach(partition -> callback.seekToBeginning(kafkaProperties.getTopic(), partition.partition()));
//    }
}
