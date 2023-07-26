package com.around.reactive.controller.v2;


import com.around.reactive.dto.Book;
import com.around.reactive.dto.BookDto;
import com.around.reactive.mapper.v2.BookMapper;
import com.around.reactive.service.v2.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController("bookControllerV2")
@RequestMapping("/v2/books")
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
    public Mono postBook(@RequestBody Mono<BookDto.Post> requestBody){
        Mono<Book> result = bookService.createBook(requestBody);

        return result.flatMap(book -> Mono.just(bookMapper.bookToResponse(book)));
    }

    @PatchMapping("/{book-id}")
    public Mono patchBook(@PathVariable("book-id") long bookId,
                          @RequestBody Mono<BookDto.Patch> requestBody){

        Mono<Book> result = bookService.updateBook(bookId, requestBody);

        return result.flatMap(book -> Mono.just(bookMapper.bookToResponse(book)));
    }

    @GetMapping("/{book-id}")
    public Mono getBook(@PathVariable("book-id") long bookId){
        return bookService.findBook(bookId).flatMap(book -> Mono.just(bookMapper.bookToResponse(book)));
    }
}
