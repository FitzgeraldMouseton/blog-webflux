package com.cheeseind.blogenginewebflux.services;

import com.cheeseind.blogenginewebflux.models.GlobalSetting;
import com.cheeseind.blogenginewebflux.repositories.SettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;


@Slf4j
@Service@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;

    @Value("#{${settings}}")
    private Map<String, String> settings;

    @Value("${setting.multiuser}")
    private String multiuserSetting;
    @Value("${setting.premoderation}")
    private String premoderationSetting;

    public Flux<GlobalSetting> getSettings() {
        return settingsRepository.findAllBy();
    }

    public Mono<GlobalSetting> save(final GlobalSetting setting) {
        return settingsRepository.save(setting);
    }

    public Mono<Boolean> isMultiUserEnabled() {
        return isSettingEnabled(multiuserSetting);
    }

    public Mono<Boolean> isPremoderationEnabled() {
        return isSettingEnabled(premoderationSetting);
    }

    public Mono<GlobalSetting> getSettingByCode(final String code) {
        return settingsRepository.findByCode(code);
    }

    public Flux<GlobalSetting> fillSettings() {
        return Flux.fromIterable(settings.entrySet())
                .flatMap(entry -> settingsRepository.save(new GlobalSetting(entry.getKey(), entry.getValue(), true)));
    }

    public GlobalSetting setSetting(final String setting, final Boolean isActive) {
        return new GlobalSetting(setting, settings.get(setting), isActive);
    }

    private Mono<Boolean> isSettingEnabled(String settingCode) {
        return settingsRepository.findByCode(settingCode).map(GlobalSetting::getValue).defaultIfEmpty(true);
    }
}
