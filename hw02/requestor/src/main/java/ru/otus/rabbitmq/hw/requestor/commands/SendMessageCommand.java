package ru.otus.rabbitmq.hw.requestor.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class SendMessageCommand {

    private final HttpClient client = HttpClient.newHttpClient();

    @ShellMethod(value = "Send number of messages from Producer", key = "send")
    public void ProducerSendMessage(Integer numberOfMessages) {
        try (client) {
            for (int i = 0; i < numberOfMessages; i++) {
                String body = "{\"id\": " +
                              i +
                              ", \"message\": \"some kind of random message " +
                              i +
                              "\"}";
                HttpRequest request = HttpRequest
                        .newBuilder()
                        .uri(URI.create("http://localhost:8080/api/v1/message"))
                        .header("Content-Type", "application/json")
                        .method("POST", HttpRequest.BodyPublishers.ofString(body))
                        .build();
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenAccept(System.out::println)
                        .join();
            }
        }
    }
}
