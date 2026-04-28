package com.daniilkhanukov.spring.myworks;

import com.daniilkhanukov.spring.myworks.map.Mapper;
import com.daniilkhanukov.spring.myworks.reduce.Reducer;
import com.daniilkhanukov.spring.myworks.task.MapTask;
import com.daniilkhanukov.spring.myworks.task.ReduceTask;
import com.daniilkhanukov.spring.myworks.task.Task;

import java.util.*;

/**
 * Coordinator:
 * - хранит очередь задач (taskQueue)
 * - сначала в очереди находятся MAP задачи
 * - после того, как все map завершены, coordinator создаёт и кладёт в очередь REDUCE задачи
 * - метод getTask() блокируется (wait) пока задачи не появятся или пока весь процесс не завершится
 */
public class Coordinator {
    private final Queue<Task> taskQueue = new ArrayDeque<>();
    private final int numMapTasks;
    private final int numReduceTasks;
    private final Reducer reducer;

    // для каждого reduce-id список имён промежуточных файлов, которые нужно обработать
    private final List<List<String>> intermediateFilesPerReduce;

    private int remainingMapTasks;
    private int remainingReduceTasks;
    // флаг завершения
    private boolean done = false;

    public Coordinator(List<String> inputFiles, int numReduceTasks, Mapper mapper, Reducer reducer) {
        this.numMapTasks = inputFiles.size();
        this.remainingMapTasks = numMapTasks;
        this.numReduceTasks = numReduceTasks;
        this.remainingReduceTasks = numReduceTasks;
        this.reducer = reducer;

        // создание списка списков
        this.intermediateFilesPerReduce = new ArrayList<>(numReduceTasks);
        for (int i = 0; i < numReduceTasks; i++) {
            intermediateFilesPerReduce.add(new ArrayList<>());
        }

        // заполнение taskQueue объектами MapTask
        for (int mapId = 0; mapId < inputFiles.size(); mapId++) {
            String fileName = inputFiles.get(mapId);
            taskQueue.add(new MapTask(mapId, fileName, numReduceTasks, mapper));
        }
    }

    /**
     * Воркеры вызывают этот метод, чтобы получить задачу.
     * Если нет задач и процесс не завершён - поток ждёт (wait).
     * Если процесс завершён - возвращает null (сигнал на выход)
     */
    public synchronized Task getTask() throws InterruptedException {
        while (taskQueue.isEmpty() && !done) {
            wait();
        }
        if (done) return null;
        return taskQueue.poll();
    }

    /**
     * Воркеры сообщают о завершении map задачи и передают имена созданных файлов (map: reduceId -> filename).
     * Если для какого-то reduce файл не создан - в мапе этого reduceId не будет
     */
    @SuppressWarnings("java:S1172")
    public synchronized void reportMapDone(int mapId, Map<Integer, String> createdFiles) {
        // добавление имён файлов к соответствующим reduce спискам
        for (Map.Entry<Integer, String> e : createdFiles.entrySet()) {
            int reduceId = e.getKey();
            String fileName = e.getValue();
            intermediateFilesPerReduce.get(reduceId).add(fileName);
        }

        remainingMapTasks--;
        // если все map завершены - создаются reduce задачи и будятся воркеры
        if (remainingMapTasks == 0) {

            for (int r = 0; r < numReduceTasks; r++) {
                List<String> filesForR = new ArrayList<>(intermediateFilesPerReduce.get(r));
                taskQueue.add(new ReduceTask(r, filesForR, reducer));
            }
            notifyAll();
        }
    }

    /**
     * Воркеры сообщают о завершении reduce задачи.
     * Когда все reduce выполнены, меняется флаг done=true и пробуждаются ожидающие
     */
    @SuppressWarnings("java:S1172")
    public synchronized void reportReduceDone(int reduceId) {
        remainingReduceTasks--;
        if (remainingReduceTasks == 0) {
            done = true;
            notifyAll();
        } else {
            notifyAll();
        }
    }
}