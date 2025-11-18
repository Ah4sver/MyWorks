package com.daniilkhanukov.spring.myworks.task;

import com.daniilkhanukov.spring.myworks.Coordinator;
import com.daniilkhanukov.spring.myworks.KeyValue;
import com.daniilkhanukov.spring.myworks.map.Mapper;
import com.daniilkhanukov.spring.myworks.Worker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class MapTask implements Task {

    private final int mapId;
    private final String inputFile;
    private final int numReduce;
    private final Mapper mapper;

    public MapTask(int mapId, String inputFile, int numReduce, Mapper mapper) {
        this.mapId = mapId;
        this.inputFile = inputFile;
        this.numReduce = numReduce;
        this.mapper = mapper;
    }

    @Override
    public void execute(Coordinator coordinator, Worker worker) {
        try {
            String content = Files.readString(Path.of(inputFile));
            List<KeyValue> pairs = mapper.map(inputFile, content);

            Map<Integer, List<KeyValue>> buckets = new HashMap<>();
            for (KeyValue kv : pairs) {
                int r = (kv.getKey().hashCode() & Integer.MAX_VALUE) % numReduce;
//                System.out.println("mapId=" + mapId + " key=[" + kv.getKey() + "] -> " + r);
                buckets.computeIfAbsent(r, k -> new ArrayList<>()).add(kv);
            }

            Map<Integer, String> created = new HashMap<>();
            for(Map.Entry<Integer, List<KeyValue>> e : buckets.entrySet()) {
                int r = e.getKey();
                String fileName = "mr-" + mapId + "-" + r + ".txt";
                try (BufferedWriter w = Files.newBufferedWriter(Path.of("intermediate", fileName))) {
                    for (KeyValue kv : e.getValue()) {
                        w.write(kv.getKey() + "\t" + kv.getValue());
                        w.newLine();
                    }
                }
                created.put(r, fileName);
            }
            coordinator.reportMapDone(mapId, created);
            System.out.println(worker.getName() + " finished MAP " + mapId + " (" + inputFile + ")");
        } catch (IOException e) {
            System.out.println("MapTask error for " + inputFile + ": " + e.getMessage());
            coordinator.reportMapDone(mapId, Collections.emptyMap());
        }
    }
}
