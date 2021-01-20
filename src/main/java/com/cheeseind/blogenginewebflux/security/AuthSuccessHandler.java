package com.cheeseind.blogenginewebflux.security;

import com.cheeseind.blogenginewebflux.mappers.UserDtoMapper;
import com.cheeseind.blogenginewebflux.models.User;
import com.cheeseind.blogenginewebflux.models.dto.authdto.AuthenticationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class AuthSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final UserDtoMapper userDtoMapper;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange exchange, Authentication authentication) {

        Mono<User> principal = Mono.just(authentication.getPrincipal()).cast(User.class);
        exchange.getExchange().getResponse().setStatusCode(HttpStatus.OK);
        Mono<DataBuffer> map = principal
                .flatMap(userDtoMapper::userToLoginResponse)
                .map(loginDto -> new AuthenticationResponse(true, loginDto))
                .handle((r, sink) -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try  {
                        final byte[] bytes = objectMapper.writeValueAsBytes(r);
                        final DataBuffer wrap = exchange.getExchange().getResponse().bufferFactory().wrap(bytes);
                        sink.next(wrap);
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    sink.complete();
                });
        return exchange.getExchange().getResponse().writeWith(map);
    }
}
