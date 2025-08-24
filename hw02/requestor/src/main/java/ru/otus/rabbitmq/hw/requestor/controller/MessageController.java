package ru.otus.rabbitmq.hw.requestor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.rabbitmq.hw.requestor.service.RequestorService;
import ru.otus.rabbitmq.hw.shared.dto.MessageDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/message")
public class MessageController {

    private final RequestorService requestorService;

    @PostMapping
    public void createMessage(@RequestBody MessageDto message) {
        requestorService.sendMessage(message);
    }
}
