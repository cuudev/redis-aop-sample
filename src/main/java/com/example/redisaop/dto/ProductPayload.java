package com.example.redisaop.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ProductPayload {
    private Long id;
    private String name;
    private Float price;
    private Long stock;
}
