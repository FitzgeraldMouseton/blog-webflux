package com.cheeseind.blogenginewebflux.exceptions;

import com.cheeseind.blogenginewebflux.models.dto.ErrorResponse;
import com.cheeseind.blogenginewebflux.models.dto.SimpleResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler
    public final ResponseEntity<SimpleResponseDto> inappropriateActionExHandler(final InappropriateActionException exception) {
        log.info(exception.getLocalizedMessage());
        return ResponseEntity.ok().body(new SimpleResponseDto(false));
    }

//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler({AbstractBadRequestException.class})
//    public final ResponseEntity<Object> handleApplicationExceptions(final AbstractBadRequestException ex) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//    }
//
//    @ExceptionHandler(AbstractUnauthenticatedException.class)
//    protected final ResponseEntity<Object> handleUnauthenticatedException(final AbstractUnauthenticatedException ex) {
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//    }
//
//    @ExceptionHandler(AbstractAuthException.class)
//    protected final ResponseEntity<ErrorResponse> handleAuthException(final AbstractAuthException ex) {
//        Map<String, String> errors = new HashMap<>();
//        errors.put(ex.prefix(), ex.getMessage());
//        return ResponseEntity.ok().body(new ErrorResponse(errors));
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected final ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.ok().body(new ErrorResponse(errors));
    }

//    @ExceptionHandler(FileUploadBase.FileSizeLimitExceededException.class)
//    public final ResponseEntity<ErrorResponse> handleFileSizeLimitExceededException(final FileUploadBase.FileSizeLimitExceededException ex) {
//        Map<String, String> errors = new HashMap<>();
//        errors.put(ex.getFieldName(), ex.getLocalizedMessage());
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errors));
//    }

//    @ExceptionHandler({RuntimeException.class})
//    public final void handleRuntimeExceptions(final RuntimeException ex) {
//        log.info("rtytyui");
//    }
}
