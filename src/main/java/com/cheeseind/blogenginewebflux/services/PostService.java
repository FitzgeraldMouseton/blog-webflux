package com.cheeseind.blogenginewebflux.services;

import com.cheeseind.blogenginewebflux.models.ModerationStatus;
import com.cheeseind.blogenginewebflux.models.Post;
import com.cheeseind.blogenginewebflux.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Mono<Post> findPostById(final String id) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Не найден пост с id " + id)));
    }

    public Flux<Post> findVisiblePosts() {
        return postRepository.findVisiblePosts();
    }

    public Flux<Date> findAllDatesInYear(int year) {
        return postRepository.findAllDatesWithPostsInYear(year);
    }

    public Flux<Integer> findAllYears() {
        return postRepository.findAllYears();
    }

    public Mono<Long> countVisiblePosts() {
        return postRepository.countVisiblePosts();
    }

    public Mono<Post> save(final Post post) {
        return postRepository.save(post);
    }

    public Mono<Long> countUserAcceptedPosts(final String userId) {
        return postRepository.countActivePostsOfUser(userId, ModerationStatus.ACCEPTED);
    }

    public Mono<Post> findFirstPost() {
        return postRepository.findFirstPost();
    }

    public Mono<Post> findFirstPostOfUser(String userId) {
        return Mono.from(postRepository.findFirstPostOfUser(userId).limitRequest(1));
    }

    public Mono<Long> countLikesOfUserPosts(String userId) {
        return postRepository.countLikesOfAllUserPosts(userId);
    }

    public Mono<Long> countDislikesOfUserPosts(String userId) {
        return postRepository.countDislikesOfAllUserPosts(userId);
    }

    public Mono<Long> countUserPostsViews(String userId) {
        return postRepository.countUserPostsViews(userId);
    }

    public Mono<Long> countAllLikes() {
        return postRepository.countAllLikes();
    }

    public Mono<Long> countAllDislikes() {
        return postRepository.countAllDislikes();
    }

    public Mono<Long> countAllPostsViews() {
        return postRepository.countAllPostsViews();
    }

    public Mono<Long> countPostsForModeration() {
        return postRepository.countPostsForModeration();
    }

}

