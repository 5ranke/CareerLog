package com.team03.careerlog.reference;

import com.team03.careerlog.note.CareerNote;
import com.team03.careerlog.note.CareerNoteRepository;
import com.team03.careerlog.reference.dto.ReferenceRecommendationResponse;
import com.team03.careerlog.reference.dto.ReferenceRequest;
import com.team03.careerlog.reference.dto.ReferenceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReferenceService {

    private static final Set<String> STOP_WORDS = Set.of("오늘", "공부", "기록", "대한", "관련", "그리고", "했다");

    private final ReferenceContentRepository referenceRepository;
    private final NoteReferenceRepository noteReferenceRepository;
    private final CareerNoteRepository careerNoteRepository;

    public ReferenceService(ReferenceContentRepository referenceRepository,
                            NoteReferenceRepository noteReferenceRepository,
                            CareerNoteRepository careerNoteRepository) {
        this.referenceRepository = referenceRepository;
        this.noteReferenceRepository = noteReferenceRepository;
        this.careerNoteRepository = careerNoteRepository;
    }

    @Transactional
    public ReferenceResponse create(ReferenceRequest request) {
        ReferenceContent reference = new ReferenceContent(request.title(), request.url(), request.referenceType(),
                request.thumbnailUrl(), request.source(), request.description());
        return ReferenceResponse.from(referenceRepository.save(reference));
    }

    public List<ReferenceResponse> getAll() {
        return referenceRepository.findAllByOrderByCreatedAtDesc().stream().map(ReferenceResponse::from).toList();
    }

    @Transactional
    public List<ReferenceRecommendationResponse> recommend(String loginId, Long noteId, int limit) {
        CareerNote note = careerNoteRepository.findByIdAndUserLoginId(noteId, loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "취준노트를 찾을 수 없습니다."));
        Set<String> keywords = extractKeywords(note.getTitle() + " " + note.getContent());

        List<ScoredReference> ranked = referenceRepository.findAll().stream()
                .map(reference -> new ScoredReference(reference, score(reference, keywords)))
                .sorted(Comparator.comparingInt(ScoredReference::score).reversed()
                        .thenComparing(item -> item.reference().getCreatedAt(), Comparator.reverseOrder()))
                .limit(Math.max(1, Math.min(limit, 10)))
                .toList();

        for (ScoredReference item : ranked) {
            if (!noteReferenceRepository.existsByCareerNoteIdAndReferenceId(noteId, item.reference().getId())) {
                noteReferenceRepository.save(new NoteReference(note, item.reference(), reason(item)));
            }
        }
        return ranked.stream()
                .map(item -> new ReferenceRecommendationResponse(
                        ReferenceResponse.from(item.reference()), reason(item)))
                .toList();
    }

    private String reason(ScoredReference item) {
        return item.score() > 0
                ? "노트의 주요 키워드와 관련된 자료입니다."
                : "취업 준비에 참고할 만한 최신 자료입니다.";
    }

    private Set<String> extractKeywords(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^가-힣a-z0-9+#.]+"))
                .filter(word -> word.length() >= 2 && !STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }

    private int score(ReferenceContent reference, Set<String> keywords) {
        String target = String.join(" ", reference.getTitle(),
                reference.getDescription() == null ? "" : reference.getDescription(),
                reference.getSource() == null ? "" : reference.getSource()).toLowerCase(Locale.ROOT);
        return (int) keywords.stream().filter(target::contains).count();
    }

    private record ScoredReference(ReferenceContent reference, int score) {
    }
}
