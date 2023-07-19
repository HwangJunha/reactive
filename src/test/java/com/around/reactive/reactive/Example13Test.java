package com.around.reactive.reactive;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;

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

}
