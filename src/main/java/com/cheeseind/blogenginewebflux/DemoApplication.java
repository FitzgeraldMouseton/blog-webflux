package com.cheeseind.blogenginewebflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.tools.agent.ReactorDebugAgent;

/*
    @EnableWebFlux - аннотация, как ни странно, нужная только в том случае, если мы сами хотим
    настроить всю конфигурацию WebFlux. Так что не ставим.
 */

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        ReactorDebugAgent.init();
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> imgRouter() {
        return RouterFunctions
                .resources("/images/**", new FileSystemResource("images/"));
    }

    @Bean
    public RouterFunction<ServerResponse> avatarRouter() {
        return RouterFunctions
                .resources("/avatars/**", new FileSystemResource("avatars/"));
    }
}
