package com.safespend.api;

import jakarta.validation.constraints.*;

public record TransactionDTO(
  @NotBlank String userId,
  @NotBlank String merchant,
  @NotBlank String category,
  @Positive @DecimalMin(value = "0.01") double amount
) {}
