package ru.otus.rabbitmq.hw.consumer_median.service;

import com.rabbitmq.stream.Consumer;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"unused", "LoggingSimilarMessage"})
public class StreamMedianConsumerService implements CommandLineRunner {

    private final Environment environment;
    private final AtomicInteger receivedCount = new AtomicInteger(0);
    private final PriorityQueue<Integer> maxHeap = new PriorityQueue<>((a, b) -> b - a);
    private final PriorityQueue<Integer> minHeap = new PriorityQueue<>();

    @Value("${rabbitmq.stream.stream-name}")
    private String streamName;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting consumer-median for stream: {}", streamName);
        try (Consumer consumer = environment.consumerBuilder()
                .stream(streamName)
                .offset(OffsetSpecification.first())
                .name("consumer-median")
                .messageHandler((context, message) -> {
                    byte[] data = message.getBodyAsBinary();
                    String numberStr = new String(data);
                    try {
                        int number = Integer.parseInt(numberStr);
                        int count = receivedCount.incrementAndGet();
                        addNumber(number);
                        double median = findMedian();
                        DecimalFormat df = new DecimalFormat("#.0");
                        log.info("📥 [{}] Received: {}, Current Median: {}", count, number, df.format(median));
                    } catch (NumberFormatException e) {
                        log.error("Invalid number format: {}", numberStr);
                    } catch (Exception e) {
                        log.error("Error calculating median: {}", e.getMessage());
                    }
                })
                .build()) {
            log.info("Median consumer started successfully");
            Thread.currentThread().join();
        }
    }

    private void addNumber(int num) {
        try {
            maxHeap.offer(num);
            Integer maxFromMaxHeap = maxHeap.poll();
            if (maxFromMaxHeap != null) {
                minHeap.offer(maxFromMaxHeap);
            }
            if (maxHeap.size() < minHeap.size()) {
                Integer minFromMinHeap = minHeap.poll();
                if (minFromMinHeap != null) {
                    maxHeap.offer(minFromMinHeap);
                }
            }
        } catch (Exception e) {
            log.error("Error adding number to heaps: {}", e.getMessage());
        }
    }

    private double findMedian() {
        try {
            if (maxHeap.isEmpty() && minHeap.isEmpty()) {
                return 0.0;
            }
            Integer maxHeapTop = maxHeap.peek();
            if (maxHeap.size() == minHeap.size()) {
                Integer minHeapTop = minHeap.peek();
                if (maxHeapTop != null && minHeapTop != null) {
                    return ((double) maxHeapTop + (double) minHeapTop) / 2.0;
                } else if (maxHeapTop != null) {
                    return (double) maxHeapTop;
                } else if (minHeapTop != null) {
                    return (double) minHeapTop;
                } else {
                    return 0.0;
                }
            } else {
                return maxHeapTop != null ? (double) maxHeapTop : 0.0;
            }
        } catch (Exception e) {
            log.error("Error calculating median: {}", e.getMessage());
            return 0.0;
        }
    }
}