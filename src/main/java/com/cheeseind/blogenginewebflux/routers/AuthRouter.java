package com.cheeseind.blogenginewebflux.routers;

import com.cheeseind.blogenginewebflux.handlers.AuthHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AuthRouter {

    @Bean
    public RouterFunction<ServerResponse> authenticationRouter(AuthHandler authHandler) {
        return route()
                .GET("/api/auth/check", authHandler::check)
                .GET("/api/auth/captcha", authHandler::captcha)
                .POST("/api/auth/register", authHandler::register)
                .POST("/api/auth/restore", authHandler::sendRestorePassCode)
                .POST("/api/auth/password", authHandler::setNewPassword)
                .build();
    }
}
