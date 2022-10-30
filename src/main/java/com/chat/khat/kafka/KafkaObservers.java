package com.chat.khat.kafka;

import com.proto.service.MessageResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KafkaObservers {

    private List<StreamObserver<MessageResponse>> observers = new ArrayList<>();

    public void addObserver(StreamObserver<MessageResponse> observer){
        observers.add(observer);
    }
    public List<StreamObserver<MessageResponse>> getObservers() { return observers; }
}
