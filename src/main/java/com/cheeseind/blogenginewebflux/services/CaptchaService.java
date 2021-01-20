package com.cheeseind.blogenginewebflux.services;

import com.cheeseind.blogenginewebflux.models.CaptchaCode;
import com.cheeseind.blogenginewebflux.repositories.CaptchaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final CaptchaRepository captchaRepository;

    public Mono<CaptchaCode> findBySecretCode(String code) {
        return captchaRepository.findBySecretCode(code);
    }

    public Mono<CaptchaCode> findByCode(final String code) {
        return captchaRepository.findByCode(code);
    }

    public Mono<CaptchaCode> save(final CaptchaCode captchaCode) {
        return captchaRepository.save(captchaCode);
    }

    @Transactional
    public Mono<Void> delete(final CaptchaCode captchaCode) {
        return captchaRepository.delete(captchaCode);
    }

    @Transactional
    public Mono<Void> deleteCaptchaCodeBySecretCode(final String secretCode) {
        return captchaRepository.deleteBySecretCode(secretCode);
    }

    public Flux<CaptchaCode> getAllCaptchaCodes() {
        return captchaRepository.findAllBy();
    }
}
