package com.daniilkhanukov.spring.myworks.map;

import com.daniilkhanukov.spring.myworks.KeyValue;

import java.util.List;

/**
 * fileName добавил для возможного расширения в будущем, либо для логирования
 */
public interface Mapper {
    List<KeyValue> map(String fileName, String content);
}
