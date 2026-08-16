package com.team03.careerlog.note.dto;

import com.team03.careerlog.note.CareerNote;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CareerNoteResponse(
        Long id,
        String whatDidYouDo,
        String memorablePoint,
        String inputReason,
        LocalDate noteDate,
        String experience,
        List<String> activities,
        String reaction,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CareerNoteResponse from(CareerNote note) {
        return new CareerNoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getInputReason(),
                note.getNoteDate(),
                note.getExperience(),
                note.getActivities(),
                note.getReaction(),
                note.getReason(),
                note.getCreatedAt(),
                note.getUpdatedAt());
    }
}
