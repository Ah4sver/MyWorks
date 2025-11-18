package com.daniilkhanukov.spring.myworks.reduce;

import java.util.List;

public class WordCountReducer implements Reducer {
    @Override
    public String reduce(String key, List<String> values) {
        int sum = 0;
        for (String value : values) {
            try {
                sum += Integer.parseInt(value);
            } catch (NumberFormatException e) {}
        }
        return String.valueOf(sum);
    }
}
