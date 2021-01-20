package com.cheeseind.blogenginewebflux.handlers;

import com.cheeseind.blogenginewebflux.mappers.CommentDtoMapper;
import com.cheeseind.blogenginewebflux.models.*;
import com.cheeseind.blogenginewebflux.models.dto.SimpleResponseDto;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.BlogInfo;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.CalendarDto;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.ModerationRequest;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.StatisticsDto;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.commentdto.CommentRequest;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.commentdto.CommentResponse;
import com.cheeseind.blogenginewebflux.models.dto.userdto.EditProfileRequest;
import com.cheeseind.blogenginewebflux.services.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneralHandler {

    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;
    private final SettingsService settingsService;
    private final CommentDtoMapper commentDtoMapper;
    private final BCryptPasswordEncoder encoder;

    private static final int FOLDER_NAME_LENGTH = 4;
    private static final int NUMBER_OF_FOLDERS_IN_IMAGE_PATH = 3;

    @Value("${blog_info.title}")
    private String title;
    @Value("${blog_info.subtitle}")
    private String subtitle;
    @Value("${blog_info.phone}")
    private String phone;
    @Value("${blog_info.email}")
    private String email;
    @Value("${blog_info.copyright}")
    private String copyright;
    @Value("${blog_info.copyright_form}")
    private String copyrightForm;

    @Value("${image.avatar_width}")
    private int avatarWidth;
    @Value("${image.avatar_height}")
    private int avatarHeight;
    @Value("${image.max_image_width}")
    private int maxImageWidth;
    @Value("${image.max_image_height}")
    private int maxImageHeight;
    @Value("${password.min_length}")
    private int passwordMinLength;
    @Value("${location.images}")
    private String imagesLocation;
    @Value("${location.avatars}")
    private String avatarsLocation;

    public Mono<ServerResponse> getBlogInfo(ServerRequest request) {
        BlogInfo blogInfo = BlogInfo.builder().title(title).subtitle(subtitle).phone(phone)
                .email(email).copyright(copyright).copyrightForm(copyrightForm).build();
        return ServerResponse.ok().body(Mono.just(blogInfo), BlogInfo.class);
    }

    public Mono<ServerResponse> getUserStatistics(ServerRequest request) {
        Mono<String> userIdMono = userService.getCurrentUserId();
        return userIdMono.flatMap(userId -> {
            Mono<Long> postsCountMono = postService.countUserAcceptedPosts(userId);
            Mono<Long> firstPostDateMono = postService.findFirstPostOfUser(userId).map(post -> post.getTime().toEpochSecond());
            Mono<Long> likesCountMono = postService.countLikesOfUserPosts(userId);
            Mono<Long> dislikesCountMono = postService.countDislikesOfUserPosts(userId);
            Mono<Long> viewsCountMono = postService.countUserPostsViews(userId);
            return collectStatistics(postsCountMono, firstPostDateMono, likesCountMono,
                    dislikesCountMono, viewsCountMono);
        })
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), StatisticsDto.class));
    }

    public Mono<ServerResponse> getBlogStatistics(ServerRequest request) {
        Mono<Long> postsCountMono = postService.countVisiblePosts();
        Mono<Long> firstPostDateMono = postService.findFirstPost().map(post -> post.getTime().toEpochSecond());
        Mono<Long> likesCountMono = postService.countAllLikes();
        Mono<Long> dislikesCountMono = postService.countAllDislikes();
        Mono<Long> viewsCountMono = postService.countAllPostsViews();

        return collectStatistics(postsCountMono, firstPostDateMono, likesCountMono, dislikesCountMono, viewsCountMono)
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), StatisticsDto.class));
    }

    public Mono<ServerResponse> calendar(ServerRequest request) {
        int year = Integer.parseInt(request.queryParam("year").get());
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        final Flux<Integer> allYears = postService.findAllYears().distinct();
        final Flux<Date> allDaysWithPostsInYear = postService.findAllDatesInYear(year);

        return Mono.from(allDaysWithPostsInYear
                .map(date -> date.toInstant().atZone(ZoneOffset.UTC))
                .collect(Collectors.groupingBy(dateFormat::format, Collectors.counting()))
                .flatMapMany(m -> allYears.collectList().map(list -> list.toArray(Integer[]::new))
                        .map(years -> new CalendarDto(years, m))))
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), CalendarDto.class));
    }

    public Mono<ServerResponse> getSettings(ServerRequest request) {
        return settingsService.getSettings()
                .switchIfEmpty(settingsService.fillSettings())
                .collectMap(GlobalSetting::getCode, GlobalSetting::getValue)
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), Map.class));
    }

    public Mono<ServerResponse> changeSettings(ServerRequest request) {

        Mono<Map<String, Boolean>> requestMono
                = request.bodyToMono(new ParameterizedTypeReference<>() {
        });
        return requestMono.map(Map::entrySet).flatMapMany(Flux::fromIterable)
                .doOnNext(entry -> settingsService.getSettingByCode(entry.getKey())
                        .map(setting -> {
                            String key = entry.getKey();
                            Boolean value = entry.getValue();
                            if (setting == null) {
                                setting = settingsService.setSetting(key, value);
                            }
                            if (!setting.getValue().equals(value)) {
                                setting.setValue(value);
                            }
                            return setting;
                        })
                        .flatMap(settingsService::save)
                        .subscribe())
                .then(ServerResponse.ok().body(Mono.empty(), Void.class));
    }

    public Mono<ServerResponse> addComment(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CommentRequest.class)
                .flatMap(request -> postService.findPostById(request.getPostId())
                        .doOnSuccess(this::incrementCommentsCountForPost)
                        .flatMap(postService::save)
                        .then(commentDtoMapper.commentDtoToComment(request))
                        .<Comment>handle((comment, sink) -> {
                            if (request.getParentId() != null && !request.getParentId().isEmpty()) {
                                comment.setParentId(request.getParentId());
                            }
                            sink.next(comment);
                        })
                        .flatMap(commentService::save)
                        .map(comment -> new CommentResponse(comment.getId())))
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), CommentResponse.class));
    }

    public Mono<ServerResponse> moderation(ServerRequest serverRequest) {
        Mono<User> moderator = userService.getCurrentUser();
        return serverRequest.bodyToMono(ModerationRequest.class).flatMap(request -> {
            Mono<Post> post = postService.findPostById(request.getPostId());
            return post.zipWith(moderator, (p, m) -> {
                if ("decline".equals(request.getDecision())) {
                    p.setModerationStatus(ModerationStatus.DECLINED);
                } else if ("accept".equals(request.getDecision())) {
                    p.setModerationStatus(ModerationStatus.ACCEPTED);
                }
                p.setModerator(m);
                return p;
            }).flatMap(postService::save);
        }).flatMap(response -> ServerResponse.ok().body(Mono.just(response), Post.class));
    }

    public Mono<ServerResponse> uploadImage(ServerRequest request) {
        return request.body(BodyExtractors.toMultipartData()).flatMap(parts -> {
            Map<String, Part> stringPartMap = parts.toSingleValueMap();
            FilePart image = (FilePart) stringPartMap.get("image");
            Path pathForUpload = getPathForUpload(imagesLocation, image.filename());
            return image.content().handle((dataBuffer, sink) -> {
                try {
                    BufferedImage bufferedImage = ImageIO.read(dataBuffer.asInputStream());
                    bufferedImage = getAdjustedPostImage(bufferedImage);
                    String imageFormat = getImageFormat(pathForUpload.toString());
                    ImageIO.write(bufferedImage, imageFormat, pathForUpload.toFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            })
                    .then(ServerResponse.ok().body(Mono.just("/" + pathForUpload.toString()), String.class));
        });
    }

    public Mono<ServerResponse> editProfile(ServerRequest request) {

        Mono<EditProfileRequest> editProfileRequestMono = request.bodyToMono(EditProfileRequest.class);
        Mono<User> userMono = getEditedUser(editProfileRequestMono);
        return userMono.flatMap(userService::save)
                .flatMap(user -> ServerResponse.ok().body(Mono.just(new SimpleResponseDto(true)), SimpleResponseDto.class));
    }

    //TODO совместить запросы
    @SneakyThrows
    public Mono<ServerResponse> editProfileWithPhoto(ServerRequest request) {

        Mono<EditProfileRequest> editProfileRequestMono = request.bodyToMono(EditProfileRequest.class);
        Mono<User> userMono = userService.getCurrentUser();
        return request.body(BodyExtractors.toMultipartData()).flatMap(parts -> {

            Map<String, Part> stringPartMap = parts.toSingleValueMap();
            FilePart image = (FilePart) stringPartMap.get("photo");
            Path pathForUpload = getPathForUpload(avatarsLocation, image.filename());

            return image.content().handle((dataBuffer, sink) -> {
                try {
                    BufferedImage avatar = ImageIO.read(dataBuffer.asInputStream());
                    avatar = getAdjustedAvatarPicture(avatar);
                    String imageFormat = getImageFormat(pathForUpload.toString());
                    ImageIO.write(avatar, imageFormat, pathForUpload.toFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            })
                    .then(userMono)
                    .<User>handle((user, sink) -> {
                        user.setPhoto(pathForUpload.toString());
                        sink.next(user);
                    })
                    .flatMap(userService::save)
                    .flatMap(r -> ServerResponse.ok().body(Mono.just(new SimpleResponseDto(true)), SimpleResponseDto.class));
        });
    }

    private Mono<StatisticsDto> collectStatistics(Mono<Long> postsCountMono, Mono<Long> firstPostDateMono, Mono<Long> likesCountMono, Mono<Long> dislikesCountMono, Mono<Long> viewsCountMono) {
        return Mono.just(new StatisticsDto())
                .zipWith(postsCountMono, (statisticsDto, postsCount) -> {
                    statisticsDto.setPostsCount(postsCount);
                    return statisticsDto;
                }).zipWith(firstPostDateMono, (statisticsDto, firstPostDate) -> {
                    statisticsDto.setFirstPublication(firstPostDate);
                    return statisticsDto;
                }).zipWith(likesCountMono, (statisticsDto, likesCount) -> {
                    statisticsDto.setLikesCount(likesCount);
                    return statisticsDto;
                }).zipWith(dislikesCountMono, (statisticsDto, dislikesCount) -> {
                    statisticsDto.setDislikesCount(dislikesCount);
                    return statisticsDto;
                }).zipWith(viewsCountMono, (statisticsDto, viewsCount) -> {
                    statisticsDto.setViewsCount(viewsCount);
                    return statisticsDto;
                });
    }

    private void incrementCommentsCountForPost(Post post) {
        post.setCommentsCount(post.getCommentsCount() + 1);
    }


    private Mono<User> getEditedUser(final Mono<EditProfileRequest> requestMono) {
        return userService.getCurrentUser().zipWith(requestMono, (user, request) -> {
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            String password = request.getPassword();
            if (request.getRemovePhoto() == 1 && user.getPhoto() != null) {
                removeUserAvatar(Mono.just(user)).subscribe();
            }
            if (password != null && password.length() >= passwordMinLength) {
                user.setPassword(encoder.encode(request.getPassword()));
            }
            return user;
        });
    }

    @SneakyThrows
    private Path getPathForUpload(final String location, final String fileName) {
        StringBuilder builder = new StringBuilder(location);
        for (int i = 0; i < NUMBER_OF_FOLDERS_IN_IMAGE_PATH; i++) {
            String rand = RandomStringUtils.randomAlphabetic(FOLDER_NAME_LENGTH).toLowerCase();
            builder.append(rand).append("/");
        }
        Path path = Path.of(builder.toString());
        Files.createDirectories(path);
        builder.append(fileName);
        return Path.of(builder.toString());
    }

    private Mono<User> removeUserAvatar(final Mono<User> user) {

        return user
                .handle((u, sink) -> {
                    removeOldAvatarFolder(u.getPhoto());
                    u.setPhoto("");
                    sink.next(u);
                });
    }

    private void removeOldAvatarFolder(String pathToPhoto) {
        if (pathToPhoto == null || pathToPhoto.isEmpty()) {
            return;
        }
        int startIndex = pathToPhoto.indexOf(avatarsLocation);
        int endIndex = pathToPhoto.indexOf("/", avatarsLocation.length() + 1);
        Path path = Path.of(pathToPhoto.substring(startIndex, endIndex));
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage getAdjustedAvatarPicture(BufferedImage image) {
        float height = image.getHeight();
        float width = image.getWidth();
        if (height > maxImageHeight || width > maxImageWidth) {
            if (image.getHeight() > avatarHeight || image.getWidth() > maxImageWidth) {
                image = getCroppedImage(image, avatarWidth, avatarHeight);
            }
        }
        return image;
    }

    private BufferedImage getAdjustedPostImage(BufferedImage image) {
        float height = image.getHeight();
        float width = image.getWidth();
        if (height > maxImageHeight || width > maxImageWidth) {
            float i;
            if (height > width) {
                i = width / height;
                image = getCroppedImage(image, (int) (maxImageWidth * i), maxImageHeight);
            } else {
                i = height / width;
                image = getCroppedImage(image, maxImageWidth, (int) (maxImageHeight * i));
            }
        }
        return image;
    }

    private String getImageFormat(String fileName) throws IOException {
        int i = fileName.lastIndexOf(".") + 1;
        return fileName.substring(i);
    }

    private BufferedImage getCroppedImage(BufferedImage bufferedImage, int width, int height) {

        BufferedImage preliminaryResizedImage = resizeImage(bufferedImage, width * 2,
                height * 2, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        bufferedImage = resizeImage(preliminaryResizedImage,
                width, height, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return bufferedImage;
    }

    private BufferedImage resizeImage(final BufferedImage sourceImage, final int width,
                                      final int height, final Object renderHint) {
        BufferedImage resizedImage = new BufferedImage(width, height, sourceImage.getType());
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderHint);
        graphics2D.drawImage(sourceImage, 0, 0, width, height, null);
        graphics2D.dispose();
        return resizedImage;
    }
}
