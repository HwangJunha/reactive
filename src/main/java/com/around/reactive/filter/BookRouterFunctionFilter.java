package com.around.reactive.filter;

import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class BookRouterFunctionFilter implements HandlerFilterFunction {
    /**
     * WebFilter 구현체는 Spring Bean으로 등록되는 반면 HandlerFilterFunction 구현체는 애너테이션 기반의 핸드러가 아닌 함수형 기반의 요ㅕ청 핸들러에서 함수 형태로 사용되기 때문에 Spring Bean으로 등록되지 않는다는 차이점이 있다
     * @param request the request
     * @param next the next handler or filter function in the chain
     * @return
     */
    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction next) {
        String path = request.requestPath().value();

        return next.handle(request).doAfterTerminate(() ->{
            System.out.println("path: "+ path + ", status: "+ request.exchange().getResponse().getStatusCode());
        });
    }

}

