package com.team03.careerlog.note;

import com.team03.careerlog.ai.OpenAiSummaryClient;
import com.team03.careerlog.note.dto.CareerNoteRequest;
import com.team03.careerlog.note.dto.CareerNoteResponse;
import com.team03.careerlog.user.User;
import com.team03.careerlog.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CareerNoteService {

    private static final Logger log = LoggerFactory.getLogger(CareerNoteService.class);

    private final CareerNoteRepository careerNoteRepository;
    private final UserRepository userRepository;
    private final OpenAiSummaryClient openAiSummaryClient;

    public CareerNoteService(CareerNoteRepository careerNoteRepository,
                             UserRepository userRepository,
                             OpenAiSummaryClient openAiSummaryClient) {
        this.careerNoteRepository = careerNoteRepository;
        this.userRepository = userRepository;
        this.openAiSummaryClient = openAiSummaryClient;
    }

    @Transactional
    public CareerNoteResponse create(String loginId, CareerNoteRequest request) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        CareerNote note = new CareerNote(user, request.title(), request.content(), request.noteDate());
        careerNoteRepository.save(note);
        summarizeWithoutBreakingSave(note);
        return CareerNoteResponse.from(note);
    }

    public CareerNoteResponse get(String loginId, Long noteId) {
        return CareerNoteResponse.from(findOwnedNote(loginId, noteId));
    }

    public List<CareerNoteResponse> getAll(String loginId, LocalDate from, LocalDate to) {
        if ((from == null) != (to == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from과 to는 함께 입력해야 합니다.");
        }
        if (from != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from은 to보다 늦을 수 없습니다.");
        }

        List<CareerNote> notes = from == null
                ? careerNoteRepository.findAllByUserLoginIdOrderByNoteDateDescCreatedAtDesc(loginId)
                : careerNoteRepository.findAllByUserLoginIdAndNoteDateBetweenOrderByNoteDateDescCreatedAtDesc(
                        loginId, from, to);
        return notes.stream().map(CareerNoteResponse::from).toList();
    }

    @Transactional
    public CareerNoteResponse update(String loginId, Long noteId, CareerNoteRequest request) {
        CareerNote note = findOwnedNote(loginId, noteId);
        note.update(request.title(), request.content(), request.noteDate());
        return CareerNoteResponse.from(note);
    }

    @Transactional
    public void delete(String loginId, Long noteId) {
        careerNoteRepository.delete(findOwnedNote(loginId, noteId));
    }

    @Transactional
    public CareerNoteResponse generateAiSummary(String loginId, Long noteId) {
        CareerNote note = findOwnedNote(loginId, noteId);
        if (!openAiSummaryClient.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "OPENAI_API_KEY가 설정되지 않았습니다.");
        }

        String summary;
        try {
            summary = openAiSummaryClient.summarize(note.getTitle(), note.getContent())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_GATEWAY, "AI 요약 결과를 받지 못했습니다."));
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.warn("AI 요약 API 호출에 실패했습니다. noteId={}", noteId, exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 요약 API 호출에 실패했습니다.");
        }
        note.updateAiSummary(summary);
        return CareerNoteResponse.from(note);
    }

    private void summarizeWithoutBreakingSave(CareerNote note) {
        if (!openAiSummaryClient.isConfigured()) {
            return;
        }
        try {
            openAiSummaryClient.summarize(note.getTitle(), note.getContent())
                    .ifPresent(note::updateAiSummary);
        } catch (RuntimeException exception) {
            log.warn("노트는 저장했지만 AI 요약 생성에 실패했습니다. noteId={}", note.getId(), exception);
        }
    }

    private CareerNote findOwnedNote(String loginId, Long noteId) {
        return careerNoteRepository.findByIdAndUserLoginId(noteId, loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "취준노트를 찾을 수 없습니다."));
    }
}
