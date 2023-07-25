package part2;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class Example10 {
    public static void main(String[] args){
        //example10_1();
        //example10_2();
        //example10_3();
        //example10_4();
        //example10_5();
        //example10_6();
        //example10_7();
        //example10_8();
        //example10_9();
        //example10_10();
        example10_11();
    }

    /**
     *subscribeOn 구독이 발생한 직후 실행될 스레드를 지정하는 operator
     */
    @SneakyThrows
    public static void example10_1(){
        Flux.fromArray(new Integer[]{1,3,5,7})
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(data -> log.info("# doOnNext: {}", data))
                .doOnSubscribe(subscription -> log.info("# doOnSubscribe"))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    /**
     * publishOn() Operator는 Downstream으로 Signal을 전송할 때 실행되는 스레드르르 제어하는 역할을 하는 Operator라고 할 수 있다
     * publishOn()는 코드상에서 publishOn()을 기준으로 아래쪽인 Downstream의 실행 스레드를 변경한다.
     */
    @SneakyThrows
    public static void example10_2(){
        Flux.fromArray(new Integer[]{1,3,5,7})
                .doOnNext(data -> log.info("# doOnNext: {}", data))
                .doOnSubscribe(subscription -> log.info("# doOnSubscribe"))
                .publishOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    /**
     * parallel() Operator는 병렬성을 가지는 물리적인 스레드에 해당한다.
     * parallel()은 emit되는 데이터를 CPU의 논리적인 코어 수에 맞게 사전에 골고루 분배하는 역할만 하며, 실제로 병렬작업을 수행할 스레드의 할당은 runOn() Operator가 담당합니다.
     * runOn 라인을 주석 했을 경우 쓰레드가 할당 되지 않음
     */
    @SneakyThrows
    public static void example10_3(){
        Flux.fromArray(new Integer[]{1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39})
                        .parallel()
                                .runOn(Schedulers.parallel())
                                        .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    /**
     * 스레드를 전부 사용할 필요가 없을때 그 개수를 제한한다.
     */
    @SneakyThrows
    public static void example10_4(){
        Flux.fromArray(new Integer[]{1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39})
                .parallel(4)
                .runOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    /**
     * Scheulder를 추가하지 않았기 때문에 main 쓰레드에서만 사용된다.
     */
    @SneakyThrows
    public static void example10_5(){
        Flux.fromArray(new Integer[]{1,3,5,7})
                        .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
                        .filter(data -> data > 3)
                        .doOnNext(data -> log.info("# doOnNext filter: {}", data))
                        .map(data -> data * 10)
                        .doOnNext(data -> log.info("# doOnNext map: {}", data))
                        .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    /**
     * publishOn을 추가하면 지정한 해당 Scheduler유형의 스레드가 실행된다.
     */
    @SneakyThrows
    public static void example10_6(){
        Flux.fromArray(new Integer[]{1,3,5,7})
                .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
                .publishOn(Schedulers.parallel())
                .filter(data -> data > 3)
                .doOnNext(data -> log.info("# doOnNext filter: {}", data))
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# doOnNext map: {}", data))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    /**
     * publishOn()을 두 번 사용할 경우 Operator 체인에서 실행되는 스레드의 동작 과정을 보여 줍니다.
     *
     */
    @SneakyThrows
    public static void example10_7(){
        Flux.fromArray(new Integer[]{1,3,5,7})
                .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
                .publishOn(Schedulers.parallel())
                .filter(data -> data > 3)
                .doOnNext(data -> log.info("# doOnNext filter: {}", data))
                .publishOn(Schedulers.parallel())
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# doOnNext map: {}", data))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    /**
     * subscribeOn() Opertator는 구독이 발생한 직후에, 실행될 스레드를 지정하는 Operator
     * 그렇기 때문에 fromArray는 subscribeOn()에서 지정한 쓰레드로 사용된다
     */
    @SneakyThrows
    public static void example10_8(){
        Flux.fromArray(new Integer[]{1,3,5,7})
                .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
                .subscribeOn(Schedulers.boundedElastic())
                .filter(data -> data > 3)
                .doOnNext(data -> log.info("# doOnNext filter: {}", data))
                .publishOn(Schedulers.parallel())
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# doOnNext map: {}", data))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    /**
     * Schedulers.immediate는 별도의 스레드를 추가적으로 생성하지 않고 현재 스레드에서 작업을 처리하고자 할 때 사용할 수 있다
     * .publishOn(Schedulers.immediate())을 지우면 똑같은 쓰레드에 사용될 수 있는데 굳이 Schedulers.immediate()를 사용할 필요가 있을까라는 의문에서는
     * 해당 API가 공통의 역할을 하는 API이고 해당 API의 파라미터로 Scheduler를 전달 할 수 있다고 가정할 때 똑같은 쓰레드에서 실행 될 수 있게끔 사용할 수 있는게 immediate()이다
     */
    @SneakyThrows
    public static void example10_9(){
        Flux.fromArray(new Integer[]{1,3,5,7})
                .doOnNext(data -> log.info("# doOnNext fromArray: {}", data))
                .publishOn(Schedulers.parallel())
                .filter(data -> data > 3)
                .doOnNext(data -> log.info("# doOnNext filter: {}", data))
                .publishOn(Schedulers.immediate())
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# doOnNext map: {}", data))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    /**
     * single
     * 첫 번째 호출에서 이미 생성된 스레드를 재사용하게 되면서 다수의 작업을 처리할 있습니다.
     * 하나의 스레드로 다수의 작업을 처리해야 되므로 지여 시간이 짧은 작업을 처리하는 것이 효과적이다.
     */
    @SneakyThrows
    public static void example10_10(){
        doTaskSingle("task1").subscribe(data -> log.info("# onNext: {}", data));
        doTaskSingle("task2").subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    private static Flux<Integer> doTaskSingle(String taskName){
        return Flux.fromArray(new Integer[]{1,3,5,7})
                .publishOn(Schedulers.single())
                .filter(data -> data >3)
                .doOnNext(data -> log.info("# {} doOnNext filter", taskName, data))
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# {} doOnNext map : {}", taskName, data));
    }

    /**
     * Schedulers.newSingle()은 매번 새로운 스레드 하나를 생성합니다.
     * 두 번째 파라미터는 이 스레드를 데몬 스레드로 동잗ㄱ하게 할지 여부를 설정합니다.
     * 데몬 스레드는 보조 스레드라고 불리우며 주 스레드가 종료되면 자동으로 종료되는 특징이 있다.
     * main 스레드가 종료되면 자동으로 종료되도록 설정
     */
    @SneakyThrows
    public static void example10_11(){
        doTaskNewSingle("task1").subscribe(data -> log.info("# onNext: {}", data));
        doTaskNewSingle("task2").subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    private static Flux<Integer> doTaskNewSingle(String taskName){
        return Flux.fromArray(new Integer[]{1,3,5,7})
                .publishOn(Schedulers.newSingle("new-single", true))
                .filter(data -> data >3)
                .doOnNext(data -> log.info("# {} doOnNext filter", taskName, data))
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# {} doOnNext map : {}", taskName, data));
    }

}
