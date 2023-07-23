package com.around.reactive.example14;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

@Slf4j
public class Example14Peek {
    public static void main(String[] args){
        example14_42();
    }


    /**
     * Reactor에서는 Upstream publisher에서 emit되는 데이터의 변경 없이 부수 효과만을 수행하기 위한 Operat들이 있는데 doOnXXXX()으로 시작하는 Operator가 바로 그러한 역할의 Operator이다
     * doOnXXXX은 Operator는 Consumer 또는 Runnable타입의 함수형 인터페이스를 파라미터로 가지기 때문에 별도의 리턴 값이 없다
     * 부수 효과: 함수형 프로그래밍에서는 외부의 상태를 변경하거나 함수로 전달되는 파라미터의 상태가 변경되는 것 역시 부수 효과가 있다고 말한다
     * 또한 함수가 값을 리턴하지 않는 void 형이면 어떤 식으로든 다른 일을 할 수밖에 없기 때문에 리턴 값이 없는 함수는 부수 효과가 있다고 생각해도 무방하다.
     * 1. doOnSubscribe() Publisher가 구독 중일 때 트리거되는 동작을 추가 할 수 있다
     * 2. doOnRequest() Pubilsher가 요청을 수신할 때 트리거되는 동작을 추가 할 수 있다
     * 3. doOnNext() Publisher가 데이터를 emit할 때 트리거되는 동작을 추가 할 수 있다
     * 4. doOnComplete() Publisher가 성공적으로 완료되었을때 트리거되는 동작을 추가할  수 있다
     * 5. doOnError Publisher가 에러가 발생한 상태로 종료되었을 때 트리거되는 동작울 추가할 수 있다
     * 6. doOnCancel() Publisher가 취소되었을 때 트리거되는 동작을 추가 할 수 있다
     * 7. doOnTerminate() Publisher가가 성공적으로 완료되었을 때 또는 에러가 발생한 상태로 종료되었을 때 트리거되는 동작을 추가 할 수 있다
     * 8. doOnDiscard() Upstream에 있는 전체 Operator 체인의 동작 중에서 Operator에 의해 폐기되는 요소를 조건부로 정리할 수 있다
     * 9. doAfterTerminate Downstream을 성공적으로 완료한 직후 또는 에러가 발생하여 Publisher가 종료된 직후에 트리거되는 동작을 추가 할 수 있다.
     * 10. doFilrst() Publisher가 구독되기 전에 트리거 되는 동작을 추가할 수 있다
     * 11. doFinally() 에러를 포함해서 어떤 이유이든 간에 Publisher가 종료된 트리거되는 동작을 추가할 수 있다
     *
     * doFirst() Operator는 Operator의 위치와 상관없이 제일 먼저 동작
     * doFinally() Operator는 Operator의 위치와 상관없이 제일 마지막에 동작
     * 구독이 발생하면 doOnsubscribe() Operator가 동작하고 subscriber 요청이 있을 때 doOnRequest() Operator가 동작하며 Upstream에서 데이터가 emit될 때마다 doOnNext() Oeprator가 동작한다
     * doFinally()이를 여러번 선언하면 선언한 시점의 역순으로 동작한다
     */
    public static void example14_42(){
        Flux
                .range(1,5)
                .doFinally(signalType -> log.info("# doFinally 1: {}", signalType))
                .doFinally(signalType -> log.info("# doFinally 2: {}", signalType))
                .doOnNext(data -> log.info("range > doOnNext:{}:", data))
                .doOnRequest(data -> log.info("#doOnRequesrt: {}", data))
                .doOnSubscribe(subscription -> log.info("doOnSubscribe 1"))
                .doFirst(() -> log.info("# doFirst()"))
                .filter(num -> num%2 ==1)
                .doOnNext(data ->log.info("# filter > doOnNext(): {}", data))
                .doOnComplete(() -> log.info("# doOnComplete()"))
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        super.hookOnSubscribe(subscription);
                        request(1);
                    }

                    @Override
                    protected void hookOnNext(Integer value){
                        log.info("# hookOnNext: {}", value);
                        request(1);
                    }
                });

    }

}
