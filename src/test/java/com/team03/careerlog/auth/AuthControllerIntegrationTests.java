package com.team03.careerlog.auth;

import com.team03.careerlog.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.config.import=",
        "spring.datasource.url=jdbc:h2:mem:auth-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "ai.openai.api-key="
})
@AutoConfigureMockMvc
class AuthControllerIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void signupHashesPassword() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"loginId":"career_user","password":"password123!"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loginId").value("career_user"));

        var savedUser = userRepository.findByLoginId("career_user").orElseThrow();
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("password123!");
        assertThat(passwordEncoder.matches("password123!", savedUser.getPasswordHash())).isTrue();
    }

    @Test
    void loginCreatesSessionAndMeReturnsCurrentUser() throws Exception {
        signup("career_user", "password123!");

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"loginId":"career_user","password":"password123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("career_user"))
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("career_user"));
    }

    @Test
    void unauthenticatedUserCannotReadMe() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateLoginIdReturnsConflict() throws Exception {
        signup("career_user", "password123!");

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"loginId":"career_user","password":"password123!"}
                                """))
                .andExpect(status().isConflict());
    }

    private void signup(String loginId, String password) throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"loginId":"%s","password":"%s"}
                                """.formatted(loginId, password)))
                .andExpect(status().isCreated());
    }
}
