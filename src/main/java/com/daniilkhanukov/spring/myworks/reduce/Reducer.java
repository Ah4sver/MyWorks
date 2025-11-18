package com.daniilkhanukov.spring.myworks.reduce;

import java.util.List;

/**
 * key добавил для возможного расширения в будущем, либо для логирования
 */
public interface Reducer {
    String reduce(String key, List<String> values);
}
