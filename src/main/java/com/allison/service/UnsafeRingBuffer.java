package com.allison.service;

import com.allison.utils.SystemOutLogger;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeRingBuffer<T> implements RingBuffer<T> { // referenced Disruptor
    private static final SystemOutLogger LOGGER = new SystemOutLogger();

    private static final Unsafe UNSAFE;

    // avoid false sharing. cache line size is 64
    private static class Padding {
        long p1, p2, p3, p4, p5, p6, p7;
    }

    private static class Index extends Padding {
        volatile long value;
    }

    private final int mask;
    private final Object[] buffer;

    private final Index head = new Index(); // producer index
    private final Index tail = new Index(); // consumer index

    private static final long HEAD_OFFSET;
    private static final long TAIL_OFFSET;
    private static final long ARRAY_BASE;
    private static final int ELEMENT_SHIFT;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);

            HEAD_OFFSET = UNSAFE.objectFieldOffset(Index.class.getDeclaredField("value"));
            TAIL_OFFSET = UNSAFE.objectFieldOffset(Index.class.getDeclaredField("value"));

            ARRAY_BASE = UNSAFE.arrayBaseOffset(Object[].class);
            int scale = UNSAFE.arrayIndexScale(Object[].class);
            ELEMENT_SHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public UnsafeRingBuffer(int capacity) {
        int size = 1 << (32 - Integer.numberOfLeadingZeros(capacity - 1)); // size has to be power of 2
        this.mask = size - 1;
        this.buffer = new Object[size]; // all allocation in construction phase to prevent GC; pre-size the collection
    }

    @Override
    public boolean offer(T value) {
        long producerIndex, nextProducerIndex;
        do {
            producerIndex = head.value;
            nextProducerIndex = (producerIndex + 1) & mask;
            if (nextProducerIndex == tail.value) {
                LOGGER.log("RingBuffer is full");
                return false;
            }
        } while (!UNSAFE.compareAndSwapLong(head, HEAD_OFFSET, producerIndex, nextProducerIndex));

        UNSAFE.putOrderedObject(buffer, index(producerIndex), value);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T poll() {
        long consumerIndex;
        T value;
        do {
            consumerIndex = tail.value;
            if (consumerIndex == head.value)  {
                LOGGER.log("RingBuffer is empty");
                return null;
            }

            value = (T) UNSAFE.getObjectVolatile(buffer, index(consumerIndex));
        } while (value == null || !UNSAFE.compareAndSwapLong(tail, TAIL_OFFSET, consumerIndex, (consumerIndex + 1) & mask));

        UNSAFE.putOrderedObject(buffer, index(consumerIndex), null);
        return value;
    }

    private long index(long index) {
        return ARRAY_BASE + (index << ELEMENT_SHIFT);
    }
}