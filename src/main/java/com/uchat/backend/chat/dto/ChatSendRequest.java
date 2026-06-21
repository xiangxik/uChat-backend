package com.uchat.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatSendRequest(
        @NotBlank String conversationId,
        @NotBlank String clientMessageId,
        @Size(max = 4000) String content,
        @Pattern(regexp = "zh|en", message = "locale must be zh or en") String locale
) {
}
