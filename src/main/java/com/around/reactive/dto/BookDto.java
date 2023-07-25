package com.around.reactive.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BookDto {

    private long bookId;
    private String bookName;
    private String author;
    private String isbn;
}
