package com.daniilkhanukov.spring.myworks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class NumberPrinter {

    private final int max;
    private int current = 0;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private boolean isEvenNow = true;

    /**
     *
     * @param max  до указанного числа невключительно
     */
    public NumberPrinter(int max) {
        this.max = max;
    }

    public void printEven() {
        try {
            while (true) {
                lock.lock();
                try {
                    while (current < max && !isEvenNow) {
                        condition.await();
                    }
                    if (current >= max) {
                        condition.signalAll();
                        break;
                    }
                    System.out.println("Чётный поток: " + current);
                    current++;
                    isEvenNow = false;
                    condition.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void printOdd() {
        try {
            while (true) {
                lock.lock();
                try {
                    while (current < max && isEvenNow) {
                        condition.await();
                    }
                    if (current >= max) {
                        condition.signalAll();
                        return;
                    }
                    System.out.println("Нечётный поток: " + current);
                    current++;
                    isEvenNow = true;
                    condition.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
