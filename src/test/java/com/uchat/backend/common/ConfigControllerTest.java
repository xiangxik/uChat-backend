package com.uchat.backend.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.uchat.backend.config.UChatProperties;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

class ConfigControllerTest {

    @Test
    void returnsDefaultLocaleFrontendConfigWhenLocaleIsNotProvided() {
        UChatProperties properties = new UChatProperties(
                "uChat",
                "en",
                List.of("http://localhost:5173", "http://127.0.0.1:5173"),
                "/app",
                "/user",
                List.of("/topic", "/queue"),
                "/chat.send",
                "/ws",
                "/app/chat.send",
                "/user/queue/chat.messages",
                "/user/queue/chat.errors"
        );
        StaticMessageSource messageSource = new StaticMessageSource();
        registerMessages(messageSource);
        ConfigController controller = new ConfigController(properties, messageSource);

        AppConfigResponse response = controller.getConfig(null, null);

        assertThat(response.appName()).isEqualTo("uChat");
        assertThat(response.defaultLocale()).isEqualTo("en");
        assertThat(response.locale()).isEqualTo("en");
        assertThat(response.initialBotMessage()).isEqualTo("Welcome to the uChat Enterprise Service Center. Tell us what you would like to ask and we will arrange a specialist to follow up.");
        assertThat(response.messagePlaceholder()).isEqualTo("For example: I want to learn about business loan options and eligibility");
        assertThat(response.sendLabel()).isEqualTo("Send message");
        assertThat(response.tipText()).isEqualTo("Note: Responses on this page are for demo purposes only. Please refer to official documents and human support for formal business matters.");
        assertThat(response.thankYouText()).isEqualTo("Thank you for your feedback");
        assertThat(response.thinkingText()).isEqualTo("Advisor is checking the details...");
        assertThat(response.serviceCenterText()).isEqualTo("Service Center");
        assertThat(response.onlineText()).isEqualTo("Online");
        assertThat(response.ratingLabels()).containsExactly("Poor", "Fair", "Good", "Very good", "Excellent");
        assertThat(response.webSocketEndpoint()).isEqualTo("/ws");
        assertThat(response.chatSendDestination()).isEqualTo("/app/chat.send");
        assertThat(response.chatMessageSubscription()).isEqualTo("/user/queue/chat.messages");
        assertThat(response.chatErrorSubscription()).isEqualTo("/user/queue/chat.errors");
    }

    @Test
    void returnsRequestedLocaleFrontendConfig() {
        UChatProperties properties = new UChatProperties(
                "uChat",
                "en",
                List.of("http://localhost:5173", "http://127.0.0.1:5173"),
                "/app",
                "/user",
                List.of("/topic", "/queue"),
                "/chat.send",
                "/ws",
                "/app/chat.send",
                "/user/queue/chat.messages",
                "/user/queue/chat.errors"
        );
        StaticMessageSource messageSource = new StaticMessageSource();
        registerMessages(messageSource);
        ConfigController controller = new ConfigController(properties, messageSource);

        AppConfigResponse response = controller.getConfig("zh", null);

        assertThat(response.locale()).isEqualTo("zh");
        assertThat(response.initialBotMessage()).isEqualTo("欢迎来到 uChat 企业服务中心。请告诉我们你想咨询的业务，我们将为你安排专员跟进。");
        assertThat(response.messagePlaceholder()).isEqualTo("例如：我想了解企业贷款方案与办理条件");
        assertThat(response.sendLabel()).isEqualTo("发送消息");
        assertThat(response.tipText()).isEqualTo("提示: 本页面回复为演示资讯，正式业务请以人工服务及官方文件为准。");
        assertThat(response.thankYouText()).isEqualTo("感谢你的反馈");
        assertThat(response.thinkingText()).isEqualTo("顾问正在为您查询资料...");
        assertThat(response.serviceCenterText()).isEqualTo("服务中心");
        assertThat(response.onlineText()).isEqualTo("在线");
        assertThat(response.ratingLabels()).containsExactly("不满意", "一般", "满意", "很满意", "非常满意");
    }

    @Test
    void usesAcceptLanguageWhenLocaleQueryIsMissing() {
        UChatProperties properties = new UChatProperties(
                "uChat",
                "en",
                List.of("http://localhost:5173", "http://127.0.0.1:5173"),
                "/app",
                "/user",
                List.of("/topic", "/queue"),
                "/chat.send",
                "/ws",
                "/app/chat.send",
                "/user/queue/chat.messages",
                "/user/queue/chat.errors"
        );
        StaticMessageSource messageSource = new StaticMessageSource();
        registerMessages(messageSource);
        ConfigController controller = new ConfigController(properties, messageSource);

        AppConfigResponse response = controller.getConfig(null, "zh-CN,zh;q=0.9,en;q=0.8");

        assertThat(response.locale()).isEqualTo("zh");
        assertThat(response.sendLabel()).isEqualTo("发送消息");
    }

