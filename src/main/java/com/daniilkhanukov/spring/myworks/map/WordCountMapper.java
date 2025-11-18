package com.daniilkhanukov.spring.myworks.map;

import com.daniilkhanukov.spring.myworks.KeyValue;

import java.util.ArrayList;
import java.util.List;

public class WordCountMapper implements Mapper {

    @Override
    public List<KeyValue> map(String fileName, String content) {
        System.out.println("Mapping file: " + fileName);
        List<KeyValue> result = new ArrayList<>();
        if (content == null || content.isEmpty()) return result;
        String[] lines = content.split("\\W+");
        for (String line : lines) {
            if (line.isEmpty()) continue;
            result.add(new KeyValue(line.toLowerCase(), "1"));
        }
        return result;
    }
}
