package com.cheeseind.blogenginewebflux.handlers;

import com.cheeseind.blogenginewebflux.exceptions.InappropriateActionException;
import com.cheeseind.blogenginewebflux.exceptions.InvalidParameterException;
import com.cheeseind.blogenginewebflux.exceptions.PageNotFoundException;
import com.cheeseind.blogenginewebflux.mappers.PostDtoMapper;
import com.cheeseind.blogenginewebflux.models.ModerationStatus;
import com.cheeseind.blogenginewebflux.models.Post;
import com.cheeseind.blogenginewebflux.models.User;
import com.cheeseind.blogenginewebflux.models.dto.SimpleResponseDto;
import com.cheeseind.blogenginewebflux.models.dto.Validatable;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.postdto.AddPostRequest;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.postdto.PostDto;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.postdto.PostsInfoResponse;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.votedto.VoteRequest;
import com.cheeseind.blogenginewebflux.repositories.PostRepository;
import com.cheeseind.blogenginewebflux.services.PostService;
import com.cheeseind.blogenginewebflux.services.UserService;
import com.cheeseind.blogenginewebflux.util.validators.AddPostRequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostHandler {

    private final PostService postService;
    private final PostDtoMapper postDtoMapper;
    private final PostRepository postRepository;
    private final UserService userService;
    private final AddPostRequestValidator addPostRequestValidator;

    /*
    Вследствие того, что фронт принимает конкретную dto, приходится в итоге собирать flux в лист,
    а потом из этого листа и его размера собирать dto и заворачивать в mono. Второе возможное решение
    в dto иметь flux, а не лист, но не заворачивать это в mono. Оба варианта плохие, но тут вынужденная
    мера из-за фронта.
    Здесь показано, как можно делить flux на куски и обрабатыавть их параллельно. Это дело необязтельное,
    и вообще, как известно, может даже замедлить процесс, но просто в целях обучения.
     */
    public Mono<ServerResponse> getPosts(ServerRequest serverRequest) {
        int offset = getPaginationParam(serverRequest, "offset");
        int limit = getPaginationParam(serverRequest, "limit");
        String mode = serverRequest.queryParam("mode").get();

        return getAllPostsInCorrectOrder(mode, PageRequest.of(offset / limit, limit))
                .buffer(Runtime.getRuntime().availableProcessors())
                .flatMap(bufferedChunk -> Flux.fromIterable(bufferedChunk)
                        .map(postDtoMapper::postToPostDto).subscribeOn(Schedulers.parallel()))
                .collectList()
                .map(response -> new PostsInfoResponse<>((long) response.size(), response))
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), PostsInfoResponse.class));
    }

    public Mono<ServerResponse> getCurrentUserPosts(ServerRequest serverRequest) {
        int offset = getPaginationParam(serverRequest, "offset");
        int limit = getPaginationParam(serverRequest, "limit");
        String status = serverRequest.queryParam("status").get();

        Pageable pageable = PageRequest.of(offset / limit, limit);
        return userService.getCurrentUserId()
                .flatMapMany(userId -> findUserPosts(status, userId, pageable))
                .map(postDtoMapper::postToPostDto)
                .collectList()
                .map(response -> new PostsInfoResponse<>((long) response.size(), response))
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), PostsInfoResponse.class));
    }

    public Mono<ServerResponse> getPostsForModeration(ServerRequest serverRequest) {
        int offset = getPaginationParam(serverRequest, "offset");
        int limit = getPaginationParam(serverRequest, "limit");
        String status = serverRequest.queryParam("status").get();

        Pageable pageable = PageRequest.of(offset / limit, limit);
        return userService.getCurrentUser()
                .flatMapMany(u -> findPostsForModeration(status, pageable))
                .map(postDtoMapper::postToModerationResponse)
                .collectList()
                .map(response -> new PostsInfoResponse<>((long) response.size(), response))
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), PostsInfoResponse.class));
    }

    public Mono<ServerResponse> getPostById(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        Mono<Post> postMono = postRepository.findById(id);
        Mono<User> currentUserMono = userService.getCurrentUser().defaultIfEmpty(new User());
        return Mono.zip(postMono, currentUserMono)
                .<Post>handle((tuple2, sink) -> {
                    Post post = tuple2.getT1();
                    User postAuthor = post.getUser();
                    User currentUser = tuple2.getT2();
                    if (isCurrentUserAnonymous(currentUser)
                            || !(currentUser.getId().equals(postAuthor.getId())
                            || currentUser.isModerator())) {
                        incrementPostViewsCount(post);
                    }
                    sink.next(post);
                })
                .flatMap(postRepository::save)
                .flatMap(postDtoMapper::singlePostToPostDto)
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), PostDto.class));
    }

    public Mono<ServerResponse> addPost(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(AddPostRequest.class)
                .doOnNext(req -> validate(req, addPostRequestValidator))
                .flatMap(r -> postDtoMapper.addPostRequestToPost(Mono.just(r)))
                .flatMap(postRepository::save)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .body(Mono.just(new SimpleResponseDto(true)), SimpleResponseDto.class));
    }

    public Mono<ServerResponse> editPost(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        Mono<AddPostRequest> requestMono = serverRequest.bodyToMono(AddPostRequest.class);
        return postDtoMapper.editPostRequestToPost(id, requestMono)
                .flatMap(postRepository::save)
                .map(post -> new SimpleResponseDto(true))
                .flatMap(response -> ServerResponse.ok()
                        .body(Mono.just(new SimpleResponseDto(true)), SimpleResponseDto.class));
    }

    public Mono<ServerResponse> likePost(ServerRequest serverRequest) {
        Mono<String> userIdMono = userService.getCurrentUserId();
        Mono<Post> postMono = serverRequest.bodyToMono(VoteRequest.class)
                .map(VoteRequest::getPostId)
                .flatMap(postRepository::findById)
                .switchIfEmpty(Mono.error(new PageNotFoundException("Страница не найдена")));

        return Mono.zip(postMono, userIdMono)
                .filterWhen(tuple -> Mono.just(!tuple.getT1().getUser().getId().equals(tuple.getT2())))
                .switchIfEmpty(Mono.error(new InappropriateActionException("Самолайк")))
                .filterWhen(tuple -> Mono.just(!tuple.getT1().getUsersLikedPost().contains(tuple.getT2())))
                .switchIfEmpty(Mono.error(new InappropriateActionException("Повторный лайк")))
                .<Tuple2<Post, String>>handle((tuple2, sink) -> {
                    Post post = tuple2.getT1();
                    String userId = tuple2.getT2();
                    post.getUsersDislikedPost().removeIf(userId::equals);
                    post.getUsersLikedPost().add(userId);
                    sink.next(tuple2);
                })
                .map(Tuple2::getT1)
                .flatMap(postRepository::save)
                .flatMap(response -> ServerResponse.ok()
                        .body(Mono.just(new SimpleResponseDto(true)), SimpleResponseDto.class));
    }

    public Mono<ServerResponse> dislikePost(ServerRequest serverRequest) {
        Mono<String> userIdMono = userService.getCurrentUserId();
        Mono<Post> postMono = serverRequest.bodyToMono(VoteRequest.class)
                .map(VoteRequest::getPostId)
                .flatMap(postRepository::findById)
                .switchIfEmpty(Mono.error(new PageNotFoundException("Страница не найдена")));

        return Mono.zip(postMono, userIdMono)
                .filterWhen(tuple -> Mono.just(!tuple.getT1().getUser().getId().equals(tuple.getT2())))
                .switchIfEmpty(Mono.error(new InappropriateActionException("Самодизлайк")))
                .filterWhen(tuple -> Mono.just(!tuple.getT1().getUsersDislikedPost().contains(tuple.getT2())))
                .switchIfEmpty(Mono.error(new InappropriateActionException("Повторный дизлайк")))
                .<Tuple2<Post, String>>handle((tuple2, sink) -> {
                    Post post = tuple2.getT1();
                    String userId = tuple2.getT2();
                    post.getUsersLikedPost().removeIf(userId::equals);
                    post.getUsersDislikedPost().add(userId);
                    sink.next(tuple2);
                })
                .map(Tuple2::getT1)
                .flatMap(postRepository::save)
                .flatMap(response -> ServerResponse.ok()
                        .body(Mono.just(new SimpleResponseDto(true)), SimpleResponseDto.class));
    }

    public Mono<ServerResponse> searchPost(ServerRequest request) {
        int offset = getPaginationParam(request, "offset");
        int limit = getPaginationParam(request, "limit");
        String query = request.queryParam("query").orElseThrow(PageNotFoundException::new);
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Flux<Post> posts = query == null ? postRepository.getRecentPosts(pageable) : postRepository.findPostsByQuery(query, pageable);
        return posts.map(postDtoMapper::postToPostDto)
                .collectList()
                .map(response -> new PostsInfoResponse<>((long) response.size(), response))
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), PostsInfoResponse.class));
    }


    public Mono<ServerResponse> getPostsByDate(ServerRequest request) {
        int offset = getPaginationParam(request, "offset");
        int limit = getPaginationParam(request, "limit");
        String date = request.queryParam("date").get();
        ZonedDateTime zonedDateTime = parseDateFromParam(date);

        return postRepository.findPostsByDate(zonedDateTime, zonedDateTime.plusDays(1), PageRequest.of(offset / limit, limit))
                .map(postDtoMapper::postToPostDto)
                .collectList()
                .map(postDtos -> new PostsInfoResponse<>((long) postDtos.size(), postDtos))
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), PostsInfoResponse.class));
    }

    public Mono<ServerResponse> getPostsByTag(ServerRequest request) {
        int offset = getPaginationParam(request, "offset");
        int limit = getPaginationParam(request, "limit");
        String tag = request.queryParam("tag").get();
        return postRepository.findPostsByTag(tag, PageRequest.of(offset / limit, limit))
                .map(postDtoMapper::postToPostDto)
                .collectList()
                .map(response -> new PostsInfoResponse<>((long) response.size(), response))
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), PostsInfoResponse.class));
    }


    private int getPaginationParam(ServerRequest request, String param) {
        return Integer.parseInt(request.queryParam(param).get());
    }

    private void validate(Validatable request, Validator validator) {
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);
        if (errors.hasErrors()) {
            throw new InvalidParameterException("fuck", errors);
        }
    }

    private Flux<Post> getAllPostsInCorrectOrder(String mode, Pageable pageable) {
        Flux<Post> posts;
        posts = switch (mode) {
            case "recent" -> postRepository.getRecentPosts(pageable);
            case "early" -> postRepository.getEarlyPosts(pageable);
            case "popular" -> postRepository.getPopularPosts(pageable);
            case "best" -> postRepository.getBestPosts(pageable);
            default -> throw new IllegalArgumentException("Wrong argument 'mode': " + mode);
        };
        return posts;
    }

    private Flux<Post> findUserPosts(String status, String userId, Pageable pageable) {
        return switch (status) {
            case "inactive" -> postRepository.findAllByUserIdAndActiveFalse(userId, pageable);
            case "pending" -> postRepository.getCurrentUserActivePosts(userId, ModerationStatus.NEW, pageable);
            case "declined" -> postRepository.getCurrentUserActivePosts(userId, ModerationStatus.DECLINED, pageable);
            case "published" -> postRepository.getCurrentUserActivePosts(userId, ModerationStatus.ACCEPTED, pageable);
            default -> throw new IllegalArgumentException("Wrong argument 'status': " + status);
        };
    }

    private Flux<Post> findPostsForModeration(String status, Pageable pageable) {
        return switch (status) {
            case "new" -> postRepository.getPostsForModeration(ModerationStatus.NEW, pageable);
            case "declined" -> postRepository.getPostsForModeration(ModerationStatus.DECLINED, pageable);
            case "accepted" -> postRepository.getPostsForModeration(ModerationStatus.ACCEPTED, pageable);
            default -> throw new IllegalArgumentException("Wrong argument 'status': " + status);
        };
    }

    private boolean isCurrentUserAnonymous(User currentUser) {
        return currentUser.getId() == null;
    }

    private void incrementPostViewsCount(Post post) {
        post.setViewCount(post.getViewCount() + 1);
    }

    private ZonedDateTime parseDateFromParam(String date) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateQuery = LocalDate.parse(date, dateFormat);
        return dateQuery.atStartOfDay(ZoneOffset.UTC);
    }

}
