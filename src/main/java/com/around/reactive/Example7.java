package com.around.reactive;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Documented;
import java.net.URI;
import java.sql.Time;
import java.time.Duration;
import java.util.Arrays;

@Slf4j
public class Example7 {
    public static void main(String[] args) throws InterruptedException {
        //example7_1();
        //example7_2();
        example7_3();
    }

    //Cold Sequence에 대한 예제
    public static void example7_1() throws InterruptedException {
        Flux<String> coldFlux =
                Flux.fromIterable(Arrays.asList("KOREA", "JAPAN", "CHINESE"))
                        .map(String::toLowerCase);

        coldFlux.subscribe(country -> log.info("# subscriber1 : {}", country));
        System.out.println("----------------------------");
        Thread.sleep(2000L);
        coldFlux.subscribe(country -> log.info("# subscriber2 : {}", country));
    }
    //Hot Sequence에 대한 예제
    public static void example7_2() throws InterruptedException {
        String[] signers = {"singer A", "singer B", "singer C", "singer D", "singer E"};
        log.info("# Begin concert:");
        Flux<String> concertFlux = Flux.fromArray(signers)
                .delayElements(Duration.ofSeconds(1))
                .share();

        concertFlux.subscribe(
                singer -> log.info("# Subscriber1 is watching {}'s song", singer)
        );

        Thread.sleep(2500L);

        concertFlux.subscribe(
                singer -> log.info("# Subscriber2 is watching {}'s song", singer)
        );

        Thread.sleep(3000L);
    }

    public static void example7_3() throws InterruptedException {
        URI worldTimeUri = UriComponentsBuilder.newInstance().scheme("http")
                .host("worldtimeapi.org")
                .port(80)
                .path("/api/timezone/Asia/Seoul")
                .build()
                .encode()
                .toUri();
        //Mono<String> mono = getWorldTime(worldTimeUri); //Cold Sequence 동작
        Mono<String> mono = getWorldTime(worldTimeUri).cache(); //Hot Sequence 동작
        mono.subscribe(dateTime -> log.info("# dateTime 1 : {}", dateTime));
        Thread.sleep(2000L);
        mono.subscribe(dateTime -> log.info("# dateTime 2 : {}", dateTime));

        Thread.sleep(2000L);
    }

    private static Mono<String> getWorldTime(URI worldTimeUri){
        return WebClient.create()
                .get()
                .uri(worldTimeUri)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    DocumentContext jsonContext = JsonPath.parse(response);
                    String dateTime = jsonContext.read("$.datetime");
                    return dateTime;
                });
    }
}
