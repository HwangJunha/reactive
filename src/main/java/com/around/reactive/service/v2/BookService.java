package com.around.reactive.service.v2;

import com.around.reactive.dto.Book;
import com.around.reactive.dto.BookDto;
import com.around.reactive.mapper.v2.BookMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service("bookServiceV2")
public class BookService {
    private final BookMapper bookMapper;
    public BookService(
            BookMapper bookMapper
    ){
        this.bookMapper = bookMapper;
    }

    public Mono<Book> createBook(Mono<BookDto.Post> book){
        return book.flatMap(post -> Mono.just(bookMapper.bookPostToBook(post)));
    }

    public Mono<Book> updateBook(final long bookId, Mono<BookDto.Patch> book){
        return book.flatMap(patch -> {
            patch.setBookId(bookId);
            return Mono.just(bookMapper.bookPatchToBook(patch));
        });
    }

    public Mono<Book> findBook(Long bookId){
        return Mono.just(
                new Book(bookId, "Java 고급", "Advanced Java", "Kevin", "111-11-1111-111-1", "Java 중급 프로그래밍 마스터", "2022-03-22", LocalDateTime.now(), LocalDateTime.now())
        );
    }





}
