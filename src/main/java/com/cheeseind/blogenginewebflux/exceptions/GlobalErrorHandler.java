package com.cheeseind.blogenginewebflux.exceptions;

import com.cheeseind.blogenginewebflux.models.dto.ErrorResponse;
import com.cheeseind.blogenginewebflux.models.dto.SimpleResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Order(-2)
@Configuration
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {

        DataBufferFactory bufferFactory = serverWebExchange.getResponse().bufferFactory();
        if (throwable instanceof InappropriateActionException || throwable instanceof PageNotFoundException) {
            serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
            log.info("exception: " + throwable.getMessage());
            Mono<DataBuffer> responseBody = Mono.fromCallable(() -> {
                DataBuffer dataBuffer;
                try {
                    dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(new SimpleResponseDto(false)));
                } catch (JsonProcessingException e) {
                    dataBuffer = bufferFactory.wrap("".getBytes());
                }
                return dataBuffer;
            });
            serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return serverWebExchange.getResponse().writeWith(responseBody);
        }

        if (throwable instanceof InvalidParameterException) {
            log.info("exception: " + throwable.getMessage());
            serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
            Mono<DataBuffer> responseBody = Mono.fromCallable(() -> {
                DataBuffer dataBuffer;
                try {
                    Map<String, String> errors = new HashMap<>();
                    ((InvalidParameterException) throwable).getErrors().getFieldErrors()
                            .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
                    dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(new ErrorResponse(errors)));
                } catch (JsonProcessingException e) {
                    dataBuffer = bufferFactory.wrap("".getBytes());
                }
                return dataBuffer;
            });
            serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return serverWebExchange.getResponse().writeWith(responseBody);
        }

        serverWebExchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        serverWebExchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        DataBuffer dataBuffer = bufferFactory.wrap("Unknown error".getBytes());
        return serverWebExchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
}
