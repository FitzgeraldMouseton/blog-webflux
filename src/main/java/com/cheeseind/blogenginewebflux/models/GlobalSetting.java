package com.cheeseind.blogenginewebflux.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Data
@Document(collection = "global_settings")
public class GlobalSetting {

    @Id
    private String id;

    @NotNull
    private String code;

    @NotNull
    private String name;

    @NotNull
    private Boolean value;

    public GlobalSetting() {
    }

    public GlobalSetting(@NotNull String code, @NotNull String name, @NotNull Boolean value) {
        this.code = code;
        this.name = name;
        this.value = value;
    }
}
