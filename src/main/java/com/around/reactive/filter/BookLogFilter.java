package com.around.reactive.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class BookLogFilter implements WebFilter {


    /**
     * doAtferTerminate() OPErator를 이용해 종료 이벤트 발생 시 요청 URI path에 "books"가 포함되어 있다면 Book 리소스에 대한 요청이라고 간주하고 로그 출력
     * @param exchange the current server exchange
     * @param chain provides a way to delegate to the next filter
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        return chain.filter(exchange).doAfterTerminate( () -> {
            if(path.contains("books")){
                System.out.println("path: "+ path + ", status: " + exchange.getResponse().getStatusCode());
            }
        });
    }
}
