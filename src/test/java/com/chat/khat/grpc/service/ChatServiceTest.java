package com.chat.khat.grpc.service;

import com.chat.khat.kafka.KafkaObservers;
import com.chat.khat.kafka.KafkaProperties;
import com.proto.service.Message;
import com.proto.service.MessageRequest;
import com.proto.service.MessageResponse;
import com.proto.service.ReadMessageRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.testing.StreamRecorder;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.*;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    KafkaTemplate<String, byte[]> kafkaTemplate;

    @Mock
    KafkaProperties kafkaProperties;

    @Mock
    ListenableFuture<SendResult<String, byte[]>> listenableFuture;

    @Mock
    SendResult<String, byte[]> sendResult;

    @Mock
    ProducerRecord<String, byte[]> producerRecord;

    @Mock
    KafkaObservers kafkaObservers;

    private ChatService chatService;

    @BeforeEach
    void init(){
        chatService = new ChatService(kafkaTemplate,
                kafkaProperties,
                kafkaObservers);
    }

    @Test
    void when_sendMessage_success_thenReturns() throws Exception {
        var timestamp = System.currentTimeMillis();
        when(sendResult.getProducerRecord()).thenReturn(producerRecord);
        when(producerRecord.timestamp()).thenReturn(timestamp);

        when(kafkaProperties.getTopic()).thenReturn("chat-topic");

        SettableListenableFuture<SendResult<String, byte[]>> future = new SettableListenableFuture<>();
        future.set(sendResult);
        when(kafkaTemplate.send(anyString(), any(), any(), any(), any())).thenReturn(future);

        StreamRecorder<MessageResponse> responseObserver = StreamRecorder.create();
        MessageRequest request = MessageRequest.newBuilder()
                        .setMessage(Message.newBuilder()
                                .setMessage("hello-world")
                                .setUserName("hey").build()).build();

        chatService.sendMessage(request, responseObserver);
        responseObserver.awaitCompletion(1000, TimeUnit.MILLISECONDS);
        var result = responseObserver.getValues();
        assertEquals(1, result.size());
        var response = result.get(0);
        assertEquals("hello-world", response.getMessage().getMessage());
        assertEquals(timestamp, response.getTimestamp());
        assertEquals("hey", response.getMessage().getUserName());

    }

    @Test
    void when_sendMessage_fails_thenGetError() throws Exception {

        when(kafkaProperties.getTopic()).thenReturn("chat-topic");

        SettableListenableFuture<SendResult<String, byte[]>> future = new SettableListenableFuture<>();
        future.setException(new RuntimeException());
        when(kafkaTemplate.send(anyString(), any(), any(), any(), any())).thenReturn(future);

        StreamRecorder<MessageResponse> responseObserver = StreamRecorder.create();
        MessageRequest request = MessageRequest.newBuilder()
                .setMessage(Message.newBuilder()
                        .setMessage("hello-world")
                        .setUserName("hey").build()).build();

        chatService.sendMessage(request, responseObserver);
        responseObserver.awaitCompletion(1000, TimeUnit.MILLISECONDS);
        assertNotNull(responseObserver.getError());
        assertTrue(responseObserver.getError() instanceof StatusRuntimeException);
        var error = ((StatusRuntimeException) responseObserver.getError()).getStatus();
        assertEquals(error.getCode(), Status.Code.UNAVAILABLE);

    }

    @Test
    void when_readMessage_add_toObservers() {
        StreamRecorder<MessageResponse> responseObserver = StreamRecorder.create();
        ReadMessageRequest request = ReadMessageRequest.getDefaultInstance();

        chatService.readMessages(request, responseObserver);
        verify(kafkaObservers, times(1)).addObserver(any());
    }
}