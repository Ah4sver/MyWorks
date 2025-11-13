package com.daniilkhanukov.spring.myworks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyWorksApplication {

    public static void main(String[] args) throws InterruptedException {

        int N = 13;
        NumberPrinter printer = new NumberPrinter(N);

        Thread tEven = new Thread(printer::printEven);
        Thread tOdd  = new Thread(printer::printOdd);

        tEven.start();
        tOdd.start();

        tEven.join();
        tOdd.join();

//        SpringApplication.run(MyWorksApplication.class, args);
    }

}
