package com.cheeseind.blogenginewebflux.repositories;

import com.cheeseind.blogenginewebflux.models.GlobalSetting;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SettingsRepository extends ReactiveMongoRepository<GlobalSetting, Integer> {

    Mono<GlobalSetting> findByCode(String code);
    Flux<GlobalSetting> findAllBy();
}
