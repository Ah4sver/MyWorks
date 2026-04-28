package com.daniilkhanukov.spring.myworks.map;

import com.daniilkhanukov.spring.myworks.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WordCountMapper implements Mapper {

    private static final Logger log = LoggerFactory.getLogger(WordCountMapper.class);

    @Override
    public List<KeyValue> map(String fileName, String content) {
        log.info("Mapping file: {}", fileName);
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
