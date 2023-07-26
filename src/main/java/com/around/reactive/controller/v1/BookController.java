package com.around.reactive.controller.v1;


import com.around.reactive.dto.BookDto;
import com.around.reactive.mapper.v1.BookMapper;
import com.around.reactive.service.v1.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


import com.around.reactive.dto.Book;

@Slf4j
@RestController
@RequestMapping("/v1/books")
public class BookController {

    private final BookService bookService;

    private final BookMapper bookMapper;

    @Autowired
    public BookController(
          BookService bookService,
          BookMapper bookMapper){
        this.bookMapper = bookMapper;
        this.bookService = bookService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono postBook(@RequestBody BookDto.Post requestBody){
        Mono<Book> book = bookService.createBook(bookMapper.bookPostToBook(requestBody));

        Mono<BookDto.Response> response = bookMapper.bookToBookResponse(book);
        return response;
    }

    @PatchMapping("/{book-id}")
    public Mono patchBook(@PathVariable("book-id") long bookId,
                          @RequestBody BookDto.Patch requestBody){
        requestBody.setBookId(bookId);
        Mono<Book> book = bookService.updateBook(bookMapper.bookPatchToBook(requestBody));

        return bookMapper.bookToBookResponse(book);
    }

    @GetMapping("/{book-id}")
    public Mono getBook(@PathVariable("book-id") long bookId){
        Mono<Book> book = bookService.findBook(bookId);
        return bookMapper.bookToBookResponse(book);
    }
}
