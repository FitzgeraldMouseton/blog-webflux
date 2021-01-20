package com.cheeseind.blogenginewebflux.handlers;

import com.cheeseind.blogenginewebflux.exceptions.PageNotFoundException;
import com.cheeseind.blogenginewebflux.models.Post;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.tagdto.SingleTagDto;
import com.cheeseind.blogenginewebflux.models.dto.blogdto.tagdto.TagsResponse;
import com.cheeseind.blogenginewebflux.services.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TagHandler {

    private final PostService postService;

    @Value("${tag.min_weight}")
    private float tagMinWeight;

    public Mono<ServerResponse> getTags(ServerRequest request) {
        return postService.findVisiblePosts()
                .switchIfEmpty(Mono.error(new PageNotFoundException()))
                .filter(Objects::nonNull)
                .map(Post::getTags)
                .flatMap(Flux::fromIterable)
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()))
                .map(tagsWithCounts -> Tuples.of(tagsWithCounts, Collections.max(tagsWithCounts.values())))
                .<List<SingleTagDto>>handle((tuple2, sink) -> {
                    List<SingleTagDto> tagDtos = new ArrayList<>();
                    tuple2.getT1().forEach((tagName, tagCount) -> {
                        float tagWeight = Precision.round((float) tagCount / tuple2.getT2(), 3);
                        if (tagWeight < tagMinWeight)
                            tagWeight = tagMinWeight;
                        tagDtos.add(new SingleTagDto(tagName, tagWeight));
                    });
                    tagDtos.sort(Comparator.comparing(SingleTagDto::getWeight).reversed());
                    sink.next(tagDtos);
                })
                .map(std -> std.toArray(new SingleTagDto[std.size()]))
                .map(TagsResponse::new)
                .flatMap(response -> ServerResponse.ok().body(Mono.just(response), TagsResponse.class));
    }
}
