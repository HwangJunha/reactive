package part2.example14;

import part2.dto.SampleData;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Example14Transformation {
    public static void main(String[] args) throws InterruptedException {
        //example14_27();
        //example14_28();
        //example14_29();
        //example14_30();
        //example14_31();
        //example14_32();
        //example14_33();
        //example14_34();
        //example14_35();
        //example14_36();
        //example14_37();
        //example14_38();
        //example14_39();
        //example14_40();
        example14_41();
    }

    /**
     * map() Operator는 Upstream에서 emit된 데이터를 mapper Function을 사용하여 변환한 후, Downstream으로 emit한다.
     * Upstream에서 emit된 문자열의 일부인 'Cicle'을 map() Operator 내부에서 replace을 통해서 변환한 후 Downstream으로 emit한다.
     */
    public static void example14_27(){
        Flux
                .just("1-Circle","2-Circle","3-Circle")
                .map(circle -> circle.replace("Circle", "Rectangle"))
                .subscribe(data -> log.info("# onNext: {}", data));
    }

    /**
     * 특정 연도의 BTC 최고가 시점의 수익률을 계산하는 예제 코드
     */
    public static void example14_28(){
        final double buyPrice = 50_000_000;
        Flux
                .fromIterable(SampleData.btcTopPricesPerYear)
                .filter(tuple -> tuple.getT1() == 2021)
                .doOnNext(data -> log.info("# doOnNext : {}", data))
                .map(tuple -> calculateProfitRate(buyPrice, tuple.getT2()))
                .subscribe(data -> log.info("# onNext: {}%", data));
    }

    private static double calculateProfitRate(final double buyPrice, Long topPrice){
        return (topPrice - buyPrice) / buyPrice * 100;
    }

    /**
     * flatMap() Operator 영역 위쪽의 Upstream에서 emit된 데이터 한 건이 Inner Sequence에서 여러 간의 데이터로 변환된다
     * Upstream에서 emit된 데이터는 이렇게 Inner Sequence에서 평탄화 작업을 거치면서 하나의 Sequence로 병합 되어 Downstream으로 emit된다
     */
    public static void example14_29(){
        Flux
                .just("Good", "Bad")
                .flatMap(feeling -> Flux
                        .just("Morning", "Afternoon", "Evening")
                        .map(time -> feeling + " " + time))
                .subscribe(log::info);
    }

    /**
     * flatMap()은 Operator를 사용하여 구구단을 출력하는 예제이지만 내부의 Inner Sequence에 Scheduler를 설정해서 비동기적으로 데이터를 emit 하면 실행결과와 같이 순서는 보장되지 않는다.
     * @throws InterruptedException
     */
    public static void example14_30() throws InterruptedException {
        Flux
                .range(2,8)
                .flatMap(dan -> Flux
                        .range(1, 9)
                        .publishOn(Schedulers.parallel())
                        .map(n -> dan + " *" + n +" = "+ dan*n))
                .subscribe(log::info);
        Thread.sleep(100L);
    }

    /**
     * concat() Operator는 파라미터로 입력되는 publisher의 Sequence를 연결해서 데이터를 순차적으로 emit한다
     * 특히 먼저 입력된 publisher의 Sequence가 종료될 때까지 나머지 Publisher의 Sequence는 subscribe되지 않고 대기하는 특성을 가진다.
     */
    public static void example14_31(){
        Flux
                .concat(Flux.just(1,2,3) , Flux.just(4,5))
                .subscribe(data -> log.info("# onNext: {}", data));
    }

    /**
     * 세 개의 Flux를 concat() Operator의 파라미터로 전달해서 유형별 코로나 백신의 재고 수량을 순차적으로 subscriber에게 전달한다
     */
    public static void example14_32(){
        Flux
                .concat(
                    Flux.fromIterable(getViralVector()),
                    Flux.fromIterable(getMRNA()),
                    Flux.fromIterable(getSubunit())
                )
                .subscribe(data -> log.info("# onNext: {}", data));
    }

    private static List<Tuple2<SampleData.CovidVaccine, Integer>> getViralVector(){
        return SampleData.viralVectorVaccines;
    }

    private static List<Tuple2<SampleData.CovidVaccine, Integer>> getMRNA(){
        return SampleData.mRNAVaccines;
    }

    private static List<Tuple2<SampleData.CovidVaccine, Integer>> getSubunit(){
        return SampleData.subunitVaccines;
    }

    /**
     * merge() Operator는 concat() Operator처럼 먼저 입력된 publisher의 Sequence가 종료될 때까지 나머지 publisher의 Sequence가 subscribe되지 않고 대기하는 것이 아니라
     * 모든 Publuisher의 Sequence가 즉시 subscriber됩니다.
     * @throws InterruptedException
     */
    public static void example14_33() throws InterruptedException {
        Flux
                .merge(
                        Flux.just(1,2,3,4).delayElements(Duration.ofMillis(300L)),
                        Flux.just(5,6,7).delayElements(Duration.ofMillis(500L))
                )
                .subscribe(data -> log.info("# onNext: {}", data));
        Thread.sleep(2000L);
    }

    /**
     * 해당 코드는 merge() Operator에 List<Mono<String>>을 전달해서 미국의 10개의 주에 위치한 원자력 발전소가 복구된 시간 순서대로 복구 메시지를 emit하는 상황을 시뮬레이션 했다.
     * @throws InterruptedException
     */
    public static void example14_34() throws InterruptedException {
        String[] usaStates = {
                "Ohio", "Michigan", "New Jersey", "Illinois", "New Hampshire",
                "Virginia", "Vermont", "North Carolina", "Ontario", "Georgia"
        };

        Flux
                .merge(getMeltDownRecoveryMessage(usaStates))
                .subscribe(log::info);
        Thread.sleep(2000L);
    }
    private static List<Mono<String>> getMeltDownRecoveryMessage(String[] usaStates){
        List<Mono<String>> messages = new ArrayList<>();
        for(String state : usaStates){
            messages.add(SampleData.nppMap.get(state));
        }
        return messages;
    }

    /**
     * zip() Operator는 파라미터로 입력되는 Publisher Sequnce에서 emit된 데이터를 결합하는데, 각 Publisher가 데이터를 하나씩 emit할 때까지 기다렸다가 결합한다.
     * @throws InterruptedException
     */
    public static void example14_35() throws InterruptedException {
        Flux
                .zip(
                        Flux.just(1,2,3).delayElements(Duration.ofMillis(300L)),
                        Flux.just(4,5,6).delayElements(Duration.ofMillis(500L))
                )
                .subscribe(tuple2 -> log.info("# onNExt: {}", tuple2));
        Thread.sleep(2500);
    }

    /**
     * Flux가 emit하는 데이터를 묶어서 subscriber에게 전달하는 것이 아니라 zip() Operator의 세 번째 파라미터로 combinator(BIFunction 함수형 인터페이스)를 추가해서
     * 두 개의 Flux가 emit하는 한 쌍의 데이터를 combinator에서 전달받아 변환 작업을 거친 후, 최종 변환된 데이터를 subscriber에게 전달한다.
     * @throws InterruptedException
     */
    public static void example14_36() throws InterruptedException {
        Flux
                .zip(
                        Flux.just(1,2,3).delayElements(Duration.ofMillis(300L)),
                        Flux.just(4,5,6).delayElements(Duration.ofMillis(500L)),
                        (n1, n2) -> n1*n2
                )
                .subscribe(tuple2 -> log.info("# onNExt: {}", tuple2));
        Thread.sleep(2500);
    }

    /**
     * 세 개 도시의 특정 시간 범위 내의 시간별 코로나 확직자 수를 출력한다
     */
    public static void example14_37(){
        getInfectedPersonsPerHour(10, 21)
                .subscribe(tuples ->{
                    Tuple3<Tuple2,Tuple2,Tuple2> t3 = (Tuple3) tuples;
                    int sum = (int)t3.getT1().getT2() + (int)t3.getT2().getT2() + (int)t3.getT3().getT2();
                    log.info("# onNext: {}, {}", t3.getT1().getT1(), sum);
                });
    }

    private static Flux getInfectedPersonsPerHour(int start, int end){
        return Flux.zip(
                Flux
                        .fromIterable(SampleData.seoulInfected)
                        .filter(t2 -> t2.getT1() >= start && t2.getT1() <= end),
                Flux
                        .fromIterable(SampleData.incheonInfected)
                        .filter(t2 -> t2.getT1() >= start && t2.getT1() <= end),
                Flux
                        .fromIterable(SampleData.suwonInfected)
                        .filter(t2 -> t2.getT1() >= start && t2.getT1() <= end)
        );
    }

    /**
     * and() Operator는 Mono Complete Signal과 파라미터로 입력된 Publisher의 Complete Signal을 결합하여 새로운 Mono<void>를 반환한다
     * subsceiber에게 onmComplete Signal만 전달되고 Upstream에서 emit된 데이터는 전달되지 않는다.
     * @throws InterruptedException
     */
    public static void example14_38() throws InterruptedException {
        Mono
                .just("Task 1")
                .delayElement(Duration.ofSeconds(1))
                .doOnNext(data -> log.info("# Mono doOnNext: {}", data))
                .and(
                        Flux
                                .just("Task 2", "Task 3")
                                .delayElements(Duration.ofMillis(600))
                                .doOnNext(data -> log.info("# Flux doOnNext: {}",data))
                )
                .subscribe(
                    data -> log.info("# onNext: {}", data),
                        error -> log.error("# onError:", error),
                        () -> log.info("# onComplete")
                );
        Thread.sleep(5000);
    }

    /**
     * 먼저 애플리케이션 서버가 2초 뒤에 재가동되었고, 4초 뒤에 DB서버가 재가동 되었다. 두 개의 서버가 성공적으로 재가동된 후, 두 개 서버의 재가동이 성공적으로 수행되었음을 이메일로 관리자에게 알리고 있다
     * 이처럼 and() Oeprator는 모든 작업이 끝난 시점에 최종적으로 후처리 작업을 수행하기 적합한 Operator이다.
     * @throws InterruptedException
     */
    public static void example14_39() throws InterruptedException {
        restartApplicationServer()
                .and(restartDBServer())
                .subscribe(
                        data -> log.info("# onNext: {}", data),
                        error -> log.error("# onError :", error),
                        () -> log.info("# sent an email to Administrator: " + "All Servers are restated successfully")
                );

        Thread.sleep(5500L);
    }

    private static Mono<String> restartApplicationServer(){
        return Mono
                .just("Application Server was restarted successfully.")
                .delayElement(Duration.ofSeconds(2))
                .doOnNext(log::info);
    }
    private static Publisher<String> restartDBServer(){
        return Mono
                .just("DB Server was restarted successfully.")
                .delayElement(Duration.ofSeconds(4))
                .doOnNext(log::info);
    }

    /**
     * collecList() Operator는 Flux에서 emit된 데이터를 모아서 List로 변환한 후 변환된 List를 emit하는 Mono를 반환한다.
     * 만약 Upstream Sequence가 비어있다면 비어 있는 List를 Downstream으로 emit한다
     */
    public static void example14_40(){
        Flux
                .just("...", "---", "...")
                .map(code -> transformMorseCode(code))
                .collectList()
                .subscribe(list -> log.info(list.stream().collect(Collectors.joining())));
    }
    public static String transformMorseCode(String morseCode){
        return SampleData.morseCodeMap.get(morseCode);
    }

    /**
     * collectMap() Operator는 Flux에서 emit된 데이터를 기반으로 key와 value를 생성하여 Map의 Element로 추가한 후 최종적으로 Map을 Downstream으로 emit한다
     */
    public static void example14_41(){
        Flux
                .range(0, 26)
                .collectMap(key -> SampleData.morseCodes[key], value -> transformToLetter(value))
                .subscribe(map -> log.info("# onNext: {}", map));

    }

    private static String transformToLetter(int value){
        return Character.toString((char)('a'+value));
    }
}
