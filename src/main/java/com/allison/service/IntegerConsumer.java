package com.allison.service;

import com.allison.utils.SystemOutLogger;

public class IntegerConsumer implements Runnable {
    private static final SystemOutLogger LOGGER = new SystemOutLogger();

    private final int consumerId;
    private final RingBuffer<Integer> ringBuffer;

    public IntegerConsumer(int consumerId, RingBuffer<Integer> ringBuffer) {
        this.consumerId = consumerId;
        this.ringBuffer = ringBuffer;
    }

    @Override
    public void run() {
        while (true) {
            Integer value = ringBuffer.poll();
            if (value != null) {
                LOGGER.log("Consumer #{0} consumed value: {1}", consumerId, value);
            }
        }
    }
}
