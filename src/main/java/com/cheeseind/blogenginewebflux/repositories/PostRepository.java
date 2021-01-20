package com.cheeseind.blogenginewebflux.repositories;


import com.cheeseind.blogenginewebflux.models.ModerationStatus;
import com.cheeseind.blogenginewebflux.models.Post;
import com.cheeseind.blogenginewebflux.models.User;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public interface PostRepository extends ReactiveMongoRepository<Post, String> {

    String MAKE_POST_VISIBLE_CONDITION = "'active' : true, 'moderationStatus' : 'ACCEPTED', 'time' : {$lt : new Date()}";

    // ======================== Recent posts
    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}", sort = "{'time' : -1}")
    Flux<Post> getRecentPosts(Pageable pageable);

    // ========================= Early posts
    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}", sort = "{'time' : 1}")
    Flux<Post> getEarlyPosts(Pageable pageable);

    // ========================= Popular posts
    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}", sort = "{'comments' : -1}")
    Flux<Post> getPopularPosts(Pageable pageable);

    // ========================= Best posts
    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}", sort = "{'usersLikedPost' : -1}")
    Flux<Post> getBestPosts(Pageable pageable);

    // ========================= Find posts by query
    @Query("{" + MAKE_POST_VISIBLE_CONDITION + "'text' : {$regex : ?0, $options: 'i'}, 'title' : {$regex : ?0, $options: 'i'}}" )
    Flux<Post> findPostsByQuery(String query, Pageable pageable);

//    List<Post> findAllByTextRegex("")

    // ========================= Find active accepted post by id
    Mono<Post> findByIdAndModerationStatusAndTimeBeforeAndActiveTrue(String id, ModerationStatus moderationStatus, ZonedDateTime date);

    // ========================= Find post by id for current user
    Mono<Post> findByIdAndUser(String id, User user);

    // ========================= Find posts by date
    @Query("{'active' : true, 'moderationStatus' : ACCEPTED, 'time' : {$gte : ?0, $lt : ?1}}")
    Flux<Post> findPostsByDate(ZonedDateTime startOfDay, ZonedDateTime startOfNextDay, Pageable pageable);

    // ========================= Find all dates of active posts by year
    @Query(value = "{'active' : true}, {'moderationStatus' : 'ACCEPTED'}, {{$year(time)} : ?0}}",
            sort = "{'time' : -1}")
    Flux<Post> findAllByYear(int year);

    // ======================== Count user's inactive posts count
    @Query(value = "{'user.id' : ?0, 'active' : false}", count = true)
    Mono<Long> countInactivePostsOfUser(Mono<String> userId);

    Mono<Post> findByTitle(Mono<String> title);

    // ======================== Count user's active posts count
    @Query(value = "{'user.id' : ?0, 'moderationStatus' : ?1, 'active' : true}", count = true)
    Mono<Long> countActivePostsOfUser(String userId, ModerationStatus moderationStatus);

    Mono<Post> countAllByTitle(Publisher<String> title);


    // ======================== Inactive posts of user
    @Query(value = "{'user.id' : ?0, 'active' : false}")
    Flux<Post> getCurrentUserInactivePosts(String userId, Pageable pageable);

    Flux<Post> findAllByUserIdAndActiveFalse(String userId, Pageable pageable);

    // ========================= Active posts of user
    @Query(value = "{'user.id' : ?0, 'moderationStatus' : ?1,'active' : true}")
    Flux<Post> getCurrentUserActivePosts(String userId, ModerationStatus moderationStatus, Pageable pageable);

    Flux<Post> findAllByUserIdAndModerationStatusAndActiveTrue(Mono<String> user, ModerationStatus moderationStatus, Pageable pageable);

    // ======================== Posts for moderation
    Flux<Post> findAllByActiveTrueAndModerationStatus(ModerationStatus moderationStatus, Pageable pageable);

    default Flux<Post> getPostsForModeration(ModerationStatus moderationStatus, Pageable pageable) {
        return findAllByActiveTrueAndModerationStatus(moderationStatus, pageable);
    }

    // ======================== Count posts for moderation
    Mono<Long> countAllByModerationStatusAndActiveTrue(ModerationStatus moderationStatus);

    @Query(value = "{ 'active' : true, moderationStatus : { $ne : ACCEPTED}, time : {$lt : new Date()} }", count = true)
    Mono<Long> countPostsForModeration();

    // ======================== Find first post
    Mono<Post> findFirstByModerationStatusAndActiveTrueOrderByTime(ModerationStatus moderationStatus);

    default Mono<Post> findFirstPost() {
        return findFirstByModerationStatusAndActiveTrueOrderByTime(ModerationStatus.ACCEPTED);
    }

    // ======================== Count visible posts
    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}", count = true)
    Mono<Long> countVisiblePosts();

    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}")
    Flux<Post> findVisiblePosts();

    // ======================== Find all tags in visible posts
    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + "} }",
            "{ '$project': { 'id' : '$tags' } }"
    })
    Flux<Flux<String>> findAllTags();

    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}")
    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + "} }",
            "{ '$project': { 'id' : {$year : '$time' } } }"
    })
    Flux<Integer> findAllYears();

    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}")
    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + "} }",
            "{ '$project': { 'id' : '$time' } }"
    })
    Flux<Date> findAllDatesWithPostsInYear(int year);

    @Query(value = "{ 'tags' : ?0, " + MAKE_POST_VISIBLE_CONDITION + "}")
    Flux<Post> findPostsByTag(String tag, Pageable pageable);

    // ========================
    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + "} }",
            "{ '$group' : { '_id' : null, 'total' : { $sum : 'viewCount' } } }"
    })
    Mono<Long> countAllPostsViews();

    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + "}", sort = "{'time' : 1}")
    Stream<Post> findFirstPostInBlog();

    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + "} }",
            "{ '$group' : { 'id' : null, 'total' : { $sum : {$size : '$usersLikedPost'} } } }"
    })
    Mono<Long> countAllLikes();

    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + "} }",
            "{ '$group' : { 'id' : null, 'total' : { $sum : {$size : '$usersDislikedPost'} } } }"
    })
    Mono<Long> countAllDislikes();

    // ========================
    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + ", 'user.id' : ?0} }",
            "{ '$group' : { '_id' : ?0, 'total' : { $sum : 'viewCount' } } }"
    })
    Mono<Long> countUserPostsViews(String userId);

    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + ", 'user.id' : ?0} }",
            "{ '$group' : { 'id' : ?0, 'total' : { $sum : {$size : '$usersLikedPost'} } } }"})
    Mono<Long> countLikesOfAllUserPosts(String userId);

    @Aggregation(pipeline = {
            "{ '$match' : {" + MAKE_POST_VISIBLE_CONDITION + ", 'user.id' : ?0} }",
            "{ '$group' : { 'id' : ?0, 'total' : { $sum : {$size : '$usersDislikedPost'} } } }"})
    Mono<Long> countDislikesOfAllUserPosts(String userId);

    // ======================== Find first post of user
    @Query(value = "{" + MAKE_POST_VISIBLE_CONDITION + ", 'user.id' : ?0}", sort = "{'time' : 1}")
    Flux<Post> findFirstPostOfUser(String userId);
}
