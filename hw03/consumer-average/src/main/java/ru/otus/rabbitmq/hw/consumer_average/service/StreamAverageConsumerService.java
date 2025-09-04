package ru.otus.rabbitmq.hw.consumer_average.service;

import com.rabbitmq.stream.Consumer;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class StreamAverageConsumerService implements CommandLineRunner {

    private final Environment environment;
    private final AtomicInteger receivedCount = new AtomicInteger();
    private final AtomicLong sum = new AtomicLong();
    private volatile double currentAverage = 0.0;

    @Value("${rabbitmq.stream.stream-name}")
    private String streamName;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting average consumer for stream: {}", streamName);
        try (Consumer consumer = environment.consumerBuilder()
                .stream(streamName)
                .offset(OffsetSpecification.first())
                .name("consumer-average")
                .messageHandler((context, message) -> {
                    byte[] data = message.getBodyAsBinary();
                    String numberStr = new String(data);
                    try {
                        int number = Integer.parseInt(numberStr);
                        int count = receivedCount.incrementAndGet();
                        long newSum = sum.addAndGet(number);
                        currentAverage = (double) newSum / count;
                        DecimalFormat df = new DecimalFormat("#.##");
                        log.info("📥 [{}] Received: {}, Running Sum: {}, Current Average: {}",
                                count, number, newSum, df.format(currentAverage));
                    } catch (NumberFormatException e) {
                        log.error("Invalid number format: {}", numberStr);
                    } catch (Exception e) {
                        log.error("Error processing message: {}", numberStr, e);
                    }
                })
                .build()) {
            log.info("Average consumer started successfully");
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("Error in average consumer: ", e);
            throw e;
        }
    }
}