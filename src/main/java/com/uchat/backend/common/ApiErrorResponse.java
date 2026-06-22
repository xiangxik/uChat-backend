package com.uchat.backend.common;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        ApiErrorCode code,
        String message,
        Instant timestamp,
        String path,
        List<String> details
) {
}
