package com.cheeseind.blogenginewebflux.security;

import com.cheeseind.blogenginewebflux.models.dto.authdto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginJsonAuthConverter implements ServerAuthenticationConverter {

    private final ObjectMapper mapper;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {

        return exchange.getRequest().getBody()
                .next()
                .flatMap(buffer ->
                        Mono.fromCallable(() -> mapper.readValue(buffer.asInputStream(), LoginRequest.class)))
                .map(request -> new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    }
}