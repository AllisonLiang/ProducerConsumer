package com.allison.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

class AtomicIntegerRingBufferTest {
    @Test
    void singleProducerSingleConsumer() {
        CASRingBuffer<Integer> ringBuffer = new CASRingBuffer<>(8);
        createProducerConsumer(1, 1, ringBuffer);
    }

    @Test
    void moreProducerThanConsumer() {
        CASRingBuffer<Integer> ringBuffer = new CASRingBuffer<>(8);
        createProducerConsumer(8, 2, ringBuffer);
    }

    @Test
    void lessProducerThanConsumer() {
        CASRingBuffer<Integer> ringBuffer = new CASRingBuffer<>(8);
        createProducerConsumer(2, 8, ringBuffer);
    }

    private void createProducerConsumer(int producerCount, int consumerCount, RingBuffer<Integer> ringBuffer) {
        ExecutorService executor = Executors.newFixedThreadPool(producerCount + consumerCount);

        for (int i = 0; i < producerCount; i++) {
            executor.submit(new IntegerProducer(i, ringBuffer));
        }

        for (int i = 0; i < consumerCount; i++) {
            executor.submit(new IntegerConsumer(i, ringBuffer));
        }
    }
}