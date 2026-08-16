package com.team03.careerlog.note;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CareerNoteRepository extends JpaRepository<CareerNote, Long> {

    Optional<CareerNote> findByIdAndUserLoginId(Long id, String loginId);

    List<CareerNote> findAllByUserLoginIdOrderByNoteDateDescCreatedAtDesc(String loginId);

    List<CareerNote> findAllByUserLoginIdAndNoteDateBetweenOrderByNoteDateDescCreatedAtDesc(
            String loginId, LocalDate from, LocalDate to);
}
