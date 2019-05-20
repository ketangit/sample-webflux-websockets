package com.sample.webflux.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Event {
    private final String id;
    private final List<Quote> data;
}
