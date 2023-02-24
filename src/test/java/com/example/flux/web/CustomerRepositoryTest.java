package com.example.flux.web;

import com.example.flux.DBInit;
import com.example.flux.domain.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

@DataR2dbcTest
@Import(DBInit.class)
//controller가 아니라 webflux test가 필요한게 아님
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void 한건찾기_테스트(){
        StepVerifier
                .create(customerRepository.findById(3L))
                .expectNextMatches((customer -> customer.getFirstName().equals("Kim")))
                .expectComplete()
                .verify();
    }

}
