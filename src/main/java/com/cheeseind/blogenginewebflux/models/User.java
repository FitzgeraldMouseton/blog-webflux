package com.cheeseind.blogenginewebflux.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Slf4j
@Data
@Document(collection = "users")
public class User implements UserDetails {

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    @Id
    private String id;

    private boolean isModerator;

    private LocalDateTime regTime;

    private String name;

    private String email;

    // Пароли не должны попадать в json
    @JsonIgnore
    private String password;

    private String code;

    private String photo;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setPhoto(String photo) {
        this.photo = "/" + photo;
    }
}
