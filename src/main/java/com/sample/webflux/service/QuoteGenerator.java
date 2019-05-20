package com.sample.webflux.service;

import brave.Tracer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.webflux.model.Event;
import com.sample.webflux.model.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class QuoteGenerator {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MathContext MATH_CONTEXT = new MathContext(2);
    private final Random random = new Random();
    private final List<Quote> prices = new ArrayList<>();

    @Autowired
    private Tracer tracer;

    public QuoteGenerator() {
        this.prices.add(new Quote("APPL", new BigDecimal(82.26, MATH_CONTEXT), Instant.now()));
        this.prices.add(new Quote("TSLA", new BigDecimal(63.74, MATH_CONTEXT), Instant.now()));
        this.prices.add(new Quote("GOOG", new BigDecimal(847.24, MATH_CONTEXT), Instant.now()));
        this.prices.add(new Quote("MSFT", new BigDecimal(165.11, MATH_CONTEXT), Instant.now()));
        this.prices.add(new Quote("AMZN", new BigDecimal(35.71, MATH_CONTEXT), Instant.now()));
        this.prices.add(new Quote("NFLX", new BigDecimal(84.29, MATH_CONTEXT), Instant.now()));
        this.prices.add(new Quote("INTC", new BigDecimal(20.21, MATH_CONTEXT), Instant.now()));
    }

    public Flux<String> fetchQuoteStringStream(Duration period) {
        // We want to emit quotes with a specific period;
        // to do so, we create a Flux.interval
        return Flux.interval(period)
                // In case of back-pressure, drop events
                .onBackpressureDrop()
                // For each tick, generate a list of quotes
                .map(this::generateStringQuotes)
                .log("fetchQuoteStringStream");
    }

    private String generateStringQuotes(long interval) {
        Event event = new Event(tracer.currentSpan().context().traceIdString(), generateQuotes(interval));
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "{}";
    }

    public Flux<Quote> fetchQuoteStream(Duration period) {
        // We want to emit quotes with a specific period;
        // to do so, we create a Flux.interval
        return Flux.interval(period)
                // In case of back-pressure, drop events
                .onBackpressureDrop()
                // For each tick, generate a list of quotes
                .map(this::generateQuotes)
                // "flatten" that List<Quote> into a Flux<Quote>
                .flatMapIterable(quotes -> quotes)
                .log("fetchQuoteStream");
    }

    public Flux<Event> getQuotes() {
        return Flux.fromStream(Stream.of(new Event(tracer.currentSpan().context().traceIdString(), generateQuotes(0)))).log();
    }

    private List<Quote> generateQuotes(long interval) {
        final Instant instant = Instant.now();
        return prices.stream()
                .map(baseQuote -> {
                    BigDecimal priceChange = baseQuote.getPrice()
                            .multiply(new BigDecimal(0.05 * this.random.nextDouble()), this.MATH_CONTEXT);
                    return new Quote(baseQuote.getTicker(), baseQuote.getPrice().add(priceChange), instant);
                })
                .collect(Collectors.toList());
    }
}
