package com.around.reactive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleBook {
    private long bookId;
    private String name;
    private int price;
}