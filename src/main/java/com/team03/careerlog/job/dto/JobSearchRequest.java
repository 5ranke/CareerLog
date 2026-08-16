package com.team03.careerlog.job.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record JobSearchRequest(@NotNull LocalDate from, @NotNull LocalDate to) {
}
