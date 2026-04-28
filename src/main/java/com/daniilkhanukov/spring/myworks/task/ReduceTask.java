package com.daniilkhanukov.spring.myworks.task;

import com.daniilkhanukov.spring.myworks.Coordinator;
import com.daniilkhanukov.spring.myworks.reduce.Reducer;
import com.daniilkhanukov.spring.myworks.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class ReduceTask implements Task {

    private final int reduceId;
    private final List<String> intermediateFiles;
    private final Reducer reducer;
    private static final Logger log = LoggerFactory.getLogger(ReduceTask.class);

    public ReduceTask(int reduceId, List<String> intermediateFiles, Reducer reducer) {
        this.reduceId = reduceId;
        this.intermediateFiles = intermediateFiles;
        this.reducer = reducer;
    }


    @Override
    public void execute(Coordinator coordinator, Worker worker) {
        try {
            Map<String, List<String>> groups = new HashMap<>();
            for (String fileName : intermediateFiles) {
                Path p = Path.of("intermediate", fileName);
                if (!Files.exists(p)) continue;
                try (BufferedReader r = Files.newBufferedReader(p)) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        String[] parts = line.split("\t", 2);
                        if (parts.length < 2) continue;
                        String key = parts[0];
                        String val = parts[1];
                        groups.computeIfAbsent(key, k -> new ArrayList<>()).add(val);
                    }
                }
            }

            // сортировка ключей в алфавитном порядке
            List<String> keys = new ArrayList<>(groups.keySet());
            Collections.sort(keys);

            Path outDir = Path.of("output");
            Files.createDirectories(outDir);
            Path outFile = outDir.resolve("mr-out-" + reduceId + ".txt");
            try (BufferedWriter w = Files.newBufferedWriter(outFile)) {
                for (String key : keys) {
                    String result = reducer.reduce(key, groups.get(key));
                    w.write(key + " " + result);
                    w.newLine();
                }
            }

            coordinator.reportReduceDone(reduceId);
            log.info("{} finished REDUCE {} -> {}",worker.getName(), reduceId, outFile.getFileName());
        } catch (IOException ex) {
            log.info("ReduceTask error for reduceId= {}: {}", reduceId, ex.getMessage());

            coordinator.reportReduceDone(reduceId);
        }
    }
}
