package ru.otus.rabbitmq.hw.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;
import ru.otus.rabbitmq.hw.dto.MessageDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MqttSubscriberService {

    private final List<MessageDto> receivedMessages = Collections.synchronizedList(new ArrayList<>());

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void messageArrived(String message) {
        log.info("MessageDto arrived: {}", message);

        MessageDto messageDto = new MessageDto(UUID.randomUUID().toString(), message);

        receivedMessages.addFirst(messageDto);

        if (receivedMessages.size() > 100) {
            synchronized (receivedMessages) {
                if (receivedMessages.size() > 100) {
                    receivedMessages.subList(100, receivedMessages.size()).clear();
                }
            }
        }
    }

    public List<MessageDto> getReceivedMessages() {
        synchronized (receivedMessages) {
            return new ArrayList<>(receivedMessages);
        }
    }

    public void clearMessages() {
        synchronized (receivedMessages) {
            receivedMessages.clear();
        }
    }
}
