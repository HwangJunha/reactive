package com.around.reactive.config;

import com.around.reactive.dto.Book;
import com.around.reactive.dto.SimpleBook;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BookConfiguration {
    @Bean
    public Map<Long, SimpleBook> bookMap() {
        Map<Long, SimpleBook> bookMap = new HashMap<>();
        for (long i = 1; i <= 2_000_000; i++) {
            bookMap.put(i, new SimpleBook(i, "IT Book" + i, 2000));
        }

        return bookMap;
    }
}
