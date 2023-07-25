package part2.example14;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
public class Example14Multicast {

    public static void main(String[]args) throws InterruptedException {
        //example14_60();
        //example14_61();
        //example14_62();
        example14_63();
    }


    /**
     * publish() Operator는 구독을 하더라도 구독 시점에 즉시 데이터를 emit하지 않고 connect()를 호출하는 시점에 비로소 데이터를 emit한다
     * 1. publish() Operator를 이ㅛㅇ해 0.3초에 한 번씩 1부터 5까지 emit하는 ConnectableFlux<Integer>를 리턴 받는다. publish() Operator를 호출했지만 아직 connect를 호출하지 않았기 때문에 이 시점에 emit되는 데이터는 없다
     * 2. 0.5초 뒤에 첫 번째 구독이 발생한다
     * 3. 0.2초 뒤에 두 번째 구독이 발생한다
     * 4. connect()가 호출된다. 이 시점부터 데이터가 0.3초에 한 번씩 emit된다.
     * 5. 1초 뒤에 세 번째 구독이 발생한다. 그런데 connect()가 호출된 시점부터 0.3초에 한 번씩 데이터가 emit되기 떄문에 숫자 1부터 3까지는 세 번째 구독 전에 이미 emit된 상태라서 세 번째 subscriber는 전달받지 못 한다
     * @throws InterruptedException
     */
    public static void example14_60() throws InterruptedException {
         ConnectableFlux<Integer> flux =
                 Flux
                         .range(1, 5)
                         .delayElements(Duration.ofMillis(300L))
                         .publish();

         Thread.sleep(500L);
         flux.subscribe(data -> log.info("# subscriber1: {}", data));

         Thread.sleep(200L);
         flux.subscribe(data -> log.info("# subscriber2: {}", data));

         flux.connect();

         Thread.sleep(1000L);
         flux.subscribe(data -> log.info("# subscriber3: {}", data));

         Thread.sleep(2000L);
     }
    private static ConnectableFlux<String> publisher;
    private static int checkedAudience;
    static{
        publisher = Flux
                .just("Concert part1","Concert part2","Concert part3")
                .delayElements(Duration.ofMillis(300L))
                .publish();
    }

    /**
     * 관객이 입장할 때마다 관객 수를 체크해서 관객이 두 명 이상 입장하면 콘서트를 시작하는 상황을 시뮬리이션한 코드 뒤즌게 입장한 관객은 입장한 시점 이후의 콘서트만 볼 수 있음
     * @throws InterruptedException
     */
     public static void example14_61() throws InterruptedException {
        checkedAudience();
        Thread.sleep(500L);
        publisher.subscribe((data ->log.info("# audience 1 is watching {}", data)));
        checkedAudience++;

        Thread.sleep(500L);
         publisher.subscribe((data ->log.info("# audience 2 is watching {}", data)));
         checkedAudience++;

         checkedAudience();
         Thread.sleep(500L);
         publisher.subscribe(data -> log.info("# audience 3 is watching {}", data));
         Thread.sleep(1000);
     }

     private static void checkedAudience(){
        if(checkedAudience >= 2){
            publisher.connect();
        }
     }

    /**
     * publish() Operator의 경우 구독이 발생하더라도 connect()를 직접 호출하기전까지는 데이터를 emit하지 않기 때문에 코드 상에서 connect()를 직접 호출해야 한다
     * 반면에 autoConnect() Operator는 파라미터로 지정하는 숫자만큼의 구독이 발생하는 시점에 Upstreeam 소스에 자동으로 연결되기 때문에 별도의 connect() 호출이 필요하지 않다.
     * @throws InterruptedException
     */
    public static void example14_62() throws InterruptedException {
         Flux<String> publisher =
                 Flux
                         .just("Concert part1","Concert part2","Concert part3")
                         .delayElements(Duration.ofMillis(300L))
                         .publish()
                         .autoConnect(2);
                Thread.sleep(500L);
                publisher.subscribe(data -> log.info("# audience 1 is watching {}", data));

                Thread.sleep(500L);
                publisher.subscribe(data -> log.info("# audience 2 is watching {}", data));
                Thread.sleep(500L);
                publisher.subscribe(data -> log.info("# audience 3 is watching {}", data));

                Thread.sleep(1000L);
     }

    /**
     * refCount Operator는 파라미터로 입력된 숫자만큼의 구독이 밠갱하는 시점에 Upstream 소스에 연결되며 모든 구독이 취소되거나 Upstream의 데이터 emit이 종료되면 연결이 해제된다.
     * refCoutn는 Operator는 주로 무한 스트림 상화에서 모든 구독이 취소될 경우ㅡ 연결을 해제하는 데 사용할 수 있다.
     *
     * @throws InterruptedException
     */
     public static void example14_63() throws InterruptedException {
        Flux<Long> publisher =
                Flux.interval(Duration.ofMillis(500L))
                        //.publish().autoConnect(1);
                        .publish().refCount(1);
         Disposable disposable = publisher.subscribe(data -> log.info("# subscriber 1: {}", data));

         Thread.sleep(2100L);
         disposable.dispose();
         publisher.subscribe(data -> log.info("# subscriber 2: {}", data));
        Thread.sleep(2400L);
    }
}
