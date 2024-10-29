package com.dws.challenge.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotEmpty(message = "From Account ID must not be empty")
    @NotNull(message = "From Account ID must not be null")
    private String accountFromId;
    @NotEmpty(message = "To Account ID must not be empty")
    @NotNull(message = "To Account ID must not be null")
    private String accountToId;
    @Positive(message = "The amount to transfer should always be a positive number")
    private BigDecimal amount;

}