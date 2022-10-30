package com.chat.khat.grpc.server.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class GrpcServerProperties {

    @Value(value = "${grpc.port}")
    private int port;
}
