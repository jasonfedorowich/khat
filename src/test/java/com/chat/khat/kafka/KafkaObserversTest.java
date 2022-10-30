package com.chat.khat.kafka;

import com.proto.service.MessageResponse;
import io.grpc.internal.testing.StreamRecorder;
import org.checkerframework.checker.units.qual.K;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KafkaObserversTest {

    private KafkaObservers kafkaObservers;

    @BeforeEach
    void init(){
        kafkaObservers = new KafkaObservers();
    }

    @Test
    void when_addObserver_success_thenReturns() {
        StreamRecorder<MessageResponse> responseObserver = StreamRecorder.create();
        kafkaObservers.addObserver(responseObserver);
        assertEquals(1, kafkaObservers.getObservers().size());
    }

}