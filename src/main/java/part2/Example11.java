package part2;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import part2.dto.Book;

@Slf4j
public class Example11 {
    public static final String HEADER_AUTH_TOKEN = "authToKen";

    public static void main(String[] args) throws InterruptedException {
        //example11_1();
        //example11_3();
        //example11_4();
        //example11_5();
        //example11_6();
        //example11_7();
        example11_8();
    }

    /**
     * Context에 key/value 형태의 데이터를 저장할 수 있다는 의미는 Context에 데이터의 쓰기와 읽기가 가능하다는 의미이다.
     * contextWrite() Operator를 통해서 Context에 데이터를 쓰고 있다.
     * 원본 데이터 소스레벨에서 읽는 방식인데 deferContextual()을 사용하여 읽을 수 있다
     */
    @SneakyThrows
    public static void example11_1(){
        Mono
            .deferContextual(ctx ->
                Mono
                .just("Hello" + " " + ctx.get("firstName"))
                .doOnNext(data -> log.info("# just doOnNext: {}", data)))
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel())
                .transformDeferredContextual(
                        (mono, ctx) -> mono.map(data -> data + " " + ctx.get("lastName"))
                )
                .contextWrite(context -> context.put("lastName", "Jobs"))
                .contextWrite(context -> context.put("firstName", "Steve"))
                .subscribe(data -> log.info("# onNext : {}", data));

        Thread.sleep(100L);
    }

    @SneakyThrows
    public static void example11_3(){
        final String key1 = "company";
        final String key2 = "firstName";
        final String key3 = "lastName";

        Mono
                .deferContextual(ctx ->
                        Mono.just(ctx.get(key1) + ", "+ ctx.get(key2)+ " "+ctx.get(key3)))
                .publishOn(Schedulers.parallel())
                .contextWrite(context -> context.putAll(Context.of(key2, "Steve", key3, "Jobs").readOnly()))
                .contextWrite(context -> context.put(key1, "Apple"))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(200L);
    }


    public static void example11_4() throws InterruptedException {
        final String key1 = "company";
        final String key2 = "firstName";
        final String key3 = "lastName";

        Mono
                .deferContextual(ctx ->
                        Mono.just(ctx.get(key1) + ", " +
                                ctx.getOrEmpty(key2).orElse("no firstName")+ " "+
                                ctx.getOrDefault(key3, "no lastName"))
                )
                .publishOn(Schedulers.parallel())
                .contextWrite(context -> context.put(key1, "Apple"))
                .subscribe(data -> log.info("# onNext: {}", data));
        Thread.sleep(200L);
    }

    /**
     * 구독이 발생할 때마다 해당하는 하나의 Context가 하나의 구독에 연결된다.
     * @throws InterruptedException
     */
    public static void example11_5() throws InterruptedException {
        final String key1 = "company";
        Mono<String> mono = Mono.deferContextual(ctx -> Mono.just("Company: "+ " " + ctx.get(key1))
        )
                        .publishOn(Schedulers.parallel());

        mono.contextWrite(context -> context.put(key1, "Apple"))
                        .subscribe(data -> log.info("# subscribe1 onNext: {}", data));
        mono.contextWrite(context -> context.put(key1, "Microsoft"))
                .subscribe(data -> log.info("# subscribe2 onNext: {}", data));
        Thread.sleep(200L);
    }

    /**
     * Context의 경우 Operator체인상의 아래에서 위로 전파되는 특징이 존재한다.
     * @throws InterruptedException
     */
    public static void example11_6() throws InterruptedException {
        final String key1 = "company";
        final String key2 = "name";
        Mono.deferContextual(ctx -> Mono.just(ctx.get(key1)))
                .publishOn(Schedulers.parallel())
                .contextWrite(context -> context.put(key2, "Bill"))
                .transformDeferredContextual((mono, ctx) ->
                    mono.map(data -> data + ", "+ctx.getOrDefault(key2, "Steve"))
                )
                .contextWrite(context -> context.put(key1, "Apple"))
                .subscribe(data -> log.info("# onNext: {}", data));


        Thread.sleep(200L);
    }

    /**
     * Inner Sequence내부에서는 외부 Context에 저장된 데이터를 읽을 수 있다
     * Inner Sequnce 외부에서는 Inner Sequence 내부 Context에 저장된 데이터를 읽을 수 없다
     * flatMap() Opertaor 내부에 있는 Sequence를 Inner Sequence라고 한다
     * 주석 해제시 읽을 수 없다는 오류 메시지
     * @throws InterruptedException
     */
    public static void example11_7() throws InterruptedException {
        final String key1 = "company";

        Mono.just("Steve")
                       /* .transformDeferredContextual((stringMono, ctx)->
                                ctx.get("role"))*/
                                .flatMap(name ->
                                        Mono.deferContextual(ctx ->
                                                Mono.just(ctx.get(key1) + ", "+ name)
                                                        .transformDeferredContextual((mono, innerCtx) ->
                                                                mono.map(data -> data +", "+ innerCtx.get("role")))
                                                        .contextWrite(context -> context.put("role", "CEO"))))
                                .publishOn(Schedulers.parallel())
                                .contextWrite(context -> context.put(key1, "Apple"))
                                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(200L);
    }

    /**
     * Context를 실제로 어떻게 활용하는지 보여주는 예제 코드
     * 도서 정보인 Book을 전송하기 위해 postBook 메소드로 파라미터 전달
     * zip() Operator를 사용해 Mono<Book> 객체와 인증 토큰 정보를 의미하는 Mono<String> 객체를 하나의 Mono로 합쳐지면 해당 객체는 Mono<Tuple2>가 된다.
     * flatMap() Operator 내부에서 도서 정보를 전송한다.
     * 인증 정보 같은 독립성을 가지는 정보를 전송하는데 적합하다
     */
    public static void example11_8(){
        Mono<String> mono =
                postBook(Mono.just(
                        new Book("abcd-1111-3533-2809"
                        , "Reactor's Bible"
                        ,"Kevin"))
                        )
                        .contextWrite(Context.of(HEADER_AUTH_TOKEN, "eyJhbGci0i"));
        mono.subscribe(data -> log.info("# onNExt: {}", data));
    }
    private static Mono<String> postBook(Mono<Book> book){
        return Mono
                .zip(book,
                    Mono
                        .deferContextual(ctx ->
                            Mono.just(ctx.get(HEADER_AUTH_TOKEN)))
                        )
                        .flatMap(tuple -> {
                            String response = "POST the book(" + tuple.getT1().getBookName() + ", "+ tuple.getT1().getAuthor() + ") with token: "+ tuple.getT2();
                            return Mono.just(response);
                        });
    }


}
