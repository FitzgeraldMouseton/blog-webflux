package com.cheeseind.blogenginewebflux.routers;

import com.cheeseind.blogenginewebflux.handlers.GeneralHandler;
import com.cheeseind.blogenginewebflux.handlers.TagHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
public class GeneralRouter {

    @Bean
    public RouterFunction<ServerResponse> generalRouterr(GeneralHandler generalHandler,
                                                        TagHandler tagHandler) {
        return route(GET("/api/statistics/my").and(accept(APPLICATION_JSON)), generalHandler::getUserStatistics)
                .andRoute(GET("/api/statistics/all").and(accept(APPLICATION_JSON)), generalHandler::getBlogStatistics)
                .andRoute(GET("/api/settings"), generalHandler::getSettings)
                .andRoute(GET("/api/init"), generalHandler::getBlogInfo)
                .andRoute(GET("/api/calendar"), generalHandler::calendar)
                .andRoute(POST("/api/comment"), generalHandler::addComment)
                .andRoute(POST("/api/moderation"), generalHandler::moderation)
                .andRoute(GET("/api/tag"), tagHandler::getTags)
                .andRoute(PUT("/api/settings"), generalHandler::changeSettings)
                .andRoute(POST("/api/image"), generalHandler::uploadImage)
                .andRoute(POST("/api/profile/my").and(contentType(APPLICATION_JSON)), generalHandler::editProfile)
                .andRoute(POST("/api/profile/my").and(contentType(MULTIPART_FORM_DATA)), generalHandler::editProfileWithPhoto);
    }
}
