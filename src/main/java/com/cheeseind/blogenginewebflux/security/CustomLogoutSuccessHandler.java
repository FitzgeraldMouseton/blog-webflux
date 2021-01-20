package com.cheeseind.blogenginewebflux.security;

import com.cheeseind.blogenginewebflux.models.dto.SimpleResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import reactor.core.publisher.Mono;

public class CustomLogoutSuccessHandler implements ServerLogoutSuccessHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {

//        new Jackson2JsonDecoder().decodeToMono(DataBufferUtils.read(Resource))

        ServerHttpResponse response = exchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.OK);
        byte[] bytes = mapper.writeValueAsBytes(new SimpleResponseDto(true));
        Mono<DataBuffer> wrap = Mono.just(exchange.getExchange().getResponse().bufferFactory().wrap(bytes));
        return exchange.getExchange().getResponse().writeWith(wrap);
    }
}
