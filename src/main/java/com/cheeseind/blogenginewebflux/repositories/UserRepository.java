package com.cheeseind.blogenginewebflux.repositories;

import com.cheeseind.blogenginewebflux.models.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByEmail(String email);
    Mono<User> findByCode(String code);
    Mono<Boolean> existsAllBy();
}
