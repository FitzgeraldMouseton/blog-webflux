package com.cheeseind.blogenginewebflux.mappers;

import com.cheeseind.blogenginewebflux.models.Comment;
import com.cheeseind.blogenginewebflux.models.User;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.commentdto.CommentDTO;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.commentdto.CommentRequest;
import com.cheeseind.blogenginewebflux.services.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

@Data
@Component
@RequiredArgsConstructor
public class CommentDtoMapper {

    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    public CommentDTO commentToCommentDto(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setTimestamp(comment.getTime().toEpochSecond());
        commentDTO.setUser(userDtoMapper.userToUserDto(comment.getUser()));
        commentDTO.setText(comment.getText());
        return commentDTO;
    }

    public Mono<Comment> commentDtoToComment(CommentRequest request) {
        Mono<User> userMono = userService.getCurrentUser();
        Mono<Comment> commentMono = Mono.just(new Comment());
        return commentMono.zipWith(userMono, (comment, user) -> {
            comment.setText(request.getText());
            comment.setUser(user);
            comment.setTime(ZonedDateTime.now());
            comment.setPostId(request.getPostId());
            return comment;
        });
    }
}
