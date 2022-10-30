package com.chat.khat.grpc.service;

import com.chat.khat.kafka.KafkaObservers;
import com.chat.khat.kafka.KafkaProperties;
import com.proto.service.ChatServiceGrpc;
import com.proto.service.MessageRequest;
import com.proto.service.MessageResponse;
import com.proto.service.ReadMessageRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService extends ChatServiceGrpc.ChatServiceImplBase {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final KafkaObservers kafkaObservers;

    @Override
    public void sendMessage(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {

        var result = kafkaTemplate.send(kafkaProperties.getTopic(),
                null,
                System.currentTimeMillis(),
                UUID.randomUUID().toString(),
                request.getMessage().toByteArray());

        result.addCallback((success)->{
            try{
                var response = MessageResponse
                        .newBuilder()
                        .setMessage(request.getMessage())
                        .setTimestamp(success.getProducerRecord().timestamp())
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }catch(Exception e){
                log.error("Error getting producer record: {}", e.getMessage());
                responseObserver.onError(e);
                responseObserver.onCompleted();
            }

        }, (failure)->{
            log.error("Error when trying to produce message: {}", failure.getMessage());
            responseObserver.onError(new StatusRuntimeException(Status.UNAVAILABLE));
            responseObserver.onCompleted();
        });

        try {
            var sendResult = result.get();
            if(sendResult == null)
                throw new ExecutionException("Send result is null", new RuntimeException());
            log.info("Record produced: {}", sendResult.getProducerRecord());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Unable to send kafka message with: {}", e.getMessage());
        }

    }

    @Override
    public void readMessages(ReadMessageRequest request, StreamObserver<MessageResponse> responseObserver) {
        //every time you sub to the messages you will only be added to the listener and the older messages will not be
        //read by you, you could possibly add a consumer here that would read the older messages then remove the consumer

        kafkaObservers.addObserver(responseObserver);
    }
}
