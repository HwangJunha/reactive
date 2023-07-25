package part2.example14;

import part2.dto.SampleData;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;

import java.time.Duration;

@Slf4j
public class Example14Split {

    public static void main(String[] args){
//        example14_53();
//        example14_54();
//        example14_55();
//        example14_56();
//        example14_57();
//        example14_58();
        example14_59();
    }



    /**
     * window() Operator는 Upstream에서 emit되는 첫 번째 데이터부터 maxSize 숫자만큼의 데이터를 포함하는 새로운 Flux로 분할합니다.
     * Reactor에서는 이렇게 분활된 Flux를 윈도우라고 부릅니다.
     */
    public static void example14_53(){
        Flux.range(1, 11)
                .window(4)
                .flatMap(flux -> {
                    log.info("=====================");
                    return flux;
                })
                .subscribe(new BaseSubscriber<>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        subscription.request(2);
                    }

                    @Override
                    protected void hookOnNext(Integer value) {
                        log.info("# onNext : {}", value);
                        request(2);
                    }
                });
    }

    /**
     * 원본 데이토 소스는 2021년도 월별 도서 매출액 데이터이며, 이 데이터를 window() Operator로 3개씩 분할한 후에 MathFlux.sumInt() Operator를 이용해 3개씩 분할된 데이터의 합계를 구한다
     */
    public static void example14_54(){
        Flux.fromIterable(SampleData.monthlyBookSales2021)
                .window(3)
                .flatMap(flux -> MathFlux.sumInt(flux))
                .subscribe(new BaseSubscriber<>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        subscription.request(2);
                    }

                    @Override
                    protected void hookOnNext(Integer value) {
                        log.info("# onNext {}", value);
                        request(2);
                    }
                });
    }

    /**
     * buffer(int maxSize) Operator는 Upostream에서 emit되는 첫 번째 데이터부터 maxSize숫자만큼의 데이터를 List 버퍼로 한 번에 emit한다
     * 마지막 버퍼에 포함된 데이터의 개수는 maxSize보다 더 적거나 같습니다.
     */
    public static void example14_55(){
        Flux.range(1, 95)
                .buffer(10)
                .subscribe(buffer -> log.info("# onNext: {}", buffer));
    }

    /**
     * bufferTimeout Operator는 Upstream에서 emit,되는 첫 번째 데이터부터 maxSize 숫자만큼의 데이터 또는 maxRTime내에 emit된 데이터를 List 버퍼ㅏ로 한 번에 emit한다
     * 또한 maxSize나 maxTime 중에서 먼저 조건에 부합될 때가지 emit된 데이터를 List 버퍼로 emit한다
     */
    public static void example14_56(){
        Flux.range(1, 20)
                .map(num -> {
                    try{
                        if(num < 10){
                            Thread.sleep(100L);
                        }else{
                            Thread.sleep(300L);
                        }

                    }catch (InterruptedException e){

                    }
                    return num;
                })
                .bufferTimeout(3, Duration.ofMillis(400L))
                .subscribe(buffer -> log.info("# onNext: {}", buffer));
    }

    /**
     * groupBy Operator는 emit되는 데이터를 keyMapper로 생성한 key를 기준으로 그룹화한 GroupedFlux를 리턴하며, 이 GroupedFlux를 통해서 그룹별로 작업을 수행할 수 있다.
     */
    public static void example14_57(){
        Flux.fromIterable(SampleData.books)
                .groupBy(book -> book.getAuthorName())
                .flatMap(
                        groupedFlux -> groupedFlux.map(book -> book.getBookName() + "(" + book.getAuthorName()+")").collectList()
                )
                .subscribe(bookByAuthor -> log.info("# book by author: {}", bookByAuthor));
    }

    /**
     * groupBy Operator의 경우 keyMapper를 통해 생성되는 key를 기준으로 emit되는 데이터를 그룹화하지만 groupBy(keyMapper, valueMapper) Operator는 그룹화하면서 valueMapper를 통해 그룹화되는 데이터를 다른 형태로 가공 처리 할 수 있다
     */
    public static void example14_58(){
        Flux.fromIterable(SampleData.books)
                .groupBy(
                        book -> book.getAuthorName(), book -> book.getBookName() + "(" + book.getAuthorName() + ")"
                )
                .flatMap(groupedFlux -> groupedFlux.collectList())
                .subscribe(bookByAuthor -> log.info("# book by author: {}", bookByAuthor));
    }


    /**
     * 저자별로 도서를 그룹화한 후에 그룹화한 도서가 각 도서의 재고 수량만큼 모두 판매되었을 때 저자가 얻을 수 있는 총 인세 수익을 계산하는 예제
     */
    public static void example14_59(){
        Flux.fromIterable(SampleData.books)
                .groupBy(book -> book.getAuthorName()) //저자명을 기준으로 도서 그룹화
                .flatMap( //도서 그룹별로 총 인세 수익을 계산해서 subscriber에게 전달하기 위한 평탄화 작업(인세 수익을 계산하는 작업)
                        groupedFlux -> Mono
                                .just(groupedFlux.key())
                                .zipWith( //저장명 : 총 인세 수익 형태로 subscriber에게 전달되도록 zipWith() Operator를 이용해 두 개의 Mono를 하나로 합치고 있는 과정
                                        groupedFlux
                                                .map(book -> (int)(book.getPrice() * book.getStockQuantity()*0.1)) // groupedFlux에서 emit된 도서 정보 중에서 도서의 가격, 재고 수량, 인세 지급 비율로 도서 한 권당 인세 금액을 계산
                                                .reduce((y1, y2) -> y1 + y2), (authorName, sumRoyalty) -> authorName + "'s royalty: "+ sumRoyalty) //reduce를 이용해서 그룹화도니 도서의 총 인세 합계를 계산
                )
                .subscribe(log::info);

    }
}
