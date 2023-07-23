package com.around.reactive.example14;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.net.URI;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.Collections;

@Slf4j
public class Example14Time {
    public static void main(String[] args) throws InterruptedException {
        //example14_51();
        example14_52();
    }

    /**
     * Reacotr Sequence의 동작 시간 자체를 측정하는 특별한 Operator가 존재한다
     * elapsed() Operator는 emit된 데이터 사이의 경과 시간을 측정해서 Tuple<Long, T> 형태로 Downstream에 emit한다
     * @throws InterruptedException
     */
    public static void example14_51() throws InterruptedException {
        Flux
                .range(1, 5)
                .delayElements(Duration.ofSeconds(1))
                .elapsed()
                .subscribe(data -> log.info("# onNExt: {}, time {}", data.getT2(), data.getT1()));
        Thread.sleep(6000);
    }

    /**
     * HTTP 요청을 반복적으로 전송해서 응답 시간을 측정하는 예제 코드
     * repeatr() Operator를 ㅣ용ㅇ해 구독을 4회 반복해서 HTTP request를 반복적으로 전송하고 있는데 최초 구독 + repeat() Operator의 파라미터 숫자만큼의 HTTP request를 전송한다.
     */
    public static void example14_52(){
        URI worldTimeUri = UriComponentsBuilder.newInstance().scheme("http")
                .host("worldtimeapi.org")
                .port(80)
                .path("/api/timezone/Asia/Seoul")
                .build()
                .encode()
                .toUri();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Mono.defer( () -> Mono.just(
                restTemplate
                        .exchange(
                                worldTimeUri,
                                HttpMethod.GET,
                                new HttpEntity<>(httpHeaders),
                                String.class)
                        )
                )
                .repeat(4)
                .elapsed()
                .map(response -> {
                    DocumentContext jsonContext =
                            JsonPath.parse(response.getT2().getBody());
                    String dateTime = jsonContext.read("$.datetime");
                    return Tuples.of(dateTime, response.getT1());
                })
                .subscribe(
                        data -> log.info("now: {}, elpased: {}", data.getT1(), data.getT2()),
                        error -> log.error("# onError:", error),
                        () -> log.info("# onComplete")
                );
    }

}
