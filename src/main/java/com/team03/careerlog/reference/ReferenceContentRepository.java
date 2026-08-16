package com.team03.careerlog.reference;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReferenceContentRepository extends JpaRepository<ReferenceContent, Long> {
    List<ReferenceContent> findAllByOrderByCreatedAtDesc();
}
