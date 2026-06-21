package com.uchat.backend.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FeedbackControllerTest {

  @Test
  void acceptsValidFeedback() {
    FeedbackController controller = new FeedbackController();

    FeedbackResponse response = controller.submit(new FeedbackRequest("msg-1", 5));

    assertThat(response.status()).isEqualTo("accepted");
    assertThat(response.message()).isEqualTo("Feedback received.");
    assertThat(response.timestamp()).isNotNull();
  }
}
