package ru.otus.rabbitmq.hw.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.otus.rabbitmq.hw.service.MqttPublisherService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MqttController {

    private final MqttPublisherService mqttPublisherService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/api/publish")
    @ResponseBody
    public ResponseEntity<Map<String, String>> publishMessage(@RequestBody Map<String, String> payload) {
        try {
            String topic = payload.get("topic");
            String message = payload.get("message");
            log.info("-> Publishing message to topic: {}, message: {}", topic, message);
            mqttPublisherService.sendMessage(topic, message);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "MessageDto published successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to publish message: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}