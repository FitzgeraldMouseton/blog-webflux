package com.cheeseind.blogenginewebflux.handlers;

import com.cheeseind.blogenginewebflux.exceptions.InvalidParameterException;
import com.cheeseind.blogenginewebflux.mail.EmailServiceImpl;
import com.cheeseind.blogenginewebflux.mappers.UserDtoMapper;
import com.cheeseind.blogenginewebflux.models.CaptchaCode;
import com.cheeseind.blogenginewebflux.models.User;
import com.cheeseind.blogenginewebflux.models.dto.SimpleResponseDto;
import com.cheeseind.blogenginewebflux.models.dto.Validatable;
import com.cheeseind.blogenginewebflux.models.dto.authdto.*;
import com.cheeseind.blogenginewebflux.services.CaptchaService;
import com.cheeseind.blogenginewebflux.services.UserService;
import com.cheeseind.blogenginewebflux.validators.RegisterUserValidator;
import com.cheeseind.blogenginewebflux.validators.SetNewPasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final CaptchaService captchaService;
    public final UserService userService;
    private final RegisterUserValidator registerUserValidator;
    private final SetNewPasswordValidator setNewPasswordValidator;
    private final UserDtoMapper userDtoMapper;
    private final EmailServiceImpl emailService;
    private final BCryptPasswordEncoder encoder;

    @Value("${restore_code.code_length}")
    private int restoreCodeLength;
    @Value("${restore_code.path}")
    private String restoreCodePath;
    @Value("${captcha.code_length}")
    private int codeLength;
    @Value("${captcha.secret_length}")
    private int secretCodeLength;
    @Value("${captcha.text_size}")
    private int captchaTextSize;
    @Value("${captcha.width}")
    private int captchaWidth;
    @Value("${captcha.height}")
    private int captchaHeight;
    @Value("${captcha.frame_width}")
    private int captchaFrameWidth;
    @Value("${captcha.frame_height}")
    private int captchaFrameHeight;

    public Mono<ServerResponse> register(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(RegisterRequest.class)
                .doOnNext(req -> validate(req, registerUserValidator))
                // Шифрование пароля при помощи BCrypt - долгая (намеренно) блокирующая операция,
                // поэтому ее лучше выполнять параллельно
                .flatMap(r -> Mono.just(r).map(userDtoMapper::registerRequestToUser).subscribeOn(Schedulers.parallel()))
                .doOnNext(user -> user.setPassword(encoder.encode(user.getPassword())))
                .zipWith(userService.isFirstUser(), (user, isFirst) -> {
                    if (isFirst)
                        user.setModerator(true);
                    return user;
                })
                .flatMap(userService::save)
                .flatMap(registerRequest ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Mono.just(new SimpleResponseDto(true)), SimpleResponseDto.class));
    }

    public Mono<ServerResponse> check(ServerRequest serverRequest) {

        return userService.getCurrentUser()
                .flatMap(userDtoMapper::userToLoginResponse)
                .flatMap(response ->
                        ServerResponse.ok()
                                .body(Mono.just(new AuthenticationResponse(true, response)),
                                        AuthenticationResponse.class));
    }

    public Mono<ServerResponse> captcha(ServerRequest serverRequest) {

        CaptchaDto captchaDto = new CaptchaDto();
        String secretCode = RandomStringUtils.randomAlphanumeric(secretCodeLength);
        String code = RandomStringUtils.randomAlphanumeric(codeLength);
        BufferedImage image = generateCaptchaImage(code);

        return Mono.fromCallable(() -> {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", os);
                captchaDto.setSecret(secretCode);
                captchaDto.setImage("data:image/png;charset=utf-8;base64, "
                        + java.util.Base64.getEncoder().encodeToString(os.toByteArray()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return captchaDto;
        }).subscribeOn(Schedulers.boundedElastic())
                .map(c -> new CaptchaCode(code, secretCode, LocalDateTime.now(ZoneOffset.UTC)))
                .flatMap(captchaService::save)
                .map(mono -> captchaDto)
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), CaptchaDto.class));
    }

    public Mono<ServerResponse> sendRestorePassCode(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(RestorePasswordRequest.class)
                .flatMap(request -> userService.findByEmail(request.getEmail()))
                .<User>handle((user, sink) -> {
                    String code = createPasswordRestoreCode();
                    user.setCode(code);
                    String message = "Для восстановления пароля перейдите по ссылке "
                            + restoreCodePath + user.getCode();
                    emailService.send(user.getEmail(), "restore", message);
                    sink.next(user);
                })
                .flatMap(userService::save)
                .flatMap(response -> ServerResponse.ok()
                        .body(Mono.just(new SimpleResponseDto(true)), SimpleResponseDto.class));
    }

    public Mono<ServerResponse> setNewPassword(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(SetPassRequest.class)
                .doOnNext(req -> validate(req, setNewPasswordValidator))
                .flatMap(request -> userService.findByCode(request.getCode())
                        .<User>handle((user, sink) -> {
                            user.setPassword(encoder.encode(request.getPassword()));
                            user.setCode(null);
                            sink.next(user);
                        })
                        .flatMap(userService::save))
                .flatMap(response -> ServerResponse.ok()
                        .body(Mono.just(new SimpleResponseDto(true)), SimpleResponseDto.class));
    }

    // См. пояснение в комментарии к RegisterUserValidator
    private void validate(Validatable request, Validator validator) {
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (errors.hasErrors()) {
            throw new InvalidParameterException("fuck", errors);
        }
    }

    private BufferedImage generateCaptchaImage(final String code) {
        BufferedImage image = new BufferedImage(captchaFrameWidth, captchaFrameHeight, BufferedImage.OPAQUE);
        Graphics graphics = image.createGraphics();
        graphics.setFont(new Font("Arial", Font.BOLD, captchaTextSize));
        graphics.setColor(Color.GRAY);
        graphics.fillRect(0, 0, captchaFrameWidth, captchaFrameHeight);
        graphics.setColor(Color.BLACK);
        graphics.drawString(code, captchaWidth, captchaHeight);
        return image;
    }

    private String createPasswordRestoreCode() {
        String code = RandomStringUtils.randomAlphanumeric(restoreCodeLength);
        code += getEncodedCurrentTime();
        return code;
    }

    private String getEncodedCurrentTime() {
        long currentTimeMilli = Instant.now(Clock.systemUTC()).toEpochMilli();
        String time = Long.toString(currentTimeMilli);
        return Base64.getEncoder().encodeToString(time.getBytes());
    }
}
