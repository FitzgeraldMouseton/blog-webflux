package com.cheeseind.blogenginewebflux.models.dto.blogdto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlogInfo {

    private String title;
    private String subtitle;
    private String phone;
    private String email;
    private String copyright;
    private String copyrightForm;
}
