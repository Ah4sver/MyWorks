package com.daniilkhanukov.spring.myworks;

import com.daniilkhanukov.spring.myworks.task.Task;

public class Worker extends Thread {
    private final Coordinator coordinator;

    public Worker(Coordinator coordinator, int id) {
        super("Worker-" + id);
        this.coordinator = coordinator;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Task task = coordinator.getTask();
                if (task == null) break;
                task.execute(coordinator, this);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(getName() + " ended");
    }
}