    @Test
    void queryLocaleTakesPriorityOverAcceptLanguage() {
        UChatProperties properties = new UChatProperties(
                "uChat",
                "en",
                List.of("http://localhost:5173", "http://127.0.0.1:5173"),
                "/app",
                "/user",
                List.of("/topic", "/queue"),
                "/chat.send",
                "/ws",
                "/app/chat.send",
                "/user/queue/chat.messages",
                "/user/queue/chat.errors"
        );
        StaticMessageSource messageSource = new StaticMessageSource();
        registerMessages(messageSource);
        ConfigController controller = new ConfigController(properties, messageSource);

        AppConfigResponse response = controller.getConfig("en", "zh-CN,zh;q=0.9");

        assertThat(response.locale()).isEqualTo("en");
        assertThat(response.sendLabel()).isEqualTo("Send message");
    }

    private void registerMessages(StaticMessageSource messageSource) {
        messageSource.addMessage("uchat.ui.initialBotMessage", Locale.SIMPLIFIED_CHINESE,
                "欢迎来到 uChat 企业服务中心。请告诉我们你想咨询的业务，我们将为你安排专员跟进。");
        messageSource.addMessage("uchat.ui.initialBotMessage", Locale.ENGLISH,
                "Welcome to the uChat Enterprise Service Center. Tell us what you would like to ask and we will arrange a specialist to follow up.");
        messageSource.addMessage("uchat.ui.messagePlaceholder", Locale.SIMPLIFIED_CHINESE,
                "例如：我想了解企业贷款方案与办理条件");
        messageSource.addMessage("uchat.ui.messagePlaceholder", Locale.ENGLISH,
                "For example: I want to learn about business loan options and eligibility");
        messageSource.addMessage("uchat.ui.sendLabel", Locale.SIMPLIFIED_CHINESE, "发送消息");
        messageSource.addMessage("uchat.ui.sendLabel", Locale.ENGLISH, "Send message");
        messageSource.addMessage("uchat.ui.tipText", Locale.SIMPLIFIED_CHINESE,
                "提示: 本页面回复为演示资讯，正式业务请以人工服务及官方文件为准。");
        messageSource.addMessage("uchat.ui.tipText", Locale.ENGLISH,
                "Note: Responses on this page are for demo purposes only. Please refer to official documents and human support for formal business matters.");
        messageSource.addMessage("uchat.ui.thankYouText", Locale.SIMPLIFIED_CHINESE, "感谢你的反馈");
        messageSource.addMessage("uchat.ui.thankYouText", Locale.ENGLISH, "Thank you for your feedback");
        messageSource.addMessage("uchat.ui.thinkingText", Locale.SIMPLIFIED_CHINESE, "顾问正在为您查询资料...");
        messageSource.addMessage("uchat.ui.thinkingText", Locale.ENGLISH, "Advisor is checking the details...");
        messageSource.addMessage("uchat.ui.serviceCenterText", Locale.SIMPLIFIED_CHINESE, "服务中心");
        messageSource.addMessage("uchat.ui.serviceCenterText", Locale.ENGLISH, "Service Center");
        messageSource.addMessage("uchat.ui.onlineText", Locale.SIMPLIFIED_CHINESE, "在线");
        messageSource.addMessage("uchat.ui.onlineText", Locale.ENGLISH, "Online");
        messageSource.addMessage("uchat.ui.ratingLabel.1", Locale.SIMPLIFIED_CHINESE, "不满意");
        messageSource.addMessage("uchat.ui.ratingLabel.2", Locale.SIMPLIFIED_CHINESE, "一般");
        messageSource.addMessage("uchat.ui.ratingLabel.3", Locale.SIMPLIFIED_CHINESE, "满意");
        messageSource.addMessage("uchat.ui.ratingLabel.4", Locale.SIMPLIFIED_CHINESE, "很满意");
        messageSource.addMessage("uchat.ui.ratingLabel.5", Locale.SIMPLIFIED_CHINESE, "非常满意");
        messageSource.addMessage("uchat.ui.ratingLabel.1", Locale.ENGLISH, "Poor");
        messageSource.addMessage("uchat.ui.ratingLabel.2", Locale.ENGLISH, "Fair");
        messageSource.addMessage("uchat.ui.ratingLabel.3", Locale.ENGLISH, "Good");
        messageSource.addMessage("uchat.ui.ratingLabel.4", Locale.ENGLISH, "Very good");
        messageSource.addMessage("uchat.ui.ratingLabel.5", Locale.ENGLISH, "Excellent");
    }
}
