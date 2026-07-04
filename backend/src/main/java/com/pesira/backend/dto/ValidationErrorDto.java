package com.pesira.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValidationErrorDto {

    private final String field;
    private final String message;
}
