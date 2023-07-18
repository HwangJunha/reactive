package com.around.reactive;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Slf4j
public class Example12 {
    public static Map<String, String> fruits = new HashMap<>();


    static {
        fruits.put("banana", "바나나");
        fruits.put("apple", "사과");
        fruits.put("pear", "배");
        fruits.put("grape", "포도");
    }

    public static void main(String [] args){
        //example12_1();
        //example12_2();
        //example12_3();
        //example12_4();
        //example12_5();
        //example12_6();
        example12_7();
    }

    /**
     *  Hooks.onOperatorDebug을 활성화 하면 Operator 체인이 시작되기 전에 디버그 모드를 활성화하면 에러가 발생하면 지점을 좀 더 명확하게 원인을 분석해준다
     *  애플리케이션 내에 있는 모든 Operator의 스택트레이스를 캡처한다
     *  에러가 발생하면 캡처한 정보를 기반으로 에러가 발생한 Assembly의 스택트레이스를 우너본 스택트레이스 중간에 끼워 넣는다
     *  따라서 에러 원인을 추적하기 위해 처음부터 디버그 모드를 활성화하는 것은 권장되지 않는다.
     */
    @SneakyThrows
    public static void example12_1(){
        Hooks.onOperatorDebug();

        Flux
                .fromArray(new String[]{"BANANAS", "APPLES", "PEARS", "MELONS"})
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel())
                .map(String::toLowerCase)
                .map(fruits -> fruits.substring(0, fruits.length()-1))
                .map(fruits::get)
                .map(translated -> "맛있는 "+ translated)
                .subscribe(
                        log::info,
                        error -> log.error("# onError:", error));
        Thread.sleep(100L);
    }

    /**
     * ArithmeticException을 발생한 것을 알 수 있지만 정확하게 어느 지점에서 발생되었는지 확인되지 않음
     * checkpoint 지점까지는 에러가 전파되었다는 것을 알 수 있다.
     */
    @SneakyThrows
    public static void example12_2(){
        Flux
                .just(2,4,6,8)
                .zipWith(Flux.just(1,2,3,0), (x,y) -> x/y)
                .map(num -> num+2)
                .checkpoint()
                .subscribe(
                        data -> log.info("# onNext : {}", data),
                        error -> log.error("# onError: {}", error)
                );
        Thread.sleep(100L);
    }

    /**
     * checkpoint를 하나 더 넣는 것으로 zipwith에서 에러가 발생할 수 있다는걸 추측할 수 있다.
     * zipWith에서 에러가 발생해 Downstream으로 전파 되었음을 알 수 있다.
     *
     */
    @SneakyThrows
    public static void example12_3(){
        Flux
                .just(2,4,6,8)
                .zipWith(Flux.just(1,2,3,0), (x,y) -> x/y)
                .checkpoint()
                .map(num -> num+2)
                .checkpoint()
                .subscribe(
                        data -> log.info("# onNext : {}", data),
                        error -> log.error("# onError: {}", error)
                );
        Thread.sleep(100L);
    }

    /**
     * checkpoint()에 description을 추가해 Traceback 대신에 description을 출력하도록 변경
     */
    @SneakyThrows
    public static void example12_4(){
        Flux
                .just(2,4,6,8)
                .zipWith(Flux.just(1,2,3,0), (x,y) -> x/y)
                .checkpoint("example_4.zipwith.checkpoint")
                .map(num -> num+2)
                .checkpoint("example_4.map.checkpoint")
                .subscribe(
                        data -> log.info("# onNext : {}", data),
                        error -> log.error("# onError: {}", error)
                );
        Thread.sleep(100L);
    }

    /**
     * checkpoint(description, forceStatcTrace)를 사용하면 description과 Traceback을 모두 출력 할 수 있다.
     */
    @SneakyThrows
    public static void example12_5(){
        Flux
                .just(2,4,6,8)
                .zipWith(Flux.just(1,2,3,0), (x,y) -> x/y)
                .checkpoint("example_4.zipwith.checkpoint", true)
                .map(num -> num+2)
                .checkpoint("example_4.map.checkpoint", true)
                .subscribe(
                        data -> log.info("# onNext : {}", data),
                        error -> log.error("# onError: {}", error)
                );
        Thread.sleep(100L);
    }

    /**
     * 이 처럼 Opertor 체인이 기능별로 여러 곳에 흩어져 있는 경우라면 checkpoint()를 추가히자 않은 상태에서 에러가 발생했을 떄 어디에서 에러가 발생했는지 발견하기가 쉽지 않다
     * 이 경우 checkpoint()를 추가한 후 범위를 좁혀가면서 단계적으로 에러 발생 지점을 찾을 수 있다
     */
    @SneakyThrows
    public static void example12_6(){
        Flux<Integer> source = Flux.just(2,4,6,8);
        Flux<Integer> other = Flux.just(1,2,3,0);

        Flux<Integer> multiplySource = multiply(source, other).checkpoint();
        Flux<Integer> plusSource = plus(multiplySource).checkpoint();

        plusSource.subscribe(
                data -> log.info("# onNext: {}", data),
                error -> log.error("# onError:", error)
        );

        Thread.sleep(100L);
    }

    private static Flux<Integer> multiply(Flux<Integer> source, Flux<Integer> other){
        return source.zipWith(other, (x,y) -> x/y);
    }

    private static Flux<Integer> plus(Flux<Integer> source){
        return source.map(num -> num+2);
    }

    /**
     * melon이라는 문자열을 emit했지만 두 번째 map() Operator 이후의 어떤 지점에서 melon 문자열을 처리하는 와중에 에러가 발생했다
     * .log() -> .log("Fruit.substring", Level.FINE)와 같이 바꾸어서 테스트를 했을 경우 로그 레벨이 DEBUG로 바뀌고 map() Opeator에서 발생한 Signal이라는 것을 쉽게 구분할 수 있도록 Fruit.substring이라는 카테고리까지 표시해 준다
     * FINE은 자바에서 지원하는 로그레벨로 SL4J에서는 DEBUG에 해당한다
     * log는 개수 제한이 없기 때문에 필요에 따라 다른 Operator뒤에 추가해서 Reactor Sequence의 내부 동작을 살펴 볼 수 있다.
     */
    @SneakyThrows
    public static void example12_7(){

        Flux
                .fromArray(new String[]{"BANANAS", "APPLES", "PEARS", "MELONS"})
                .map(String::toLowerCase)
                .map(fruits -> fruits.substring(0, fruits.length()-1))
                .log()
                .map(fruits::get)
                .subscribe(
                        log::info,
                        error -> log.error("# onError:", error));
        Thread.sleep(100L);
    }

}
