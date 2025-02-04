package com.allison.service;

public interface RingBuffer<T> {
    boolean offer(T value);

    T poll();
}
