package com.team03.careerlog.note;

import com.team03.careerlog.user.User;
import com.team03.careerlog.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.config.import=",
        "spring.datasource.url=jdbc:h2:mem:note-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "ai.openai.api-key="
})
@AutoConfigureMockMvc
class CareerNoteControllerIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CareerNoteRepository careerNoteRepository;

    @Autowired
    UserRepository userRepository;

    private User userOne;
    private User userTwo;

    @BeforeEach
    void setUp() {
        careerNoteRepository.deleteAll();
        userRepository.deleteAll();
        userOne = userRepository.save(new User("user_one", "password-hash"));
        userTwo = userRepository.save(new User("user_two", "password-hash"));
    }

    @Test
    void createsReadsUpdatesAndDeletesOwnNote() throws Exception {
        mockMvc.perform(post("/api/career-notes")
                        .with(user("user_one"))
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"오늘의 기록","content":"Spring Security를 공부했다.","noteDate":"2026-08-16"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("오늘의 기록"))
                .andExpect(jsonPath("$.aiSummary").doesNotExist());

        Long noteId = careerNoteRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/career-notes/{noteId}", noteId).with(user("user_one")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Spring Security를 공부했다."));

        mockMvc.perform(put("/api/career-notes/{noteId}", noteId)
                        .with(user("user_one"))
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"수정한 기록","content":"취준노트 API를 완성했다.","noteDate":"2026-08-17"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정한 기록"))
                .andExpect(jsonPath("$.noteDate").value("2026-08-17"));

        mockMvc.perform(delete("/api/career-notes/{noteId}", noteId)
                        .with(user("user_one"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/career-notes/{noteId}", noteId).with(user("user_one")))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtersNotesByInclusiveDateRange() throws Exception {
        careerNoteRepository.save(new CareerNote(userOne, "7월", "범위 밖", LocalDate.of(2026, 7, 31)));
        careerNoteRepository.save(new CareerNote(userOne, "8월 1일", "범위 안", LocalDate.of(2026, 8, 1)));
        careerNoteRepository.save(new CareerNote(userOne, "8월 16일", "범위 안", LocalDate.of(2026, 8, 16)));
        careerNoteRepository.save(new CareerNote(userTwo, "다른 사용자", "조회 금지", LocalDate.of(2026, 8, 10)));

        mockMvc.perform(get("/api/career-notes")
                        .with(user("user_one"))
                        .param("from", "2026-08-01")
                        .param("to", "2026-08-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("8월 16일"))
                .andExpect(jsonPath("$[1].title").value("8월 1일"));
    }

    @Test
    void cannotAccessAnotherUsersNote() throws Exception {
        CareerNote otherNote = careerNoteRepository.save(
                new CareerNote(userTwo, "비공개", "다른 사용자의 기록", LocalDate.of(2026, 8, 16)));

        mockMvc.perform(get("/api/career-notes/{noteId}", otherNote.getId()).with(user("user_one")))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/career-notes/{noteId}", otherNote.getId())
                        .with(user("user_one"))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticatedUserCannotUseNotesApi() throws Exception {
        mockMvc.perform(get("/api/career-notes"))
                .andExpect(status().isUnauthorized());
    }
}
