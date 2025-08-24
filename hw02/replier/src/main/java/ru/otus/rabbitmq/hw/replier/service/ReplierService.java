package ru.otus.rabbitmq.hw.replier.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.otus.rabbitmq.hw.shared.dto.MessageDto;

import static ru.otus.rabbitmq.hw.replier.config.RabbitMqConfig.HW02_QUEUE;

@Slf4j
@Service
@RabbitListener(queues = HW02_QUEUE)
public class ReplierService {

    private static final Object PRINT_LOCK = new Object();

    @RabbitHandler
    public MessageDto handleMessages(MessageDto message) throws InterruptedException {
        synchronized (PRINT_LOCK) {
            log.info("2. -> ReplierService - receive message: {}", message);
            return new MessageDto(message.getId(), "Reply: " + message.getMessage());
        }
    }
}
