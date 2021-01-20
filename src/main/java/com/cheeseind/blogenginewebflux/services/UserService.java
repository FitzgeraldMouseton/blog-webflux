package com.cheeseind.blogenginewebflux.services;

import com.cheeseind.blogenginewebflux.models.User;
import com.cheeseind.blogenginewebflux.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Mono<User> save(User user) {
        return userRepository.save(user);
    }

    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Mono<User> findByCode(String code) {
        return userRepository.findByCode(code);
    }

    public Mono<Boolean> isFirstUser() {
        return userRepository.existsAllBy().map(b -> !b);
    }

    public Mono<User> getCurrentUser() {

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getPrincipal())
                .cast(User.class);
    }

    public Mono<String> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }
}
