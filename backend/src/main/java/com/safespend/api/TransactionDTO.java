package com.safespend.api;

import jakarta.validation.constraints.*;

public record TransactionDTO(
  String userId,                       // optional; ignored for non-admins
  @NotBlank @Size(max = 120) String merchant,
  @NotBlank @Size(max = 60)  String category,
  @Positive @DecimalMin("0.01") double amount
) {}
