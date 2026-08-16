package com.team03.careerlog.note.dto;

import com.team03.careerlog.note.CareerNote;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CareerNoteResponse(
        Long id,
        String title,
        String content,
        LocalDate noteDate,
        String aiSummary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CareerNoteResponse from(CareerNote note) {
        return new CareerNoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getNoteDate(),
                note.getAiSummary(),
                note.getCreatedAt(),
                note.getUpdatedAt());
    }
}
