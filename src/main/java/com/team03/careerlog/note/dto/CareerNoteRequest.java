package com.team03.careerlog.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CareerNoteRequest(
        @Size(max = 200) String title,
        @NotBlank @Size(max = 20_000) String content,
        @NotNull LocalDate noteDate
) {
}
