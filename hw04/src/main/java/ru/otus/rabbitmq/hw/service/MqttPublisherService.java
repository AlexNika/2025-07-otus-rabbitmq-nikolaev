package ru.otus.rabbitmq.hw.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.rabbitmq.hw.config.mqtt.MqttGateway;

@Slf4j
@Service
public class MqttPublisherService {

    private final MqttGateway mqttGateway;

    public MqttPublisherService(MqttGateway mqttGateway) {
        this.mqttGateway = mqttGateway;
    }

    public void sendMessage(String topic, String message) {
        log.info("Publishing message to topic: {}, message: {}", topic, message);
        mqttGateway.sendToMqtt(message, topic);
    }
}