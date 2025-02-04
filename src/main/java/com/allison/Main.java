package com.allison;

import com.allison.service.AtomicIntegerRingBuffer;
import com.allison.service.IntegerConsumer;
import com.allison.service.IntegerProducer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) { // see more test cases in CASRingBufferTest and UnsafeRingBufferTest
        AtomicIntegerRingBuffer<Integer> ringBuffer = new AtomicIntegerRingBuffer<>(8);

        ExecutorService executor = Executors.newFixedThreadPool(7);

        for (int i = 0; i < 2; i++) {
            executor.submit(new IntegerProducer(i, ringBuffer));
        }

        for (int i = 0; i < 5; i++) {
            executor.submit(new IntegerConsumer(i, ringBuffer));
        }
    }
}