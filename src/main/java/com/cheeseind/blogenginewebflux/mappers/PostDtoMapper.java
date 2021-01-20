package com.cheeseind.blogenginewebflux.mappers;

import com.cheeseind.blogenginewebflux.models.Comment;
import com.cheeseind.blogenginewebflux.models.ModerationStatus;
import com.cheeseind.blogenginewebflux.models.Post;
import com.cheeseind.blogenginewebflux.models.User;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.ModerationResponse;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.commentdto.CommentDTO;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.postdto.AddPostRequest;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.postdto.PostDto;
import com.cheeseind.blogenginewebflux.models.constants.PostConstraints;
import com.cheeseind.blogenginewebflux.services.CommentService;
import com.cheeseind.blogenginewebflux.services.PostService;
import com.cheeseind.blogenginewebflux.services.SettingsService;
import com.cheeseind.blogenginewebflux.services.UserService;
import lombok.extern.slf4j.Slf4j;
//import org.jsoup.Jsoup;
import org.jsoup.Jsoup;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
//@RequiredArgsConstructor
public class PostDtoMapper {

    private final PostService postService;
    private final UserDtoMapper userDtoMapper;
    private final UserService userService;
    private final SettingsService settingsService;
    private final CommentService commentService;
    private final CommentDtoMapper commentDtoMapper;

    public PostDtoMapper(@Lazy PostService postService,
                         UserDtoMapper userDtoMapper,
                         UserService userService,
                         SettingsService settingsService,
                         CommentService commentService,
                         CommentDtoMapper commentDtoMapper) {
        this.postService = postService;
        this.userDtoMapper = userDtoMapper;
        this.userService = userService;
        this.settingsService = settingsService;
        this.commentService = commentService;
        this.commentDtoMapper = commentDtoMapper;
    }

    public PostDto postToPostDto(final Post post) {
        PostDto postDto = new PostDto();
        if (post != null) {
            postDto.setId(post.getId());
            postDto.setTimestamp(post.getTime().toEpochSecond());
            postDto.setTitle(post.getTitle());
            postDto.setAnnounce(getAnnounce(post));
            postDto.setLikeCount(post.getUsersLikedPost().size());
            postDto.setDislikeCount(post.getUsersDislikedPost().size());
            postDto.setCommentCount(post.getCommentsCount());
            postDto.setViewCount(post.getViewCount());
            postDto.setUser(userDtoMapper.userToUserDto(post.getUser()));
            postDto.setUserId(post.getUserId());
        }
        return postDto;
    }

    public Mono<PostDto> singlePostToPostDto(final Post post) {
        PostDto postDTO = postToPostDto(post);
        postDTO.setText(post.getText());
        postDTO.setActive(post.isActive());
        postDTO.setTags(post.getTags().toArray(String[]::new));
        Mono<List<CommentDTO>> commentsMono = Mono.from(commentService.findAllCommentOfPost(post.getId())
                .map(commentDtoMapper::commentToCommentDto)
                .collect(Collectors.toList()));
        return Mono.just(postDTO).zipWith((commentsMono), (dto, comments) -> {
            dto.setComments(comments);
            return dto;
        });
    }

    public Mono<Post> addPostRequestToPost(final Mono<AddPostRequest> request) {
        Mono<Post> post = Mono.just(new Post());
        return postDtoToPost(post, request);
    }

    public Mono<Post> editPostRequestToPost(final String id, final Mono<AddPostRequest> request) {
        Mono<Post> post = postService.findPostById(id);
        return postDtoToPost(post, request);
    }

    public Mono<Post> postDtoToPost(final Mono<Post> postMono, final Mono<AddPostRequest> requestMono) {

        Mono<User> userMono = userService.getCurrentUser();
        return postMono.zipWith(requestMono, (post, request) -> {
            post.setTitle(request.getTitle());
            post.setText(request.getText());
            setTimeToPost(post, request);
            post.setActive(request.isActive());
            request.getTagNames().forEach(tag -> post.getTags().add(tag.toUpperCase()));
            return post;
        }).zipWith(settingsService.isPremoderationEnabled(), (post, isEnabled) -> {
            if (isEnabled) {
                post.setModerationStatus(ModerationStatus.NEW);
            } else {
                post.setModerationStatus(ModerationStatus.ACCEPTED);
            }
            return post;
        }).zipWith(userMono, (post, user) -> {
            if (user.isModerator())
                post.setModerationStatus(ModerationStatus.ACCEPTED);
            post.setUser(user);
            post.setUserId(user.getId());
            return post;
        });
    }

    public ModerationResponse postToModerationResponse(final Post post) {
        ModerationResponse response = new ModerationResponse();
        response.setId(post.getId());
        response.setTimestamp(post.getTime().toEpochSecond());
        response.setUser(userDtoMapper.userToUserDto(post.getUser()));
        response.setTitle(post.getTitle());
        response.setAnnounce(getAnnounce(post));
        return response;
    }

    private String getAnnounce(final Post post) {
        String announce = Jsoup.parse(post.getText()).text();
        announce = (announce.length() > PostConstraints.ANNOUNCE_LENGTH)
                ? announce.substring(0, PostConstraints.ANNOUNCE_LENGTH) : announce;
        announce = (announce.matches(".*[.,?!]")) ? announce + ".." : announce + "...";
        return announce;
    }

    private void setTimeToPost(Post post, AddPostRequest request) {
        ZonedDateTime requestTime
                = Instant.ofEpochSecond(request.getTimestamp()).atZone(ZoneOffset.UTC);
        ZonedDateTime postTime = requestTime
                .isBefore(ZonedDateTime.now(ZoneOffset.UTC)) ? ZonedDateTime.now(ZoneOffset.UTC) : requestTime;
        post.setTime(postTime);
    }
}
