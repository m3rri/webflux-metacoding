package com.example.flux.web;

import com.example.flux.domain.Customer;
import com.example.flux.domain.CustomerRepository;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@RestController
public class CustomerController {
    private final CustomerRepository customerRepository;
    private final Sinks.Many<Customer> sinkCustomer;//many 방식이 unicast, multicast(새로 push되는 데이터를 받음), 또 다른거 있음

    public CustomerController(CustomerRepository customerRepository){
        this.customerRepository= customerRepository;
        this.sinkCustomer = Sinks.many().multicast().onBackpressureBuffer();
    }

    @GetMapping("/flux")
    public Flux<Integer> flux(){
        return Flux.just(1,2,3,4,5,6).delayElements(Duration.ofMillis(1)).log();
    }

    @GetMapping(value = "/flux-stream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Integer> fluxStream(){
        //produces = MediaType으로 APPLICATION_STREAM_JSON_VALUE로 설정하면, response는 열어두고, onNext할 때마다 buffer flush가 일어난다.
        return Flux.just(1,2,3,4,5,6).delayElements(Duration.ofMillis(300)).log();
    }

    @GetMapping(value = "/customers", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Customer> findAll(){
        return customerRepository.findAll().delayElements(Duration.ofMillis(300)).log();
/**log
 * [time] INFO 23616 --- [ctor-http-nio-2] reactor.Flux.UsingWhen.1 : onSubscribe(FluxUsingWhen.UsingWhenSubscriber)               : database data에 구독한 것. 구독정보를 돌려주고 log로 남긴것. method의 return type이 Mono인 경우 MonoUsingWhen.MonoUsingWhenSubscriber가 구독된다.
 * [time] INFO 23616 --- [ctor-http-nio-2] reactor.Flux.UsingWhen.1 : request(unbounded)                                           : 가지고 있는 데이터를 다 요청하겟다.
 * [time] INFO 23616 --- [ctor-http-nio-2] reactor.Flux.UsingWhen.1 : onNext(Customer(id=1, firstName=Jack, lastName=Bauer))
 * [time] INFO 23616 --- [ctor-http-nio-2] reactor.Flux.UsingWhen.1 : onNext(Customer(id=2, firstName=Chloe, lastName=O'Brian))
 * [time] INFO 23616 --- [ctor-http-nio-2] reactor.Flux.UsingWhen.1 : onNext(Customer(id=3, firstName=Kim, lastName=Bauer))
 * [time] INFO 23616 --- [ctor-http-nio-2] reactor.Flux.UsingWhen.1 : onNext(Customer(id=4, firstName=David, lastName=Palmer))
 * [time] INFO 23616 --- [ctor-http-nio-2] reactor.Flux.UsingWhen.1 : onNext(Customer(id=5, firstName=Michelle, lastName=Dessler))
 * [time] INFO 23616 --- [ctor-http-nio-2] reactor.Flux.UsingWhen.1 : onNext(Customer(id=6, firstName=Test, lastName=Test))
 * [time] INFO 23616 --- [ctor-http-nio-2] reactor.Flux.UsingWhen.1 : onComplete()                                                 : 다 받았음. 이 순간 client로 응답됨.. ?? ..
 */
    }

    @GetMapping("/customers/{id}")
    public Mono<Customer> findById(@PathVariable Long id){
        //1건의 데이터를 받는다 = onNext 한번으로 요청이 끝난다.
        return customerRepository.findById(id).log();
    }

    //@GetMapping(value = "/customers/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE) //요놈이 표준이다
    @GetMapping("/customers/sse") //요놈이 표준이다
    public Flux<ServerSentEvent<Customer>> findAllSSE(){
        return sinkCustomer
                .asFlux()
                .map(customer-> ServerSentEvent.builder(customer).build())//js에서는 EventSource라는 인터페이스 사용해서 구독할 수 있다.
                .doOnCancel(()-> sinkCustomer.asFlux().blockLast());
    }

    @PostMapping("/customer")
    public Mono<Customer> save(String firstName, String lastName){
        return customerRepository
                .save(new Customer(firstName, lastName))
                .doOnNext(customer->sinkCustomer.tryEmitNext(customer));
    }
}