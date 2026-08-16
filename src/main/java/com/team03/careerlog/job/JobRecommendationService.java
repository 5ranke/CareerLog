package com.team03.careerlog.job;

import com.team03.careerlog.job.dto.JobRecommendationResponse;
import com.team03.careerlog.job.dto.JobSearchRequest;
import com.team03.careerlog.note.CareerNote;
import com.team03.careerlog.note.CareerNoteRepository;
import com.team03.careerlog.user.User;
import com.team03.careerlog.user.UserRepository;
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
public class JobRecommendationService {

    private final JobPostingRepository postingRepository;
    private final JobRecommendationRepository recommendationRepository;
    private final CareerNoteRepository noteRepository;
    private final UserRepository userRepository;

    public JobRecommendationService(JobPostingRepository postingRepository,
                                    JobRecommendationRepository recommendationRepository,
                                    CareerNoteRepository noteRepository,
                                    UserRepository userRepository) {
        this.postingRepository = postingRepository;
        this.recommendationRepository = recommendationRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<JobRecommendationResponse> search(String loginId, JobSearchRequest request) {
        if (request.from().isAfter(request.to())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 기간을 확인해주세요.");
        }
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        List<CareerNote> notes = noteRepository
                .findAllByUserLoginIdAndNoteDateBetweenOrderByNoteDateDescCreatedAtDesc(
                        loginId, request.from(), request.to());
        if (notes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 기간에 취준노트가 없습니다.");
        }

        String noteText = notes.stream()
                .map(note -> (note.getTitle() == null ? "" : note.getTitle()) + " " + note.getContent())
                .collect(Collectors.joining(" "));
        Set<String> keywords = keywords(noteText);

        return postingRepository.findAll().stream()
                .map(posting -> new ScoredPosting(posting, score(posting, keywords)))
                .sorted(Comparator.comparingInt(ScoredPosting::score).reversed()
                        .thenComparing(item -> item.posting().getDeadline()))
                .limit(5)
                .map(item -> saveAndConvert(user, item))
                .toList();
    }

    private JobRecommendationResponse saveAndConvert(User user, ScoredPosting item) {
        String reason = item.score() > 0
                ? "선택한 기간의 취준노트와 관련된 역량이 공고에 포함되어 있어요."
                : "관심 직무를 넓혀볼 수 있는 데모 공고예요.";
        JobRecommendation recommendation = recommendationRepository
                .findByUserIdAndJobPostingId(user.getId(), item.posting().getId())
                .orElseGet(() -> recommendationRepository.save(
                        new JobRecommendation(user, item.posting(), reason)));
        return JobRecommendationResponse.of(item.posting(), recommendation.getRecommendationReason(),
                recommendation.getStatus());
    }

    private Set<String> keywords(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^가-힣a-z0-9+#.]+"))
                .filter(word -> word.length() >= 2)
                .collect(Collectors.toSet());
    }

    private int score(JobPosting posting, Set<String> keywords) {
        String target = (posting.getTitle() + " " + posting.getDescription()).toLowerCase(Locale.ROOT);
        return (int) keywords.stream().filter(target::contains).count();
    }

    private record ScoredPosting(JobPosting posting, int score) {
    }
}
