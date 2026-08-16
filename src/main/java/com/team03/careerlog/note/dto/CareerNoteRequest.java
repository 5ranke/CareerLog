package com.team03.careerlog.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CareerNoteRequest(
        @NotBlank @Size(max = 20_000) String whatDidYouDo,
        @NotBlank @Size(max = 20_000) String memorablePoint,
        @Size(max = 20_000) String reason,
        @NotNull LocalDate noteDate
) {
}
