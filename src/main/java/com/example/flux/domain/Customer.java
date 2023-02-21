package com.example.flux.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

@RequiredArgsConstructor
@Data
public class Customer {
    @Id
    private Long id;

    private final String firstName;
    private final String lastName;

    //https://spring.io/guides/gs/accessing-data-r2dbc/에서 코드 복사해와서 어노테이션 대체 가능한 메소드들만 수정
}
