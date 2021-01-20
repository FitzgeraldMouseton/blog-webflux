package com.cheeseind.blogenginewebflux.services;

import com.cheeseind.blogenginewebflux.models.Comment;
import com.cheeseind.blogenginewebflux.repositories.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public Mono<Comment> save(Comment comment) {
        return commentRepository.save(comment);
    }

    public Flux<Comment> findAllCommentOfPost(String postId) {
        return commentRepository.findAllByPostId(postId);
    }
}
