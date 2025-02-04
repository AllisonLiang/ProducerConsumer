package com.allison.service;

import com.allison.utils.SystemOutLogger;

public class IntegerProducer implements Runnable {
    private static final SystemOutLogger LOGGER = new SystemOutLogger();

    private final int producerId;
    private final RingBuffer<Integer> ringBuffer;

    public IntegerProducer(int producerId, RingBuffer<Integer> ringBuffer) {
        this.producerId = producerId;
        this.ringBuffer = ringBuffer;
    }

    @Override
    public void run() {
        int value = 0;
        while (true) {
            if (ringBuffer.offer(value)) {
                LOGGER.log("Produced #{0} produced value: {1}", producerId, value);
                value++;
            }
        }
    }
}
