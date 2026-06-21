package com.uchat.backend.chat.service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MockChatReplyService implements ChatReplyService {

    private static final Map<String, String> ZH_REPLIES = Map.ofEntries(
            Map.entry("贷款", "我们可以为你介绍个人贷款、按揭及中小企融资服务。请告诉我你的预算范围，我会先给出建议方向。"),
            Map.entry("信用卡", "如需比较信用卡礼遇，我可以按你的消费场景推荐适合的卡种。"),
            Map.entry("投资", "我可以先提供一般资讯，包括风险等级与产品类别；正式建议请以顾问会面为准。"),
            Map.entry("开户", "你可以通过线上预约分行办理开户。如需，我可以列出所需文件清单。")
    );

    private static final Map<String, String> EN_REPLIES = Map.ofEntries(
            Map.entry("loan", "We can walk you through personal loans, mortgages, and SME financing. Share your budget range and I will suggest a direction first."),
            Map.entry("credit", "If you want to compare card benefits, I can recommend suitable cards based on your spending habits."),
            Map.entry("investment", "I can provide general information, including risk levels and product categories; formal advice should be confirmed with a consultant."),
            Map.entry("account", "You can book an appointment to open an account at a branch. If needed, I can list the required documents.")
    );

    @Override
    public String generateReply(String content, String locale) {
        String normalizedLocale = normalizeLocale(locale);
        String cleaned = content == null ? "" : content.trim();
        if (cleaned.isEmpty()) {
            return normalizedLocale.equals("en")
                    ? "Please type your question first. I will organize the relevant information for you right away."
                    : "请先输入你的问题，我会即时为你整理相关资讯。";
        }

        Map<String, String> replies = normalizedLocale.equals("en") ? EN_REPLIES : ZH_REPLIES;
        String normalizedContent = cleaned.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : new LinkedHashMap<>(replies).entrySet()) {
            if (normalizedContent.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                return entry.getValue();
            }
        }

        return normalizedLocale.equals("en")
                ? "I have received your query. I suggest clarifying the service type, budget, and timeline first, and I can guide you step by step from there."
                : "已收到你的查询。我建议先从服务类别、预算与时程三个方向厘清需求，我可以继续一步步协助你。";
    }

    private String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return "en";
        }
        return switch (locale.trim().toLowerCase(Locale.ROOT)) {
            case "zh" -> "zh";
            case "en" -> "en";
            default -> throw new IllegalArgumentException("Unsupported locale: " + locale);
        };
    }
}
