package com.daniilkhanukov.spring.myworks.reduce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class WordCountReducer implements Reducer {
    private static final Logger log = LoggerFactory.getLogger(WordCountReducer.class);
    @Override
    public String reduce(String key, List<String> values) {
        int sum = 0;
        for (String value : values) {
            try {
                sum += Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
            }
        }
        return String.valueOf(sum);
    }
}
