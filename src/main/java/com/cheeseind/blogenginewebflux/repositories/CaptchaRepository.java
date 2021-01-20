package com.cheeseind.blogenginewebflux.repositories;

import com.cheeseind.blogenginewebflux.models.CaptchaCode;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CaptchaRepository extends ReactiveMongoRepository<CaptchaCode, String> {

    Mono<CaptchaCode> findBySecretCode(String code);

    Mono<CaptchaCode> findByCode(String code);

    Mono<Void> deleteBySecretCode(String secretCode);

    Flux<CaptchaCode> findAllBy();
}
