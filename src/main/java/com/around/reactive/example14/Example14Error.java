package com.around.reactive.example14;

import com.around.reactive.dto.Book;
import com.around.reactive.dto.Book2;
import com.around.reactive.dto.SampleData;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

@Slf4j
public class Example14Error {
    public static void main(String[] args) throws InterruptedException {
        //example14_43();
        //example14_44();
        //example14_45();
        //example14_46();
        //example14_47();
        //example14_48();
        //example14_49();
        example14_50();
    }

    /**
     * error() Operator는 파라미터로 지정된 ㅔㅇ러로 종료하는 Flux를 생성한다
     * error() Oeprator는 마치 Java으 throw 키워드를 사용해서 예외를 의도적으로 던지는 것 같은 역할을 하는데 주로 체크 예외를 캐치해서 다시 던져야 하는 경우 사용할 수 있다.
     */
    public static void example14_43(){
        Flux
                .range(1, 5)
                .flatMap(num -> {
                    if((num*2) %3 ==0){
                        return Flux.error(
                                new IllegalArgumentException("Not allowed multiple of 3")
                        );
                    }else{
                        return Mono.just(num*2);
                    }
                })
                .subscribe(
                        data -> log.info("# onNext: {}", data),
                        error -> log.error("# onError: ", error)
                );
    }

    /**
     * convert() 메소드는 파라미터로 전달받은 문자가 영문자가 아니면 DataFormatException을 던지도록 구현되어있는데
     * 이 DataFormatException은 Exception을 상솓ㄱ하는 체크 예외이기 때문에 try ~ catch문으로 반드시 처리해야 한다
     * 따라서 flatMap() operator 내부에서  try ~ catch문으로 해당 예외를 캐치한 후, 캐치된 예외를 error() Oeprator를 이용해 onError Signal형태로 Downstream에 전송한다.
     *
     */
    public static void example14_44(){
        Flux
                .just('a','b','c','3','d')
                .flatMap(letter -> {
                    try{
                        return convert(letter);
                    }catch (DataFormatException e){
                        return Flux.just(e);
                    }
                })
                .subscribe(
                        data -> log.info("# onNext: {}", data),
                        error -> log.error("# onError: ", error)
                );
    }
    private static Mono<String> convert(char ch) throws DataFormatException{
        if(!Character.isAlphabetic(ch)){
            throw new DataFormatException("Not Alphabetic");
        }
        return Mono.just("Converted to "+ Character.toUpperCase(ch));
    }

    /**
     * 도서 목록의 도서 중에는 필명이 필수 입력 값이 아니기 때문에 null값이 포함될 수 있다 하지만 필묭아ㅣ 어봇는 도서를 사전에 필터링하지 않았기 떄문에
     * map() Operator에서 영문 필명을 대문자로 바꿀 떄 null 값이 있으면 nullPointExcpetion이 발생
     * NullPointExceptiuon이 발생하게 되면 에러 이벤트가 Downstream으로 전파돠는 대신에 No pen name이라는 문자열 값으로 대체해서 emit하도록 했다.
     */
    public static void example14_45(){
        getBooks()
                .map(book -> book.getPenName().toUpperCase())
                .onErrorReturn("No Pen name")
                .subscribe(log::info);
    }

    private static Flux<Book2> getBooks(){
        return Flux.fromIterable(SampleData.books);
    }

    /**
     * onErrorReturn() Operator는 첫 번째 파라미터로 특정 예외 타입을 지정해서 지정된 타입의 예외가 발생할 경우에만 대체 값을 emit하도록 헀다.
     */
    public static void example14_46(){
        getBooks()
                .map(book -> book.getPenName().toUpperCase())
                .onErrorReturn(NullPointerException.class, "no pen name")
                .onErrorReturn(IllegalFormatException.class, "Illegal pen name")
                .subscribe(log::info);
    }

    /**
     * 특정 키워드로 키워드에 해당하는 도서가 있는지 캐시에서 먼저 검색한 후 검색된 도서가 있으면 해당 도서를 emit하는 Flux를 리턴하고 검색된 도서가 없으면 NoSuchBookException을 발생시킨다
     * NoSuchBookExcception이 발생하면 onErrorResume() Operator를 이용해서 대체 동작을 수행한다
     */
    public static void example14_47(){
        final String keyword = "DDD";
        getBooksFromCache(keyword)
                .onErrorResume(error -> getBooksFromDatabase(keyword))
                .subscribe(
                        data -> log.info("#onNext: {}", data.getBookName()),
                        error -> log.error("# onError:", error));
    }

