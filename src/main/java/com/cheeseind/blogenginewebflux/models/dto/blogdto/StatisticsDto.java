package com.cheeseind.blogenginewebflux.models.dto.blogdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsDto {

    private long postsCount;
    private long likesCount;
    private long dislikesCount;
    private long viewsCount;
    private long firstPublication;
}
