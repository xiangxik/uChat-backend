package com.uchat.backend.common;

import com.uchat.backend.config.UChatProperties;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private static final Locale ZH_LOCALE = Locale.SIMPLIFIED_CHINESE;
    private static final Locale EN_LOCALE = Locale.ENGLISH;

    private final UChatProperties properties;
    private final MessageSource messageSource;

    public ConfigController(UChatProperties properties, MessageSource messageSource) {
        this.properties = properties;
        this.messageSource = messageSource;
    }

    @GetMapping
    public AppConfigResponse getConfig(
            @RequestParam(name = "locale", required = false) String requestedLocale,
            @RequestHeader(name = "Accept-Language", required = false) String acceptLanguage
    ) {
        Locale locale = resolveLocale(requestedLocale, acceptLanguage);
        String localeCode = locale.getLanguage();

        return new AppConfigResponse(
                properties.appName(),
                properties.defaultLocale(),
                localeCode,
                message("uchat.ui.initialBotMessage", locale),
                message("uchat.ui.messagePlaceholder", locale),
                message("uchat.ui.sendLabel", locale),
                message("uchat.ui.tipText", locale),
                message("uchat.ui.thankYouText", locale),
                message("uchat.ui.thinkingText", locale),
                message("uchat.ui.serviceCenterText", locale),
                message("uchat.ui.onlineText", locale),
                ratingLabels(locale),
                properties.websocketEndpoint(),
                properties.chatSendDestination(),
                properties.chatMessageSubscription(),
                properties.chatErrorSubscription()
        );
    }

    private Locale resolveLocale(String requestedLocale, String acceptLanguage) {
        if (requestedLocale != null && !requestedLocale.isBlank()) {
            return toSupportedLocale(requestedLocale);
        }
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            return toSupportedLocale(acceptLanguage);
        }
        return toSupportedLocale(properties.defaultLocale());
    }

    private Locale toSupportedLocale(String value) {
        return value != null && value.trim().toLowerCase(Locale.ROOT).startsWith("zh")
                ? ZH_LOCALE
                : EN_LOCALE;
    }

    private String message(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }

    private List<String> ratingLabels(Locale locale) {
        return List.of(
                message("uchat.ui.ratingLabel.1", locale),
                message("uchat.ui.ratingLabel.2", locale),
                message("uchat.ui.ratingLabel.3", locale),
                message("uchat.ui.ratingLabel.4", locale),
                message("uchat.ui.ratingLabel.5", locale)
        );
    }
}
