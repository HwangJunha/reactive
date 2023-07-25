package com.around.reactive.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


import com.around.reactive.dto.Book;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/controller/books")
public class BookController {

    private Map<Long, Book> bookMap;

    @Autowired
    public BookController(
            Map<Long, Book> bookMap){
        this.bookMap = bookMap;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{book-id}")
    public Mono<Book> getBook(@PathVariable("book-id") long bookId)
            throws InterruptedException {
        Thread.sleep(5000);

        Book book = bookMap.get(bookId);
        log.info("# book for response: {}, {}", book.getBookId(), book.getName());
        return Mono.just(book);
    }
}
