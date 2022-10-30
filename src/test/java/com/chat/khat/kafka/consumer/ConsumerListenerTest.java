package com.chat.khat.kafka.consumer;

import com.chat.khat.kafka.KafkaObservers;
import com.proto.service.Message;
import com.proto.service.MessageResponse;
import io.grpc.internal.testing.StreamRecorder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumerListenerTest {

    @Mock
    KafkaObservers kafkaObservers;

    @Mock
    ConsumerRecord<String, byte[]> record;

    ConsumerListener consumerListener;

    @BeforeEach
    void setUp() {
        consumerListener = new ConsumerListener(kafkaObservers);
    }

    @Test
    void when_listen_parseableMessage_thenReturns() {
        StreamRecorder<MessageResponse> responseObserver = StreamRecorder.create();
        when(kafkaObservers.getObservers()).thenReturn(List.of(responseObserver));
        when(record.timestamp()).thenReturn(System.currentTimeMillis());
        Message message = Message.newBuilder()
                        .setMessage("hello")
                        .setUserName("me").build();

        when(record.value()).thenReturn(message.toByteArray());

        assertDoesNotThrow(()->{
            consumerListener.listen(record);
        });

    }

    @Test
    void when_listen_nonparseable_message_thenReturns(){
        StreamRecorder<MessageResponse> responseObserver = StreamRecorder.create();
        when(kafkaObservers.getObservers()).thenReturn(List.of(responseObserver));
        when(record.key()).thenReturn("jello");

        when(record.value()).thenReturn(new byte[]{1, 23});

        assertDoesNotThrow(()->{
            consumerListener.listen(record);
        });

    }
}