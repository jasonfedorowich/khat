package com.chat.khat.kafka;

import lombok.Getter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KafkaProperties {
    @Value(value = "${kafka.khat.host}")
    private final String host = "localhost";

    @Value(value = "${kafka..khat.port}")
    private final Integer port = 9092;

    @Value(value = "${kafka.khat.topic}")
    private String topic;

    @Value(value = "${kafka.khat.partitions}")
    private Integer partitions = 2;

    @Value(value = "${kafka.khat.replication_factor}")
    private Short replicationFactor = 1;

    @Value(value = "${kafka.khat.group_id}")
    private String groupId;

    @Bean
    NewTopic makeTopic(){
        return new NewTopic(topic, partitions, replicationFactor);
    }
}
