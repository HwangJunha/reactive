package part2;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
public class Example8 {

    public static void main(String[] args){
        //example8_1();
        //example8_2();
        //example8_3();
        //example8_4();
        //example8_5();
        example8_6();
    }

    //BackPressure 데이터 요청 개수 제안
    public static void example8_1(){
        Flux.range(1,5)
                .doOnRequest(data -> log.info("# doOnRequest: {}", data))
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @SneakyThrows
                    @Override
                    protected void hookOnNext(Integer value){
                        Thread.sleep(2000L);
                        log.info("# hookOnNext: {}", value);
                        request(1);
                    }
                });
    }

    //BackPressure error 전략
    @SneakyThrows
    public static void example8_2(){
        Flux.interval(Duration.ofMillis(1L))
                .onBackpressureError()
                .doOnNext(data -> log.info("# doOnNext: {}", data))
                .publishOn(Schedulers.parallel())
                .subscribe(data -> {
                    try{
                        Thread.sleep(5L);
                    }catch (InterruptedException e){}
                        log.info("# onNext: {}", data);
                    },
                        error -> log.error("# onError"));
                Thread.sleep(2000L);

    }

    //BackPressure DROP 전략
    //버퍼가 가득 찰 경우 버퍼 밖에서 대기 중인 데이터 중에서 먼저 emit된 데이터부터 DROP 시킨다.
    @SneakyThrows
    public static void example8_3(){
       Flux
               .interval(Duration.ofMillis(1))
               .onBackpressureDrop(dropped -> log.info("# dropped: {}", dropped))
               .publishOn(Schedulers.parallel())
               .subscribe(data -> {
                   try{
                       Thread.sleep(5L);
                   } catch (InterruptedException e) {}
                   log.info("# onNext: {}", data);
               },
                       error -> log.info("#onError", error));
            Thread.sleep(2000L);

    }

    //BackPressure LATEST 전략
    //새로운 데이터가 들어오는 시점에 가장 최근의 데이터만 남겨 두고 나머지 데이터를 폐기합니다.
    @SneakyThrows
    public static void example8_4(){
        Flux
                .interval(Duration.ofMillis(1))
                .onBackpressureLatest()
                .publishOn(Schedulers.parallel())
                .subscribe(data -> {
                            try{
                                Thread.sleep(5L);
                            } catch (InterruptedException e) {}
                            log.info("# onNext: {}", data);
                        },
                        error -> log.info("#onError", error));
        Thread.sleep(2000L);

    }

    //BackPressure DROP_LATEST 전략
    //버퍼가 가득 찰 경우 가장 최근에 버퍼 안에 채워진 데이터를 DROP 하여 폐기하여 Buffer의 공간을 확보한다
    @SneakyThrows
    public static void example8_5(){
        Flux
                .interval(Duration.ofMillis(300L))
                .doOnNext(data -> log.info("# emitted by original Flux: {}", data))
                .onBackpressureBuffer(2,
                        dropped -> log.info("** Overflow & Dropped: {} **", dropped),
                        BufferOverflowStrategy.DROP_LATEST)
                .doOnNext(data -> log.info("[ # emitted by Buffer: {}]", data))
                .publishOn(Schedulers.parallel(), false, 1)
                .subscribe(data -> {
                            try{
                                Thread.sleep(1000L);
                            } catch (InterruptedException e) {}
                            log.info("# onNext: {}", data);
                        },
                        error -> log.info("#onError", error));
        Thread.sleep(3000L);

    }

    //BackPressure DROP_OLDEST 전략
    //버퍼가 가득 찰 경우 가장 오래된 데이터를 DROP 하여 폐기하여 Buffer의 공간을 확보한다
    @SneakyThrows
    public static void example8_6(){
        Flux
                .interval(Duration.ofMillis(300L))
                .doOnNext(data -> log.info("# emitted by original Flux: {}", data))
                .onBackpressureBuffer(2,
                        dropped -> log.info("** Overflow & Dropped: {} **", dropped),
                        BufferOverflowStrategy.DROP_OLDEST)
                .doOnNext(data -> log.info("[ # emitted by Buffer: {}]", data))
                .publishOn(Schedulers.parallel(), false, 1)
                .subscribe(data -> {
                            try{
                                Thread.sleep(1000L);
                            } catch (InterruptedException e) {}
                            log.info("# onNext: {}", data);
                        },
                        error -> log.info("#onError", error));
        Thread.sleep(3000L);

    }

}
