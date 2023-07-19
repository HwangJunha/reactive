package com.around.reactive.reactive;

import org.junit.jupiter.api.Test;
import org.springframework.util.Base64Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import reactor.test.publisher.PublisherProbe;
import reactor.test.publisher.TestPublisher;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class Example13Test {
    /**
     * create을 통한 테스트 대상 Sequence를 생성
     * expectXXXX()을 통해 Sequence에서 예상되는 Signal의 기댓값을 평가합니다.
     */
    @Test
    public void sayHelloReactorTest(){
        StepVerifier
                .create(Mono.just("Hello Reactor")) // 테스트 대상 Sequence 생성
                .expectNext("Hello Reactor") // emit된 데이터 기대값 평가
                .expectComplete() //onComplete Signal 기대값 평가
                .verify(); //검증 실행
    }

    /**
     * as()메소드는 이전 기댓값 평가 단계에 대한 설명을 추가 할 수 있습니다. 만약 테스트에 실패하게 되면 실패한 단계에 해당하는 설명이 로그에 출력됩니다.
     */
    @Test
    public void sayHelloTest(){
        StepVerifier
                .create(GeneralTestExample.sayHello())
                .expectSubscription()
                .as("# expect subscription")
                .expectNext("Hi") //해당 부분을 Hello로 변경하면 테스트 성공
                .as("# expect Hi")
                .expectNext("Reactor")
                .as("# expect Reactor")
                .verifyComplete();
    }

    /**
     * 4개의 데이터는 expectNext을 통해 기댓값 평가에 성공하지만 마지막에 emit된 데이터는 2가 아닌 0으로 나누는 작업을 했기에 Arithemetic Exception이 발생
     * expectError 즉 에러를 기대했기 때문에 테스트는 통과 됨
     * 마찬가지로 1,2,3,4의 주석을 해제하고 나머지 expectNext를 주석을 해제해도 테스트는 통과 됨
     */
    @Test
    public void divideByTwoTest(){
        Flux<Integer> source = Flux.just(2,4,6,8,10);
        StepVerifier
                .create(GeneralTestExample.divideByTwo(source))
                .expectSubscription()
                .expectNext(1)
                .expectNext(2)
                .expectNext(3)
                .expectNext(4)
                //.expectNext(1,2,3,4)
                .expectError()
                .verify();
    }

    @Test
    public void takeNumberTest(){
        Flux<Integer> source = Flux.range(0, 1000);
        StepVerifier
                .create(GeneralTestExample.takeNumber(source, 500), StepVerifierOptions.create().scenarioName("Verifyfrom 0 to 499"))
                .expectSubscription() //구독이 발생했음을 기대
                .expectNext(0) //숫자 0이 emit되었음을 기대
                .expectNextCount(498) //498개의 숫자가 emit되었음을 기대
                .expectNext(500) //500이 emit 되었음을 기대 *499로 바뀌면 테스트 통과
                .expectComplete() //onComplete Signal이 전송됨을 기대
                .verify();
    }

    /**
     * 현재 시점에서 1시간 뒤에 COVID-19확진자 발생 현황을 체크하고자 하는데 테스트 대상 메서드의 Sequence 1시간을 기다려야 알 수 있기 때문에 이는 매우 비효울적인 테스트가 된다
     * withVirtualTime() 메소드는 VirtualTimeScheduler라는 가상 스케줄러의 제어를 받도록 해 줍니다. 따라서 구독에 대한 기댓값을 평가하고난 후 then() 메서드를 사용해서 후속 작어ㅓㅂ을 할 수 있도록 하는데 여기서는 VirtualTimeScheduler의 advanceTimeBy()를 이용해 시간을 1시간 당기는 작업을 수행한다
     *
     */
    @Test
    public void getCOVID19CountTest1(){
        StepVerifier
                .withVirtualTime(() -> TimeBasedTestExample.getCOVID19Count(Flux.interval(Duration.ofHours(1)).take(1)
                    )
                )
                .expectSubscription()
                .then(() -> VirtualTimeScheduler
                        .get()
                        .advanceTimeBy(Duration.ofHours(1)))
                .expectNextCount(11)
                .expectComplete()
                .verify();
    }

    /**
     * 메소드의 기댓값을 평사하는 데 걸리는 시간을 제한한다
     * 지정한 시간 내에 테스트 대상 메서드의 작업이 종료되었는지 확인한다
     * verify() 메서드에 3초의 시간을 지정했습니다. 이는 3초 내에 기댓값의 평가가 3초 이내에 이루어지기를 원하지만 1분 뒤에 데이터를 emit하도록 설정했기 때문에 시간 초과 에러가 발생한다
     * 값을 같게 설치하기 보다는 verify의 값을 interval시간 보다 더 길게 잡으면 성공한다
     */
    @Test
    public void getCOVID19CountTest2(){
        StepVerifier
                .create(TimeBasedTestExample.getCOVID19Count(
                        Flux.interval(Duration.ofMinutes(1)).take(1)
                )
                )
                .expectSubscription()
                .expectNextCount(11)
                .expectComplete()
                .verify(Duration.ofSeconds(3));
    }

    @Test
    public void getCOVID19CountTest3(){
        StepVerifier
                .withVirtualTime(() -> TimeBasedTestExample.getVoteCount(
                        Flux.interval(Duration.ofMinutes(1))
                ))
                .expectSubscription()
                .expectNoEvent(Duration.ofMinutes(1))
                .expectNoEvent(Duration.ofMinutes(1))
                .expectNoEvent(Duration.ofMinutes(1))
                .expectNoEvent(Duration.ofMinutes(1))
                .expectNoEvent(Duration.ofMinutes(1))
                .expectNextCount(5)
                .expectComplete()
                .verify();
    }


    /**
     * generateNumber 메소드는 한 번에 100개의 숫자 데이터를 emiot하는데
     * StepVerifer으 create 메소드에서 데이터의 요청 개수를 1로 지정해서 오버플로가 발생했기 때문에 테스트 결과는 fail이다
     */
    @Test
    public void generateNumberTest1(){
        StepVerifier
                .create(generateNumber(), 1L) //해당 부분에서 100으로 설정하면 테스트 통과
                .thenConsumeWhile(num -> num >= 1)
                .verifyComplete();
    }

    /**
     * expectError를 통해 에러를 기대하며 오버플로우로 인해 내부적으로 Drop된 데이터가 있음을 Assertion 한다
     * verifyThenAssertThat() 메소드를 사용하면 검증을 트리거하고 난 후, 추가적인 Assertion을 할 수 있다
     * hasDroppedElements() 메소드를 이용해서 Drop된 데이터가 있음을 Assertion한다
     * Assertion : 개발자가 의도한 대로 해당 기능이 실행되는지를 검증하는 것을 의미
     */
    @Test
    public void generateNumberTest2(){
        StepVerifier
                .create(generateNumber(), 1L) //해당 부분에서 100으로 설정하면 테스트 통과
                .thenConsumeWhile(num -> num >= 1)
                .expectError()
                .verifyThenAssertThat()
                .hasDroppedElements();
    }


    /**
     * create() Operator 내부에서 for문을 이요ㅗㅇ해 프로그래밍 방식으로 100개의 숫자를 emit하고 있으며, Backpressure 전략으로 ERROR 전략을 지정했기 때문에 오버플로가 발생하면
     * OverflowException이 발생할 것이다
     * @return
     */
    private Flux<Integer> generateNumber(){
        return Flux.create(emitter -> {
            for(int i=1; i<= 100; i++){
                emitter.next(i);
            }
            emitter.complete();
        }, FluxSink.OverflowStrategy.ERROR);
    }

    /**
     * expectSubscription()으로 구독이 발생함을 기대함
     * expectAccessibleContext()로 구독 이후 Context가 전파됨을 기대함
     * hasKey()로 전파된 Context에 secretKey에 해당하는 값이 있음을 기대함
     * hasKey로 전파된 context에 secretMessage 키에 해당하는 값이 있음을 기대함
     * then 메소드르로 Sequence 다음 Signal이 이벤트의 기댓값을 평가 할 수 있다
     * expectNext("Hello, Reactor") 해당 문자열이 emit되었음을 기대한다
     * expectComplete()으로 onComplete Signal이 전송됨을 기대함
     */
    @Test
    public void getSecretMessageTest(){
        Mono<String> source = Mono.just("hello");

        StepVerifier
                .create(
                        getSecretMessage(source)
                                .contextWrite(context -> context.put("secretMessage", "Hello, Reactor"))
                                .contextWrite(context -> context.put("secretKey", "aGVsbG8"))
                )
                .expectSubscription()
                .expectAccessibleContext()
                .hasKey("secretKey")
                .hasKey("secretMessage")
                .then()
                .expectNext("Hello, Reactor")
                .expectComplete()
                .verify();

    }

    /**
     * Context를 테스트 하기 위한 메소드
     * 두 개의 데이터가 저장되었는데 하나는 Base64 형식의 인코딩된 secret key가 저장되고 또 하나는 secret key에 해당하는 decret message가 저장되어 있다.
     * @param keySource
     * @return
     */
    private Mono<String> getSecretMessage(Mono<String> keySource){
        return keySource
                .zipWith(
                        Mono.deferContextual(ctx ->
                                Mono.just((String)ctx.get("secretKey"))))
                .filter(tp ->
                        tp.getT1().equals(new String(Base64Utils.decodeFromString(tp.getT2()))))
                .transformDeferredContextual(
                        (mono, ctx) -> mono.map(notUse -> ctx.get("secretMessage"))
                );
    }

    /**
     * expectSubscription 구독이 발생함을 기대함
     *  recordWith로 emit된 데이터를 기록
     *  thenConsumeWhile로 파라마터로 전달한 predicate와 일치하는 데이터는 다음 단계에서 소비 할 수 있도록 함
     *  consumeRecordedWith 컬렉션에 기록된 데이터를 소비한다
     *  recordWith을 사용하면 emit되는 데이터에 대한 세밀한 테스트가 가능
     */
    @Test
    public void getCityTest(){
        StepVerifier
                .create(getCapitalizedCountry(
                        Flux.just("korea", "england", "canada", "india")
                ))
                .expectSubscription()
                .recordWith(ArrayList::new)
                .thenConsumeWhile(country -> !country.isEmpty())
                .consumeRecordedWith(countries -> {
                    assertThat(
                            countries
                                    .stream()
                                    .allMatch(country ->
                                            Character.isUpperCase(country.charAt(0))),
                            is(true)
                    );
                })
                .expectComplete()
                .verify();

    }

    /**
     * expectRecordedMatches 메소드 내에서 predicate을 사용하여 컬렉션에 기록된 모든 데이터의 첫 글자가 대문자임을 기대한다
     * getCityTest와 테스트 결과는 같지만 expectRecordedMatches 코드가 간결해 졌다
     * 하지만 consumeRecordedWith()는 기록된 데이터를 소비하면서 조금 더 다양한 조건으로 테스트를 진행 할 수 있으므로 필요에 따라 사용하면 된다
     */
    @Test
    public void getCountryTest(){
        StepVerifier
                .create(getCapitalizedCountry(
                        Flux.just("korea", "england", "canada", "india")
                ))
                .expectSubscription()
                .recordWith(ArrayList::new)
                .thenConsumeWhile(country -> !country.isEmpty())
                .expectRecordedMatches(countries ->
                    countries
                            .stream()
                            .allMatch(country ->
                                    Character.isUpperCase(country.charAt(0)))
                )
                .expectComplete()
                .verify();

    }

    /**
     * Record 기반 테스트를 위한 대상 메소드
     * 알파벳으로 된 국가명을 전달받아서 첫 글자를 대문자로 변환하도록 정의된 Flux를 리턴한다
     * @param source
     * @return
     */
    private Flux<String> getCapitalizedCountry(Flux<String> source){
        return source
                .map(country ->
                        country.substring(0, 1).toUpperCase() + country.substring(1));
    }

    /**
     * TestPublisher.create로 생성
     * 테스트 대상 클래스에 파라미터로 Flux를 전달하기 위해 flux() 메소드를 이용하여 Flux로 변환한다
     * 마지막으로 emit() 메소드를 사용해서 테스트에 필요한 데이터를 emit한다
     * 복잡한 로직이 포함된 대상 메소드를 테스트하거나 조건에 따라서 Signal를 변경할때 용이하다
     */
    @Test
    public void divideByTwoTestPublisher(){
        TestPublisher<Integer> source = TestPublisher.create();

        StepVerifier
                .create(GeneralTestExample.divideByTwo(source.flux()))
                .expectSubscription()
                .then(() -> source.emit(2,4,6,8,10))
                .expectNext(1,2,3,4)
                .expectError()
                .verify();
    }

    /**
     * TestPublisher 로서 동작하도록 ALLOW_NULL 위반 조건을 지정하여 데이터의 값이 null이라도 정상 동작하는 TestPublisher 를 생성합니다.
     *
     */
    @Test
    public void divideByTwoTestPublisher2(){
        //TestPublisher<Integer> source = TestPublisher.create();
        TestPublisher<Integer> source =
                TestPublisher.createNoncompliant(TestPublisher.Violation.ALLOW_NULL);

        StepVerifier
                .create(GeneralTestExample.divideByTwo(source.flux()))
                .expectSubscription()
                .then(() -> {
                    getDataSource().stream()
                            .forEach(data -> source.next(data));
                    source.complete();
                })
                .expectNext(1,2,3,4,5)
                .expectComplete()
                .verify();

    }

    private List<Integer> getDataSource(){
        return Arrays.asList(2,4,6,8, null);
    }


    /**
     * 먼저 실행 경로를 테스트할 테스트 대상 of로 메소드를 매핑한다.
     * probe.mono에서 리턴된 Mono 객체를 processTask() 메소드의 두번재 파라미터로 전달한다
     * 이제 StepVerifier를 이용해서 processTask 메소드가 데이터를 emit하고 정상적으로 종료되는지 테스트 한다
     * switchIfEmpty Operator로 인해 Sequence가 분기되는 상황에서 실제로 어느 Publisher가 동작하는지 해당 Publisher의 실행 경로를 테스트한다
     * assertWasSubscribed, assertWasRequested, assertWasNotCancelled 메소드를 통해 기대하는 publisher가 구독을 했는지 요청을 했지는 중간에 취소가 되지 않았는지를 Assertion함으로써 publisher의  실행 경로를 테스트한다
     *
     */
    @Test
    public void publisherProbeTest(){
        PublisherProbe<String> probe =
                PublisherProbe.of(supplyStandbyPower());

        StepVerifier
                .create(processTask(
                        supplyMainPower(),
                        probe.mono())
                )
                .expectNextCount(1)
                .verifyComplete();

        probe.assertWasSubscribed();
        probe.assertWasRequested();
        probe.assertWasNotCancelled();
    }

    private Mono<String> processTask(Mono<String> main, Mono<String> standby){
        return main.flatMap(message -> Mono.just(message))
                .switchIfEmpty(standby);
    }

    private Mono<String> supplyMainPower(){
        return Mono.empty();
    }

    private Mono supplyStandbyPower(){
        return Mono.just("# supply Standby Power");
    }




}
