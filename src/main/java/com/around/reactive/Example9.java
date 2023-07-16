package com.around.reactive;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.stream.IntStream;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

@Slf4j
public class Example9 {
    public static void main(String[] args){
        //example9_1();
        //example9_2();
        //example9_4();
        //example9_8();
        //example9_9();
        example9_10();
    }

    /**
     * Sinks는 멀티스레드 방식으로 Signal을 전송해도 스레드 안전성을 보장하기 때문에 예기치 않은 동작으로 이어지는 것을 방지해줍니다.
     * create() Operator를 사용해 프로그래밍 방식으로 Signal를 전송하는 예제 코드입니다.
     */
    @SneakyThrows
    public static void example9_1(){
        int tasks = 6;

        Flux
                .create((FluxSink<String> sink) -> {
                    IntStream
                            .range(1, tasks)
                            .forEach(n -> sink.next(doTasks(n)));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(n -> log.info("# create(): {}", n))
                .publishOn(Schedulers.parallel())
                .map(result -> result + " success!")
                .doOnNext(n -> log.info("# map() : {}", n))
                .publishOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(5000L);

    }

    /**
     * 아처럼 Sinks는 프로그래밍 방식으로 Signal을 전송할 수 있으며, 멀티스레드 환경에서 스레드 안전성을 보장받을 수 있는 장점이 있습니다.
     */
    @SneakyThrows
    public static void example9_2(){
        int tasks = 6;

        Sinks.Many<String> unicastSink = Sinks.many().unicast().onBackpressureBuffer();
        Flux<String> fluxView = unicastSink.asFlux();
        IntStream
                .range(1, tasks)
                .forEach(n -> {
                    try{
                        new Thread(() ->{
                            unicastSink.emitNext(doTasks(n), FAIL_FAST);
                            log.info("# emitted: {}", n);
                        }).start();
                        Thread.sleep(1000L);
                    }catch (InterruptedException e){
                        log.error(e.getMessage());
                    }
                });
        fluxView
                .publishOn(Schedulers.parallel())
                .map(result -> result + " success!")
                .doOnNext(n -> log.info("# map(): {}", n))
                .publishOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(200L);




    }

    /**
     * Sinks.one은 메소드를 사용해서 한 건의 데이터를 전송하는 방법을 정의 해 둔 기능 명세라고 말 할  수 있다
     * 에러가 발생했을 때 재시도를 하지 않고 즉시 실패 처리를 한다.
     * Hi Reactor같은 경우 drop이 된다.
     */
    public static void example9_4(){
        Sinks.One<String> sinkOne = Sinks.one();
        Mono<String> mono = sinkOne.asMono();
        sinkOne.emitValue("Hello Reactor", FAIL_FAST);
        sinkOne.emitValue("Hi Reactor", FAIL_FAST);

        mono.subscribe(data -> log.info("# Subscriber1 ", data));
        mono.subscribe(data -> log.info("# Subscriber2 ", data));

    }

    /**
     * Sinks.Many 메소드를 사용해서 여러 건의 데이터를 여러 가지 방식으로 전송하는 기능을 정의해 둔 기능 명세라고 볼 수 있다.
     * unicast의 의미는 단 하나의 Subscriber에게만 데이터를 emit한다
     * 주석을 제거할 시 에러 발생
     */
    public static void example9_8(){
        Sinks.Many<Integer> unicastSink = Sinks.many().unicast().onBackpressureBuffer();
        Flux<Integer> fluxView = unicastSink.asFlux();

        unicastSink.emitNext(1, FAIL_FAST);
        unicastSink.emitNext(2, FAIL_FAST);

        fluxView.subscribe(data -> log.info("# Subscriber1: {}", data));

        unicastSink.emitNext(3, FAIL_FAST);
        //fluxView.subscribe(data -> log.info("# Subscriber2: {}", data));

    }

    /**
     * Multicast가 하나 이상의 일부 시스템들만 정보를 전달받는 방식이라고 설명한것처럼, MulticastSpec의 기능은 하나 이상의 Subscriber에게 데이터를 emit하는 것이다.
     * Sinks가 Publisher의 역할을 할 경우 기본적으로 Hot Publisher동작한다
     */
    public static void example9_9(){
        Sinks.Many<Integer> multicastSink= Sinks.many().multicast().onBackpressureBuffer();

        Flux<Integer> fluxView = multicastSink.asFlux();

        multicastSink.emitNext(1, FAIL_FAST);
        multicastSink.emitNext(2, FAIL_FAST);

        fluxView.subscribe(data -> log.info("# SubScriber1: {}", data));
        fluxView.subscribe(data -> log.info("# SubScriber2: {}", data));

        multicastSink.emitNext(3, FAIL_FAST);
    }


    /**
     * MulticastReplaySpec에는 emit된 데이터를 다시 replay해서 구독 전에 이미 emit된 데이터라도 Subscriber가 전달받을 수 있게 하는 다양한 메소들이 정의 되어 있다.
     * all()이라는 메소드는 구독 전에 이미 emit된 데이터가 있더라도 처음 emit된 데이터부터 모든 데이터들이 subscriber에게 전달 됩니다.
     * limit() 메소드는 emit된 데이터 중에서 파라미터로 입력한 개수만큼 가장 나중에 emit된 데이터부터 Subscribver에게 전달하는 기능을 합니다. 즉 emit된 데이터 중에서 2개만 뒤로 돌려서 전달하겠다는 의미이다.
     */
    public static void example9_10(){
        Sinks.Many<Integer> replaySink= Sinks.many().replay().limit(2);

        Flux<Integer> fluxView = replaySink.asFlux();

        replaySink.emitNext(1, FAIL_FAST);
        replaySink.emitNext(2, FAIL_FAST);
        replaySink.emitNext(3, FAIL_FAST);

        fluxView.subscribe(data -> log.info("# SubScriber1: {}", data));

        replaySink.emitNext(4, FAIL_FAST);
        fluxView.subscribe(data -> log.info("# SubScriber2: {}", data));
    }


    private static String doTasks(int taskNumber){
        return "task " + taskNumber + " result";
    }
}
