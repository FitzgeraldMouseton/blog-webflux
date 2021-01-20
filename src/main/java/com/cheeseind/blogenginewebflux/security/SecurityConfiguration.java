package com.cheeseind.blogenginewebflux.security;

import com.cheeseind.blogenginewebflux.mappers.UserDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    private final UserDtoMapper userDtoMapper;
    private final LoginJsonAuthConverter loginJsonAuthConverter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder());
        return authenticationManager;
    }

    @Bean
    public WebSessionServerSecurityContextRepository contextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public AuthenticationWebFilter authenticationWebFilter() {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(authenticationManager());
        filter.setServerAuthenticationConverter(loginJsonAuthConverter);
        filter.setAuthenticationSuccessHandler(new AuthSuccessHandler(userDtoMapper));
        filter.setSecurityContextRepository(new WebSessionServerSecurityContextRepository());
        filter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/api/auth/login")
        );
        return filter;
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        //        securityContextRepository.setSpringSecurityContextAttrName("securityContext");
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public ServerLogoutSuccessHandler logoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .authorizeExchange()
                    .pathMatchers(HttpMethod.GET, "/*/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/css/**", "/js/**", "/fonts/**", "/img/**", "/*").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/auth/*").permitAll()
                    .anyExchange().authenticated()
                .and()
                    .httpBasic().disable()
                .logout()
                    .requiresLogout(ServerWebExchangeMatchers.pathMatchers("/api/auth/logout"))
                    .logoutSuccessHandler(logoutSuccessHandler())
                .and()
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
