package com.quizcontest.controller;

import com.quizcontest.repository.UserRepository;
import com.quizcontest.security.JwtAuthenticationFilter;
import com.quizcontest.controller.AuthController;
import com.quizcontest.service.UserService;
import com.quizcontest.service.interfaces.IReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ContextConfiguration(classes = ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IReportService reportService;

    @MockBean
    private AuthController authController;

    @MockBean
    private UserService authUserService;

    @MockBean
    private com.quizcontest.repository.UserRepository userRepository;

    @MockBean
    private org.modelmapper.ModelMapper modelMapper;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @MockBean
    private com.quizcontest.security.JwtService jwtService;


    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void generateUserReportReturnsFile() throws Exception {
        UUID userId = UUID.randomUUID();
        ByteArrayInputStream reportStream = new ByteArrayInputStream("report".getBytes(StandardCharsets.UTF_8));
        given(reportService.generateUserReport(userId)).willReturn(reportStream);

        mockMvc.perform(get("/api/v1/reports/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=user-report-" + userId + ".xlsx"));
    }

    @Test
    void generateQuizConsolidatedReportReturnsFile() throws Exception {
        UUID quizId = UUID.randomUUID();
        ByteArrayInputStream reportStream = new ByteArrayInputStream("report".getBytes(StandardCharsets.UTF_8));
        given(reportService.generateQuizConsolidatedReport(quizId)).willReturn(reportStream);

        mockMvc.perform(get("/api/v1/reports/quiz/{quizId}", quizId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=quiz-consolidated-report-" + quizId + ".xlsx"));
    }
}