    private static Flux<Book2> getBooksFromCache(final String keyword){
        return Flux
                .fromIterable(SampleData.books)
                .filter(book2 -> book2.getBookName().contains(keyword))
                .switchIfEmpty(Flux.error(new NoSuchBookException("No such Book")));
    }
    private static Flux<Book2> getBooksFromDatabase(final String keyword){
        List<Book2> books = new ArrayList<>(SampleData.books);
        books.add(new Book2("DDD: Domain Driver Design", "Joy", "ddd-man", 35000, 200));
        return Flux
                .fromIterable(books)
                .filter(book2 -> book2.getBookName().contains(keyword))
                .switchIfEmpty(Flux.error(new NoSuchBookException("No such Book")));
    }

    private static class NoSuchBookException extends RuntimeException{
        public NoSuchBookException(String message) {
            super(message);
        }
    }

    /**
     * Reactor Sequence를 사용하다 보면 에러가 발생했을 때 Sequence가 종료되지 않고 아직 emit되지 않은 데이터를 다시 emit해야 하는 상황이 발생한다
     * 이때 사용할 수 있는게 onErrorContinue() Operator이다
     * onErrorContinue() Operator는 에러가 발생했을 떄 에러 영역 내에 있는 데이터는 제거하고 UIpstream에서 후속 데이터를 emit하는 방식으로 에러를 복구할 수 있도록 해준다.
     * onErrorContinue()은 BiConsumer 함수형 인터페이스를 통해 에러 메시지와 에러가 발생했을 때 emit된 데이터를 전달받아서 로그를 기록하는 등의 후처리를 할 수 있다.
     */
    public static void example14_48(){
        Flux
                .just(1,2,4,0,6,12)
                .map(num -> 12/num)
                .onErrorContinue(
                        (error, num) -> log.error("error: {}, num: {}", error.getMessage(), num))
                .subscribe(
                        data -> log.info("# onNext: {}", data),
                        error -> log.error("# onError: ", error));
    }

    /**
     * retry() Operator는 Publisher가 데이터를 emit하는 과정에서 에러가 발생하면 파라미터로 입력한 횟수만큼 원본 Flux의 Sequence를 다시 구독한다. 만약 파라미터로 Long.MAX_VALUE를 입력하면 재구동ㄱ을 무한 반복한다.
     * @throws InterruptedException
     */
    public static void example14_49() throws InterruptedException {
        final int[] count ={1};
        Flux
                .range(1, 3)
                .delayElements(Duration.ofSeconds(1))
                .map(
                        num -> {
                            try{
                                if(num == 3 && count[0] ==1){
                                    count[0]++;
                                    Thread.sleep(1000);
                                }
                            }catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            return num;
                        }
                )
                .timeout(Duration.ofMillis(1500))
                .retry(1)
                .subscribe(
                        data -> log.info("# onNExt: {}", data),
                        error -> log.error("# onError: ", error),
                        () -> log.info("# onComplete")
                );
        Thread.sleep(7000);
    }

    /**
     * 네트워크 지연으로 일정 시간이 지남녀 1회 재요청하는 것을 시뮬레이션 한다
     * 요청 전송 시 2총 동안 emit되는 데이터가 없다면 1회 재시도하여 도서 목록을 다시 조회할 수 있다
     * subscriber 쪽에서는 Timeoutexception이 발생하기 전에 전달받은 데이터와 1회 재시도 후 전달받은 데이터의 중복을 제거하기 위해 collect(Collectors.toSet())을 사용했다.
     * @throws InterruptedException
     */
    public static void example14_50() throws InterruptedException {
        getBooks2()
                .collect(Collectors.toSet())
                .subscribe(
                        bookSet -> bookSet
                                .stream()
                                .forEach(book -> log.info("book Name: {}, price: {}", book.getBookName(), book.getPrice())));
        Thread.sleep(12000);
    }


    private static Flux<Book2> getBooks2(){
        final int[] count = {0};
        return Flux
                .fromIterable(SampleData.books)
                .delayElements(Duration.ofMillis(500))
                .map(
                        book2 -> {
                        try{
                            count[0]++;
                            if(count[0] == 3){
                                Thread.sleep(2000);
                            }
                        }catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        return book2;
                })
                .timeout(Duration.ofSeconds(2))
                .retry(1)
                .doOnNext(book -> log.info("# getBooks > doOnNext: {} price: {}", book.getBookName(), book.getPrice()));

    }

}
