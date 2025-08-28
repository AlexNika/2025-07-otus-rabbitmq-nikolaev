package ru.otus.rabbitmq.hw.consumer_minmax.service;

import com.rabbitmq.stream.Consumer;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class StreamMinMaxConsumerService implements CommandLineRunner {

    private final Environment environment;
    private final AtomicInteger receivedCount = new AtomicInteger(0);

    private final AtomicReference<Integer> currentMin = new AtomicReference<>(null);
    private final AtomicReference<Integer> currentMax = new AtomicReference<>(null);

    @Value("${rabbitmq.stream.stream-name}")
    private String streamName;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting min-max consumer for stream: {}", streamName);
        try (Consumer consumer = environment.consumerBuilder()
                .stream(streamName)
                .offset(OffsetSpecification.first())
                .name("consumer-minmax")
                .messageHandler((context, message) -> {
                    byte[] data = message.getBodyAsBinary();
                    String numberStr = new String(data);
                    try {
                        int number = Integer.parseInt(numberStr);
                        int count = receivedCount.incrementAndGet();
                        updateMinMax(number);
                        Integer min = currentMin.get();
                        Integer max = currentMax.get();
                        log.info("📥 [{}] Received: {}, Current Min: {}, Current Max: {}",
                                count, number, min, max);
                    } catch (NumberFormatException e) {
                        log.error("Invalid number format: {}", numberStr);
                    } catch (Exception e) {
                        log.error("Error processing message: {}", numberStr, e);
                    }
                })
                .build()) {
            log.info("Min-Max consumer started successfully");
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("Error in min-max consumer: ", e);
            throw e;
        }
    }

    private void updateMinMax(int newValue) {
        currentMin.getAndUpdate(current -> {
            if (current == null || newValue < current) {
                return newValue;
            }
            return current;
        });
        currentMax.getAndUpdate(current -> {
            if (current == null || newValue > current) {
                return newValue;
            }
            return current;
        });
    }
}