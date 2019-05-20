package com.sample.webflux.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class Quote {
    private String ticker;
    private BigDecimal price;
    private Instant instant;
}
