package com.daniilkhanukov.spring.myworks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RingBuffer {
    private final int[] buffer;
    private final int capacity;
    private int head = 0;
    private int tail = 0;
    private int size = 0;

    public RingBuffer(int capacity) {
        this.capacity = capacity;
        buffer = new int[capacity];
    }

    private static final Logger log = LoggerFactory.getLogger(RingBuffer.class);

    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    public void put(int value) throws InterruptedException {
        lock.lock();
        try {
            while (size == capacity) {
                notFull.await();
            }
            buffer[tail] = value;
            tail = (tail + 1) % capacity;
            size++;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public int get() throws InterruptedException {
        lock.lock();
        try {
            while (size == 0) {
                notEmpty.await();
            }
            int value = buffer[head];
            head = (head + 1) % capacity;
            size--;
            notFull.signal();
            return value;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        RingBuffer ringBuffer = new RingBuffer(5);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 23; i++) {
                    ringBuffer.put(i);
                    log.info("Вставлено значение {}", i);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 23; i++) {
                    int item = ringBuffer.get();
                    log.info("Получено значение: {}", item);
                    Thread.sleep(2500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
        log.info("Конец");
    }
}
