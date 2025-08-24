package ru.otus.rabbitmq.hw.requestor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.otus.rabbitmq.hw.shared.dto.MessageDto;

import static ru.otus.rabbitmq.hw.requestor.config.RabbitMqConfig.EXCHANGE_NAME;
import static ru.otus.rabbitmq.hw.requestor.config.RabbitMqConfig.ROUTING_KEY_HW02_QUEUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestorService {

    private final RabbitTemplate template;

    public void sendMessage(MessageDto message) {
        MessageDto response = (MessageDto) template.convertSendAndReceive(EXCHANGE_NAME, ROUTING_KEY_HW02_QUEUE,
                message);
        log.info("1. -> RequestorService - sent message: {}", message);
        log.info("3. -> RequestorService - response received: {}", response);
    }
}
