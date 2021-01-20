package com.cheeseind.blogenginewebflux.repositories;

import com.cheeseind.blogenginewebflux.models.Comment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {

    Flux<Comment> findAllByPostId(String postId);
}
