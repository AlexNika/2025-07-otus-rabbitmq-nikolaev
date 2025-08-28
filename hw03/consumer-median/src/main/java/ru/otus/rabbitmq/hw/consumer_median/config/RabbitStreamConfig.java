package ru.otus.rabbitmq.hw.consumer_median.config;

import com.rabbitmq.stream.Environment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitStreamConfig {

    @Value("${rabbitmq.stream.host}")
    private String host;

    @Value("${rabbitmq.stream.port}")
    private int port;

    @Value("${rabbitmq.stream.username}")
    private String username;

    @Value("${rabbitmq.stream.password}")
    private String password;

    @Bean
    public Environment rabbitStreamEnvironment() {
        log.info("Creating RabbitMQ Stream Environment: {}:{}, user: {}", host, port, username);
        try {
            Environment env = Environment.builder()
                    .host(host)
                    .port(port)
                    .username(username)
                    .password(password)
                    .build();
            log.info("Environment created successfully");
            return env;
        } catch (Exception e) {
            log.error("Failed to create Environment: ", e);
            throw e;
        }
    }
}
