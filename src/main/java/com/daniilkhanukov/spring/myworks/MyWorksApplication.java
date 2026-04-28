package com.daniilkhanukov.spring.myworks;

import com.daniilkhanukov.spring.myworks.map.Mapper;
import com.daniilkhanukov.spring.myworks.map.WordCountMapper;
import com.daniilkhanukov.spring.myworks.reduce.Reducer;
import com.daniilkhanukov.spring.myworks.reduce.WordCountReducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Пример запуска:
 * - создаёт папки intermediate и output (если не существуют),
 * - создаёт тестовые входные файлы (если не существуют),
 * - запускает Coordinator и N воркеров.
 */
@SpringBootApplication
public class MyWorksApplication {

    public static void main(String[] args) throws InterruptedException, IOException {

        Logger log = LoggerFactory.getLogger(MyWorksApplication.class);
        // Примеры входных файлов
        List<String> inputs = List.of("file1.txt", "file2.txt", "file3.txt", "file4.txt", "file5.txt", "file6.txt", "file7.txt");
        createSampleIfMissing(inputs);

        // Параметры
        int numReduce = 3;
        int numWorkers = 4;

        // Создаём каталоги
        Files.createDirectories(Path.of("intermediate"));
        Files.createDirectories(Path.of("output"));

        // Создаём Coordinator
        Mapper mapper = new WordCountMapper();
        Reducer reducer = new WordCountReducer();
        Coordinator coordinator = new Coordinator(inputs, numReduce, mapper, reducer);

        // Запускаем воркеров
        Thread[] workers = new Thread[numWorkers];
        for (int i = 0; i < numWorkers; i++) {
            workers[i] = new Worker(coordinator, i);
            workers[i].start();
        }

        // Ждём окончания
        for (Thread t : workers) {
            t.join();
        }

        log.info("MapReduce finished. Check directory 'output' for mr-out-*.txt");
    }

    private static void createSampleIfMissing(List<String> files) throws IOException {
        String[] sample = new String[] {
                "What is love? Baby dont hurt me, dont hurt me, no more",
                "No I dont know why you are not there. I give you my love, but you dont care",
                "So what is right and what is wrong? Give me a sign",
                "Whoa, whoa, whoa, whoa, oh-whoa, whoa, ohh, ooh",
                "Whoa, whoa, whoa, whoa, oh-whoa, whoa, ohh, ooh",
                "Oh, I dont know, what can I do? What else can I say, it is up to you",
                "I know we are one. Just me and you. I cant go on"
        };
        for (int i = 0; i < files.size(); i++) {
            Path p = Path.of(files.get(i));
                Files.writeString(p, sample[i % sample.length]);
        }
    }

}
