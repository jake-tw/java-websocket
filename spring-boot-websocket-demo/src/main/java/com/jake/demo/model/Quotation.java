package com.jake.demo.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Quotation {

    private String symbol;
    private int price;
    private LocalDateTime time;
}
