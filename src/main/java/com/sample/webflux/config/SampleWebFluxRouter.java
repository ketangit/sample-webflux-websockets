package com.sample.webflux.config;

import com.sample.webflux.model.Event;
import com.sample.webflux.model.Quote;
import com.sample.webflux.service.QuoteGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.time.Duration;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class SampleWebFluxRouter {
    @Value("classpath:/static/index.html")
    private Resource indexHtml;

    @Bean
    public RouterFunction<ServerResponse> getIndex() {
        return route(GET("/"), request
                -> ok()
                .contentType(MediaType.TEXT_HTML)
                .syncBody(indexHtml));
    }

    @Bean
    public RouterFunction<ServerResponse> compositeRoutes(QuoteGenerator quoteGenerator) {
        return route(GET("/quotes"), request
                -> ok()
                .contentType(MediaType.APPLICATION_STREAM_JSON)
                .body(quoteGenerator.getQuotes(), Event.class))
                .and(route(GET("/quotes/{id}"), request
                        -> ok()
                        .contentType(MediaType.APPLICATION_STREAM_JSON)
                        .body(quoteGenerator.fetchQuoteStream(Duration.ofMillis(Long.parseLong(request.pathVariable("id")))), Quote.class)));
    }
}
