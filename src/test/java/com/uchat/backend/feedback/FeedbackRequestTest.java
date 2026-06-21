package com.uchat.backend.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FeedbackRequestTest {

  private final Validator validator = createValidator();

  @Test
  void acceptsRatingInAllowedRange() {
    FeedbackRequest request = new FeedbackRequest("msg-1", 5);

    Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

    assertThat(violations).isEmpty();
  }

  @Test
  void rejectsBlankMessageIdAndRatingOutsideAllowedRange() {
    FeedbackRequest request = new FeedbackRequest(" ", 0);

    Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .containsExactlyInAnyOrder("messageId", "rating");
  }

  @Test
  void rejectsRatingAboveAllowedRange() {
    FeedbackRequest request = new FeedbackRequest("msg-1", 6);

    Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

    assertThat(violations)
        .singleElement()
        .satisfies(violation -> assertThat(violation.getPropertyPath().toString()).isEqualTo("rating"));
  }

  private static Validator createValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    return factory.getValidator();
  }
}
