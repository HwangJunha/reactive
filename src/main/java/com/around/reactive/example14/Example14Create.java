package com.around.reactive.example14;

import com.around.reactive.dto.CryptoCurrencyPriceEmitter;
import com.around.reactive.dto.CryptoCurrencyPriceListener;
import com.around.reactive.dto.SampleData;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class Example14Create {
    public static void main(String[] args) throws InterruptedException {
        //example14_1();
        //example14_2();
        //example14_3();
        //example14_4();
        //example14_5();
        //example14_6();
        //example14_7();
        //example14_8();
        //example14_9();
        //example14_10();
        //example14_11();
        //example14_12();
        //example14_13();
        example14_14();
    }

    /**
     * justOrEmpty()
     * justOrEmpty()에 null을 전달하고 실행 결과에 NullPointException 없이 onComplete signal 만 전송
     */
    public static void example14_1(){
        Mono
                .justOrEmpty(null)
                .subscribe(data ->{},
                        error -> {},
                        () -> log.info("# onComplete"));
    }

    /**
     * fromIterable()
     * fromIterable() Operator는 Iterable에 포함된 데이터를 emit하는 Fluix를 생성한다.
     * java에서 제공하는 Iterable을 구현한 구현체를 fromIterable()의 파라미터로 전달한다
     */
    public static void example14_2(){
        Flux
                .fromIterable(SampleData.coins)
                .subscribe(coin ->
                        log.info("coin 명 : {}, 현재가: {}", coin.getT1(), coin.getT2()));
    }

    /**
     * fromStream() Operator는 Stream에 포함된 데이터를 emit하는 Flux를 생성합니다. Java의 Stream 특성상 Stream은 재사용할 수 없으며 cancel, error, complete 자동으로 닫힘
     */
    public static void example14_3(){
        Flux
                .fromStream(() -> SampleData.coinNames.stream())
                .filter(coin -> coin.equals("BTC") || coin.equals("ETH"))
                .subscribe(data -> log.info("{}", data));
    }

    /**
     * range() Operator는 명령어 언어의 for문처럼 특정 횟수만큼 어떤 작업을 처리하고자 할 경우에 사용
     */
    public static void example14_4(){
        Flux
                .range(5, 10)
                .subscribe(data -> log.info("{}", data));
    }

    /**
     * range() Operator에서 7부터 5개의 숫자를 emit하기 때문에 map() OIperator에서는 emit된 숫자를 btcTopPricesPerYear 리스트의 index로 사용하여 index에 해당하는 연도별 최고가 금액 정보를 가지는 튜플을 subscriber에게 전달
     */
    public static void example14_5(){
        Flux
                .range(7,5)
                .map(idx -> SampleData.btcTopPricesPerYear.get(idx))
                .subscribe(tuple -> log.info("{}'s {}", tuple.getT1(), tuple.getT2()));
    }

    /**
     * defer() Operator는 선언한 시점에 데이터를 emit하는 것이 아니라 구독하는 시점에 데이터를 emit한다
     * @throws InterruptedException
     */
    public static void example14_6() throws InterruptedException {
        log.info("# start : {]", LocalDateTime.now());
        Mono<LocalDateTime> justMono = Mono.just(LocalDateTime.now());
        Mono<LocalDateTime> deferMono = Mono.defer( () -> Mono.just(LocalDateTime.now()));

        Thread.sleep(2000);

        justMono.subscribe(data -> log.info("# onNext just1: {}", data));
        deferMono.subscribe(data -> log.info("# onNext defer1: {}", data));

        Thread.sleep(2000);
        justMono.subscribe(data -> log.info("# onNext just2: {}", data));
        deferMono.subscribe(data -> log.info("#onNext defer2: {}", data));
    }

    /**
     * 해당 부분에서 Upstream에서 emit되는 데이터가 없으면 switchIfEmpty() 내부에서 sayDefault() 메소드를 호출하도록 되어있다.
     * UpStream쪽에서 "Hello" 문자열이 emit되기 때문에 sayDefault() 메소드가 호출되지 않을 것 같지만 코드를 실행하면 sayDefault  메소드가 호출된다
     * 해당 부분을 주석처리하고 .switchIfEmpty(Mono.defer(() -> sayDefault())) 하면 의도대로 실행된다.
     * @throws InterruptedException
     */
    public static void example14_7() throws InterruptedException{
        log.info("# start : {]", LocalDateTime.now());
        Mono
                .just("Hello")
                .delayElement(Duration.ofSeconds(3))
                .switchIfEmpty(sayDefault())
//                .switchIfEmpty(Mono.defer(() -> sayDefault()))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(3500);
    }

    private static Mono<String> sayDefault(){
        log.info("# Say Hi");
        return Mono.just("Hi");
    }

    /**
     * using() Operator는 파라미터로 전달받은 resouce를 emit하는 Flux를 생성한다
     * 첫 번째 파라미터는 일어 올 resouce, 두 번째는 resouce을 emit 하는 Flux 객체, 세 번째는 종료 Signal(onComplete 또는 onError)가 발생할 경우
     */
    public static void example14_8(){
        Path path = Paths.get("D:\\resources\\using_example.txt");

        Flux
                .using(() -> Files.lines(path), Flux::fromStream, Stream::close)
                .subscribe(log::info);
    }

    /**
     * generator() Operator는 프로그래밍 방식으로 Signal 이벤트를 발생시키며, 특히 동기적으로 데이터를 하나씩 순차적으로 emit하고자 할 경우 사용됩니다.
     * generator의 첫 번째 파라미터에서 초기값을 0으로 지정했으며, 두 버째 파라미터에서는 synchronousSink객체로 상태 값을 emit한다
     * SynchronousSink는 하나의 Signal만 동기적으로 발생시킬 수 있으며 최대 하나의 상태 값만 emit하는 인터페이스이다
     * 10일 경우 onComplete 발생
     */
    public static void example14_9(){
        Flux
                .generate(() -> 0, (state, sink) ->{
                    sink.next((state));
                    if(state == 10)
                        sink.complete();
                    return ++state;
                })
                .subscribe(data -> log.info("# onNext: {}", data));
    }

    /**
     * Tuples객체를 상태 값으로 사용하였으며 generate()의 첫 번째 파라미터인 callable의 리턴 값과 두 번째 파라미터인 BiFunction의 리턴 값이 모두 Tuples 객체이다.
     *
     */
    public static void example14_10(){
        final int dan = 3;
        Flux
                .generate( () -> Tuples.of(dan, 1), (state, sink) ->{
                    sink.next(state.getT1() + "*" + state.getT2() + "=" +state.getT1() * state.getT2());
                    if(state.getT2() == 9)
                        sink.complete();
                    return Tuples.of(dan, state.getT2()+1);
                }, state -> log.info("# 구구단 {}단 종료", state.getT1()))
                .subscribe(data -> log.info("# onNext: {}", data));
    }

    /**
     * generator의 초기 상태 값을 2019로 지정해서 2019년도부터 1씩 증가한 연도별 BTC 최고가 금액을 map에서 읽어 온 후 emit 한다.
     */
    public static void example14_11(){
        Map<Integer, Tuple2<Integer, Long>> map =
                                                SampleData.getBtcTopPricesPerYearMap();

        Flux
                .generate(() -> 2019, (state, sink) -> {
                    if(state > 2021){
                        sink.complete();
                    }else{
                        sink.next(map.get(state));
                    }
                    return ++state;
                })
                .subscribe(data -> log.info("# onNext: {}", data));
    }

    static int SIZE = 0;
    static int COUNT = -1;
    final static List<Integer> DATA_SOURCE = Arrays.asList(1,2,3,4,5,6,7,8,9,10);

    /**
     * subscriber에서 요청한 경우에만 create() Operator애네엇 요청 개수만큼의 데이터를 emit한다
     * Subscriber가 직접 요청 개수를 지정하기 위해서 BaseSubScriberr를 사용했다
     * 1. 구독이 발생하면 hookOnSbuscribe 메소드 내부에서 request(2)를 호출하여 한 번의 2개의 데이터를 끌고 온다
     * 2. subscriber쪽에서 request() 메소드를 호출하면 create() Operatro내부에서 sink.onRequest메소드의 람다 표현식 실행
     * 3. subsscriber가 요청한 개수만큼 emit
     * 4. BaseSubScribver의 hookOnNext() 메소드 내부에ㅐ서 emit된 데이터를 로그,로 출력한 후, 다시 request(2)를 호출하여 두 개의 데이터를 요청
     * 5. 2에서 4의 과정이 반복되다가 dataSource List의 숫자를 모두 emit하면 onComplete Signal를 발생
     * 6. BaseSubscriber의 hookOnComplete() ㅂ메소드 내부에서 종료 로그를 실행
     * 7. sink.onDispose()의 람다 표현식이 실행되어 후 처리 된다.
     */
    public static void example14_12(){
        log.info("# start");
        Flux.create((FluxSink<Integer> sink) ->{
            sink.onRequest(n -> {
               try{
                   Thread.sleep(1000L);
                   for(int i=0; i<n; i++){
                       if(COUNT >= 9){
                           sink.complete();
                       }else{
                           COUNT++;
                           sink.next(DATA_SOURCE.get(COUNT));
                       }
                   }
               }catch (InterruptedException e){}
            });
            sink.onDispose(() -> log.info("#clean up"));
        }).subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(2);
            }

            @Override
            protected void hookOnNext(Integer value){
                SIZE++;
                log.info("# onNext: {}", value);
                if(SIZE == 2){
                    request(2);
                    SIZE = 0;
                }
            }

            @Override
            protected void hookOnComplete(){
                log.info("# onComplete");
            }
        });
    }

    /**
     * 가격 데이터를 suybscriber에게 비동기적으로 emit하는 상황을 시뮬레이션한 예제 코드
     * 1. 구독이 발생하면 create() Operator으 ㅣ파라미터인 람다 표션식ㄷ이 실행된다.
     * 2. 암호 화폐의 가격 변동이 발생하면 변동된 가격 데이터를 emit할 수 있도록 CryptoCurrencyPriceEmitter가 CryptoCurrencyPriceListener을 등록한다
     * 3. 암호 화폐의 가격 변동이 발생하기 전에 3초의 지연 시간을 가진다
     * 4. 암호 화폐의 가격 변동이 발생하는 것을 시뮬레이션하기 위해 flowInto() 메소드 호출
     * 5. 2초의 지연 시간을 가진 후 해당 암호 화폐의 데이터 처리를 종료
     */
    public static void example14_13() throws InterruptedException {
        CryptoCurrencyPriceEmitter cryptoCurrencyPriceEmitter = new CryptoCurrencyPriceEmitter();

        Flux.create((FluxSink<Integer> sink) -> {
            cryptoCurrencyPriceEmitter.setListener(new CryptoCurrencyPriceListener() {
                @Override
                public void onPrice(List<Integer> priceList) {
                    priceList.stream().forEach(price -> {
                        sink.next(price);
                    });
                }

                @Override
                public void onComplete(){
                    sink.complete();
                }
            });
        })
                .publishOn(Schedulers.parallel())
                .subscribe(
                        data -> log.info("# onNext: {}", data),
                        error -> {},
                        () -> log.info("# onComplete"));
        Thread.sleep(3000L);
        cryptoCurrencyPriceEmitter.flowInto();
        Thread.sleep(2000L);
        cryptoCurrencyPriceEmitter.complete();
    }

    static int start = 1;
    static int end = 4;

    /**
     * create() Operator의 Backpressure 전략을 적용
     * 1. 구독이 발생하면 publishOn에서 설정한 숫자만큼의 데이터를 요청한다
     * 2. create() Operator 내부에서 sink.onRequest() 메소드의 람ㅍ다 표현식이 실해행된다
     * 3. 요청한 개수보다 2개 더 많은 데이터를 emit한다
     * 4. Subscriber가 emit된 데이터를 전달받아 로그로 출력한다
     * 5. 이때 Backpressure DROP 전략이 적영되었으므로, 요청 개수를 초과하여 emit된 데이터는 DROP 된다.
     * 6. 다시 pushishOn()에서 지정한 숫자만큼의 데이터를 요청한다
     * 7. create() Operator 내부에서 onComplete Signal이 발생하지 않았기 떄문에 2에서 6의 과정을 반복하다가 설정한 지연 시간이 지나면 main 스레드가 종료되어 코드 실행이 종료된다.
     * @throws InterruptedException
     */
    public static void example14_14() throws InterruptedException {
        Flux.create((FluxSink<Integer> sink) -> {
            sink.onRequest(n -> {
                log.info("# requested: "+n);
                try{
                    Thread.sleep(500L);
                    for(int i= start; i<=end; i++){
                        sink.next(i);
                    }
                    start += 4;
                    end += 4;
                } catch (InterruptedException e) {}
            });
            sink.onDispose( () -> {
                log.info("# cleanup");
            });
        }, FluxSink.OverflowStrategy.DROP)
            .subscribeOn(Schedulers.boundedElastic())
            .publishOn(Schedulers.parallel(), 2)
            .subscribe(data -> log.info("# onNext: {}", data));
        Thread.sleep(3000L);
    }

}

