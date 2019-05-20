package com.sample.webflux.handler;

import com.sample.webflux.service.QuoteGenerator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class ReactiveWebSocketHandler implements WebSocketHandler {
    private final QuoteGenerator quoteGenerator;

    public ReactiveWebSocketHandler(final QuoteGenerator quoteGenerator) {
        this.quoteGenerator = quoteGenerator;
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        return webSocketSession.send(quoteGenerator.fetchQuoteStringStream(Duration.ofMillis(2000))
                .map(webSocketSession::textMessage))
                .and(webSocketSession.receive()
                        .map(WebSocketMessage::getPayloadAsText).log());
    }
}
