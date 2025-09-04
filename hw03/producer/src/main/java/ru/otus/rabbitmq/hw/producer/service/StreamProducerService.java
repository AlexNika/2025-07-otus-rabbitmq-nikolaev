package ru.otus.rabbitmq.hw.producer.service;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Producer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class StreamProducerService implements CommandLineRunner {

    private final Environment environment;

    @Value("${rabbitmq.stream.stream-name}")
    private String streamName;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting StreamProducerService...");
        try {
            log.info("Attempting to create stream: {}", streamName);
            environment.streamCreator().stream(streamName).create();
            log.info("Stream '{}' created", streamName);
        } catch (Exception e) {
            log.warn("Stream '{}' already exists or error: {}", streamName, e.getMessage());
        }

        log.info("Creating producer for stream: {}", streamName);

        try (Producer producer = environment.producerBuilder()
                     .stream(streamName)
                     .name("producer-1")
                     .build()) {
            AtomicInteger sentCount = new AtomicInteger(0);
            AtomicInteger confirmedCount = new AtomicInteger(0);
            CountDownLatch confirmationLatch = new CountDownLatch(1001);
            log.info("Starting to send numbers from 0 to 1000...");
            IntStream.rangeClosed(0, 1000).forEach(i -> {
                String message = String.valueOf(i);
                log.debug("Sending message: {}", message);
                producer.send(
                        producer.messageBuilder().addData(message.getBytes()).build(),
                        confirmationStatus -> {
                            sentCount.incrementAndGet();
                            if (confirmationStatus.isConfirmed()) {
                                confirmedCount.incrementAndGet();
                                confirmationLatch.countDown();
                                log.info("✅ [{}] Sent: {}", confirmedCount.get(), message);
                            } else {
                                log.error("❌ Failed to send: {}", message);
                                confirmationLatch.countDown();
                            }
                        }
                );
            });
            log.info("Waiting for all confirmations...");
            confirmationLatch.await();
            log.info("All messages sent! Total sent: {}, Confirmed: {}", sentCount.get(), confirmedCount.get());
        } catch (Exception e) {
            log.error("Error in producer: ", e);
            throw e;
        }
    }
}
