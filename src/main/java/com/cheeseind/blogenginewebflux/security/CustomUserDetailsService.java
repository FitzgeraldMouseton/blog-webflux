package com.cheeseind.blogenginewebflux.security;

import com.cheeseind.blogenginewebflux.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserService userService;

    @Override
    public Mono<UserDetails> findByUsername(String email) {
        return userService.findByEmail(email)
                .cast(UserDetails.class);
    }
}
