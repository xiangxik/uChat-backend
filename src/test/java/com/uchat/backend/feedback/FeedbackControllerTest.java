package com.uchat.backend.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedbackControllerTest {

  @Mock
  private FeedbackStore feedbackStore;

  @Test
  void acceptsValidFeedback() {
    FeedbackController controller = new FeedbackController(feedbackStore);

    FeedbackResponse response = controller.submit(new FeedbackRequest("msg-1", 5));

    verify(feedbackStore).saveFeedback("msg-1", 5);
    assertThat(response.status()).isEqualTo("accepted");
    assertThat(response.message()).isEqualTo("Feedback received.");
    assertThat(response.timestamp()).isNotNull();
  }
}
