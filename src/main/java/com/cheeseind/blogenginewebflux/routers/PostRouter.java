package com.cheeseind.blogenginewebflux.routers;

import com.cheeseind.blogenginewebflux.handlers.PostHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class PostRouter {

    @Bean
    public RouterFunction<ServerResponse> postsRouter(PostHandler postHandler) {
        return route()
                .POST("/api/post", postHandler::addPost)
                .POST("/api/post/like", postHandler::likePost)
                .POST("/api/post/dislike", postHandler::dislikePost)
                .GET("/api/post", postHandler::getPosts)
                .GET("/api/post/search", postHandler::searchPost)
                .GET("/api/post/byDate", postHandler::getPostsByDate)
                .GET("/api/post/byTag", postHandler::getPostsByTag)
                .GET("/api/post/my", postHandler::getCurrentUserPosts)
                .GET("/api/post/moderation", postHandler::getPostsForModeration)
                .GET("/api/post/{id}", postHandler::getPostById)
                .PUT("/api/post/{id}", postHandler::editPost)
                .build();
    }
}
