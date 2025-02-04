package com.allison.service;

import com.allison.utils.SystemOutLogger;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerRingBuffer<T> implements RingBuffer<T> {
    private static final SystemOutLogger LOGGER = new SystemOutLogger();

    private final Object[] buffer;
    private final int mask;
    private final AtomicInteger head; // producer index
    private final AtomicInteger tail; // consumer index

    public AtomicIntegerRingBuffer(int capacity) {
        int size = 1 << (32 - Integer.numberOfLeadingZeros(capacity - 1)); // size has to be power of 2
        mask = size - 1;
        buffer = new Object[size]; // all allocation in construction phase to prevent GC; pre-size the collection
        head = new AtomicInteger(0);
        tail = new AtomicInteger(0);
    }

    @Override
    public boolean offer(T value) {
        while (true) { // assume each thread is pinned to a CPU core, busy-spin hot threads to monopolize CPU
            int producerIndex = head.get();
            int nextProducerIndex = (producerIndex + 1) & mask;

            if (nextProducerIndex == tail.get()) {
                LOGGER.log("RingBuffer is full");
                return false;
            }

            if (head.compareAndSet(producerIndex, nextProducerIndex)) {
                buffer[producerIndex & mask] = value;
                return true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T poll() {
        while (true) { // assume each thread is pinned to a CPU core, busy-spin hot threads to monopolize CPU
            int consumerIndex = tail.get();

            if (consumerIndex == head.get()) {
                LOGGER.log("RingBuffer is empty");
                return null;
            }

            T value = (T) buffer[consumerIndex & mask];
            int nextConsumerIndex = (consumerIndex + 1) & mask;

            if (value != null && tail.compareAndSet(consumerIndex, nextConsumerIndex)) {
                buffer[consumerIndex & mask] = null;
                return value;
            }
        }
    }
}
