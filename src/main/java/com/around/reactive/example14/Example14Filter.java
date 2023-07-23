package com.around.reactive.example14;

import com.around.reactive.dto.SampleData;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Map;

@Slf4j
public class Example14Filter {
    public static void main(String[] args) throws InterruptedException {
        //example14_15();
        //example14_16();
        //example14_17();
        //example14_18();
        //example14_19();
        //example14_20();
        //example14_21();
        //example14_22();
        //example14_23();
        //example14_24();
        //example14_25();
        example14_26();
    }

    /**
     * filter() Operator는 Upstream에서 emit된 데이터 중에서 조건에 일치하는 데이터만 Downstream으로 emit합니다.
     * 즉 filter() Operator의 파라미터로 입력 받은 predicate의 리턴 값이 true인 데이터만 Downstream으로 emit한다
     */
    public static void example14_15(){
        Flux
                .range(1, 20)
                .filter(num -> num%2 != 0)
                .subscribe(data -> log.info("# onNext: {}", data));
    }

    public static void example14_16(){
        Flux
                .fromIterable(SampleData.btcTopPricesPerYear)
                .filter(tuple -> tuple.getT2() > 20_000_000)
                .subscribe(data -> log.info(data.getT1() + ":"+data.getT2()));
    }

    /**
     * filterWhen은 비동기적으로 필터링을 수행하는 Operator이다.
     * 1. fromIterable() Operator로 코로나 백신명을 emit한다
     * 2. fitlerWhen() Operator에서 코로나 백신명을 전달받는다
     * 3. fliterWhen() Operator의 Inner Sequence를 통해 백신명에 해당하는 백신의 수량이 30000000개 이상이라면 해당 백신명을 Subscriber에게 emit한다
     * @throws InterruptedException
     */
    public static void example14_17() throws InterruptedException {
        Map<SampleData.CovidVaccine, Tuple2<SampleData.CovidVaccine, Integer>> vaccineMap = SampleData.getCovidVaccines();

        Flux
                .fromIterable(SampleData.coronaVaccineNames)
                .filterWhen(vaccine -> Mono
                        .just(vaccineMap.get(vaccine).getT2() >= 3_000_000)
                        .publishOn(Schedulers.parallel()))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(2000L);
    }

    /**
     * skip() Operator는 Upstream 에서 emit된 데이터 중에서 파라미터로 입력받은 숫자만큼 건너뛴 후, 나머지 데이터를 Downstream 으로 emit합니다.
     * @throws InterruptedException
     */
    public static void example14_18() throws InterruptedException {
        Flux
                .interval(Duration.ofSeconds(1))
                .skip(2)
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(5500L);
    }

    /**
     * skip() Operator의 파라미터로 1초의 시간을 지정했기 때문에 interval() Operator에서 1초의 시간 내에ㅓ emit되는 데이터는 모두 건너뛰게 된다.
     * @throws InterruptedException
     */
    public static void example14_19() throws InterruptedException{
        Flux
                .interval(Duration.ofMillis(300))
                .skip(Duration.ofSeconds(1))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(2000L);
    }

    /**
     * 연도별 2000만원 이상이ㅓㅇㅆ던 연도 중 오래된 연도 2년의 최고가를 건너뛰기를 한다
     */
    public static void example14_20(){
        Flux
                .fromIterable(SampleData.btcTopPricesPerYear)
                .filter(tuple -> tuple.getT2() >= 20_000_000)
                .skip(2)
                .subscribe(tuple -> log.info("{}, {}", tuple.getT1(), tuple.getT2()));
    }

    /**
     * Interval() Operator에서 0부터 1 증가한 숫자를 무한히 emit한다 하지만 take() Operator으 ㅣ파라미터로 3을 지정했기 때문에 세 개의 데이터만 subscriber에게 전달된다.
     * @throws InterruptedException
     */
    public static void example14_21() throws InterruptedException {
        Flux
                .interval(Duration.ofSeconds(1))
                .take(3)
                .subscribe(data -> log.info("# onNext: {}", data));
        Thread.sleep(4000L);
    }

    /**
     * take() Operator으 ㅣ파라미터로 2.54초의 시간을 지정했기 떄문에 interval() Operator에서 2.5초 시간 내에 emit되는 데이터만 Downsteream으로 emit한다.
     * @throws InterruptedException
     */
    public static void example14_22() throws InterruptedException {
        Flux
                .interval(Duration.ofSeconds(1))
                .take(Duration.ofMillis(2500))
                .subscribe((data -> log.info("# onNExt: {}", data)));
        Thread.sleep(3000L);
    }

    /**
     * takeLast() Operator는 Upstream에서 emit된 데이터 중에서 파라미터로 입력한 개수만크 가장 마지막에 emit된 데이터를 Downstream으로 emit한다.
     * @throws InterruptedException
     */
    public static void example14_23(){
        Flux
                .fromIterable(SampleData.btcTopPricesPerYear)
                .takeLast(2)
                .subscribe(tuple -> log.info("# onNext: {}, {}", tuple.getT1(), tuple.getT2()));
    }

    /**
     * takeUntill() Operator는 파라미터로 입력한 람다 표현식이 true가 될때까지 Upstream에서 emit된 데이터를 Downstream으로 emit한다
     * Upstream에서 emit된 데이터에는 predicate을 평가할 때 사용한 데이터가 포함된다다
     */
    public static void example14_24(){
        Flux
                .fromIterable(SampleData.btcTopPricesPerYear)
                .takeUntil(tuple -> tuple.getT2() > 20_000_000)
                .subscribe(tuple -> log.info("# onNext: {}, {}", tuple.getT1(), tuple.getT2()));
    }

    /**
     * takeWhile() Operator는 takeUntill() Opertaor와 달리 파라미터로 입력한 람다 표현식이 true가 되는 동안에만 Upstream에서 emit된 데이터를 DownStream으로 emit한다
     * Predicate을 평가할때 사용한 데이터가 Downsteream으로 emit되지 않는다.
     */
    public static void example14_25(){
        Flux
                .fromIterable(SampleData.btcTopPricesPerYear)
                .takeWhile(tuple -> tuple.getT2() < 20_000_000)
                .subscribe(tuple -> log.info("# onNExt: {}, {}", tuple.getT1(), tuple.getT2()));
    }

    /**
     * next() Operator를 사용하여 연도별 BTC 최고가 데이터 중에서 가장 첫 해의 최고가를 출력하는 예제
     */
    public static void example14_26(){
        Flux
                .fromIterable(SampleData.btcTopPricesPerYear)
                .next()
                .subscribe(tuple -> log.info("# onNext: {}, {}", tuple.getT1(), tuple.getT2()));

    }


}
