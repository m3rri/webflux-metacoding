package com.example.flux.web;

import com.example.flux.domain.Customer;
import com.example.flux.domain.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest
//이 어노테이션 안에서 @AutoConfigureWebTestClient 어노테이션이 동작하고 있다.
//그러면 @Autowired WebTestClient가 동작한다.
public class CustomerControllerTest {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    CustomerRepository customerRepository;

    @Test
    public void 한건찾기_테스트(){
        /**  org.mockito.Mockito.when을 사용하는 목적
         * - 본 test class는 controller 테스트임
         * - 그리고 JUnit test시에는 Controller ~ Repository 의 어노테이션만 bean으로 등록하여 DI에 사용한다.
         * - 따라서 컨트롤러 테스트에서 url 요청해서 데이터받아오는 로직을 짠다고해도, db에도 데이터 없고 repo도 없는 상태라서 테스트가 안됨
         * - 컨트롤러의 작동 여부만 확인하기 위해서, 나머지 클래스에 대해서는 when을 사용해서 특정 클래스의 특정 메소드가 호출되는 경우, 어떤 동작을 실행할지를 지정할 수가 있다.
         */
        when(customerRepository.findById(1L)).thenReturn(Mono.just(new Customer("Jack", "Bauer")));

        webClient.get().uri("/customers/{id}", 1L)
                .exchange()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Jack")
                .jsonPath("$.lastName").isEqualTo("Bauer");

        /**Jack 대신에 Jack2로 테스트 하는 경우
         * JSON path "$.firstName" expected:<Jack2> but was:<Jack>
         * Expected :Jack2
         * Actual   :Jack
         */
    }
}
