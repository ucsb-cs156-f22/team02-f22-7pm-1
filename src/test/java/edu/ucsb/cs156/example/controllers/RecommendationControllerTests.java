package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Recommendation;
import edu.ucsb.cs156.example.repositories.RecommendationRepository;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecommendationController.class)
@Import(TestConfig.class)
public class RecommendationControllerTests extends ControllerTestCase {
    @MockBean
    RecommendationRepository recommendationRepository;

    @MockBean
    UserRepository userRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/Recommendation/all"))
                .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/Recommendation/all"))
                .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/Recommendation/post"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/Recommendation/post"))
                .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_recommendations() throws Exception {

        LocalDateTime ldt1 = LocalDateTime.parse("2022-10-31T00:00:00");

        Recommendation rec1 = Recommendation.builder()
                .requesterEmail("requesterEmail1@gmail.com")
                .professorEmail("professorEmail1@gmail.com")
                .explanation("explanation1")
                .dateRequested(ldt1)
                .dateNeeded(ldt1)
                .done(false)
                .build();

        LocalDateTime ldt2 = LocalDateTime.parse("2022-11-01T00:00:00");

        Recommendation rec2 = Recommendation.builder()
                .requesterEmail("requesterEmail2@gmail.com")
                .professorEmail("professorEmail2@gmail.com")
                .explanation("explanation2")
                .dateRequested(ldt2)
                .dateNeeded(ldt2)
                .done(false)
                .build();

        ArrayList<Recommendation> expectedRecommendations = new ArrayList<>();
        expectedRecommendations.addAll(Arrays.asList(rec1, rec2));

        when(recommendationRepository.findAll()).thenReturn(expectedRecommendations);

        // act
        MvcResult response = mockMvc.perform(get("/api/Recommendation/all"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(recommendationRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedRecommendations);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_recommendation() throws Exception {
        // arrange

        LocalDateTime ldt = LocalDateTime.parse("2022-11-01T00:00:00");

        Recommendation rec = Recommendation.builder()
                .requesterEmail("requesterEmail@gmail.com")
                .professorEmail("professorEmail@gmail.com")
                .explanation("explanation")
                .dateRequested(ldt)
                .dateNeeded(ldt)
                .done(false)
                .build();

        when(recommendationRepository.save(eq(rec))).thenReturn(rec);

        // act
        MvcResult response = mockMvc.perform(
                post("/api/Recommendation/post?requesterEmail=requesterEmail@gmail.com&professorEmail=professorEmail@gmail.com&explanation=explanation&dateRequested=2022-11-01T00:00:00&dateNeeded=2022-11-01T00:00:00&done=false")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(recommendationRepository, times(1)).save(rec);
        String expectedJson = mapper.writeValueAsString(rec);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
}
