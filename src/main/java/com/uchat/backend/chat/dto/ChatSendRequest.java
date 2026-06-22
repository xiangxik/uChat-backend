package com.uchat.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatSendRequest(
        @NotBlank(message = "conversationId must not be blank") String conversationId,
        @NotBlank(message = "clientMessageId must not be blank") String clientMessageId,
        @Size(max = 4000, message = "content must not exceed 4000 characters") String content,
        @Pattern(regexp = "zh|en", message = "locale must be zh or en") String locale
) {
}
