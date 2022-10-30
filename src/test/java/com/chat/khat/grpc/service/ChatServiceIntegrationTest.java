package com.chat.khat.grpc.service;

import com.chat.khat.KhatApplication;
import com.proto.service.*;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {KhatApplication.class})
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class ChatServiceIntegrationTest {

    private ChatServiceGrpc.ChatServiceStub stub;


    @BeforeEach
    void setUp(){
        var managedChannel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        stub = ChatServiceGrpc
                .newStub(managedChannel);
    }

    @Test
    void test_when_sendMessage_thenConsume() throws InterruptedException {
        final MessageResponse[] response = {null};
        final MessageResponse[] messageReadFromReadMessages = {null};

        stub.readMessages(ReadMessageRequest.newBuilder().build(), new StreamObserver<MessageResponse>() {
            @Override
            public void onNext(MessageResponse messageResponse) {
                messageReadFromReadMessages[0] = messageResponse;
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        });
        stub.sendMessage(MessageRequest
                .newBuilder()
                .setMessage(Message.newBuilder()
                        .setUserName("jason")
                        .setMessage("hello")
                        .build())
                .build(), new StreamObserver<>() {
            @Override
            public void onNext(MessageResponse messageResponse) {
                System.out.println("Message recieved");
                response[0] = messageResponse;
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onCompleted() {

            }
        });

        CountDownLatch countDownLatch = new CountDownLatch(10);
        countDownLatch.await(10, TimeUnit.SECONDS);

        assertNotNull(response[0]);
        assertNotNull(messageReadFromReadMessages[0]);

    }
}
