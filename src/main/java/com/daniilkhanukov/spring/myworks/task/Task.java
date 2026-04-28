package com.daniilkhanukov.spring.myworks.task;

import com.daniilkhanukov.spring.myworks.Coordinator;
import com.daniilkhanukov.spring.myworks.Worker;

public interface Task {
    void execute(Coordinator coordinator, Worker worker);
}
