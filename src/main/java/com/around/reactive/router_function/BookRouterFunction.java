package com.around.reactive.router_function;

import com.around.reactive.dto.BookDto;
import com.around.reactive.filter.BookRouterFunctionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class BookRouterFunction {

    @Bean
    public RouterFunction routerFunction(){
        return RouterFunctions
                .route(GET("/vi/router/books/{book-id}"), (ServerRequest request) -> this.getBook(request))
                .filter(new BookRouterFunctionFilter());
    }

    public Mono<ServerResponse> getBook(ServerRequest request){
        return ServerResponse
                .ok()
                .body(Mono
                        .just(BookDto.builder()
                                .bookId(Long.parseLong(request.pathVariable("book-id")))
                                .bookName("Advanced Reactor")
                                .author("Tom")
                                .isbn("222-22-2222-222-2").build()), BookDto.class);
    }

}