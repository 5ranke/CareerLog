package com.team03.careerlog.reference;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteReferenceRepository extends JpaRepository<NoteReference, Long> {
    List<NoteReference> findAllByCareerNoteIdOrderByCreatedAtDesc(Long careerNoteId);
    boolean existsByCareerNoteIdAndReferenceId(Long careerNoteId, Long referenceId);
}
