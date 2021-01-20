package com.cheeseind.blogenginewebflux.mappers;

import com.cheeseind.blogenginewebflux.models.User;
import com.cheeseind.blogenginewebflux.models.dto.authdto.RegisterRequest;
import com.cheeseind.blogenginewebflux.models.dto.authdto.UserLoginResponse;
import com.cheeseind.blogenginewebflux.models.dto.userdto.UserDto;
import com.cheeseind.blogenginewebflux.services.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
public class UserDtoMapper {

    private final PostService postService;

    public UserDtoMapper(@Lazy PostService postService) {
        this.postService = postService;
    }

    UserDto userToUserDto(final User user) {
        UserDto userDTO = new UserDto();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setPhoto(user.getPhoto());
        return userDTO;
    }

    public User registerRequestToUser(RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(request.getPassword());
        user.setRegTime(LocalDateTime.now(ZoneOffset.UTC));
        user.setModerator(false);
        return user;
    }

    public Mono<UserLoginResponse> userToLoginResponse(User user) {
        UserLoginResponse loginDto = new UserLoginResponse();
        loginDto.setId(user.getId());
        loginDto.setName(user.getName());
        loginDto.setPhoto(user.getPhoto());
        loginDto.setEmail(user.getEmail());
        loginDto.setModeration(user.isModerator());
        loginDto.setSettings(true);
        Mono<UserLoginResponse> loginResponseMono = Mono.just(loginDto);
        Mono<Long> moderationCount = user.isModerator() ? postService.countPostsForModeration() : Mono.just(0L);
        return loginResponseMono.zipWith(moderationCount, (response, count) -> {
            response.setModerationCount(count);
            return response;
        });
    }
}
