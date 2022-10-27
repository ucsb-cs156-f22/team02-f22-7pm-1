package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

        @MockBean
        UCSBOrganizationRepository ucsbOrganizationRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsborganization/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsborganization?orgCode=test"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsborganization/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBOrganization organization = UCSBOrganization.builder()
                                .orgCode("TST1")
                                .orgTranslationShort("TST1")
                                .orgTranslation("Test1")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationRepository.findById(eq("TST1"))).thenReturn(Optional.of(organization));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=TST1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById(eq("TST1"));
                String expectedJson = mapper.writeValueAsString(organization);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbOrganizationRepository.findById("TST2")).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=TST2"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById("TST2");
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganization with id TST2 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

                // arrange

                UCSBOrganization test1 = UCSBOrganization.builder()
                                .orgCode("TST1")
                                .orgTranslationShort("TST1")
                                .orgTranslation("Test1")
                                .inactive(true)
                                .build();

                UCSBOrganization test2 = UCSBOrganization.builder()
                                .orgCode("TST1")
                                .orgTranslationShort("TST1")
                                .orgTranslation("Test1")
                                .inactive(true)
                                .build();

                ArrayList<UCSBOrganization> expectedOrganization = new ArrayList<>();
                expectedOrganization.addAll(Arrays.asList(test1, test2));

                when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganization);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrganization);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_organization() throws Exception {
                // arrange

                UCSBOrganization testOrg = UCSBOrganization.builder()
                            .orgCode("TEST")
                            .orgTranslationShort("TEST")
                            .orgTranslation("TESTORG")
                            .inactive(true)
                            .build();

                when(ucsbOrganizationRepository.save(eq(testOrg))).thenReturn(testOrg);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsborganization/post?orgCode=TEST&orgTranslationShort=TEST&orgTranslation=TESTORG&inactive=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).save(testOrg);
                String expectedJson = mapper.writeValueAsString(testOrg);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_an_organization() throws Exception {
                // arrange

                UCSBOrganization organization = UCSBOrganization.builder()
                            .orgCode("NEW")
                            .orgTranslationShort("NEW")
                            .orgTranslation("NEWORG")
                            .inactive(true)
                            .build();

                when(ucsbOrganizationRepository.findById("NEW")).thenReturn(Optional.of(organization));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgCode=NEW")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("NEW");
                verify(ucsbOrganizationRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id NEW deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrganizationRepository.findById(eq("nope"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgCode=nope")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById(eq("nope"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id nope not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_organization() throws Exception {
                // arrange

                UCSBOrganization testOrg1 = UCSBOrganization.builder()
                                .orgCode("t1")
                                .orgTranslationShort("TST1")
                                .orgTranslation("TEST1")
                                .inactive(true)
                                .build();

                UCSBOrganization testOrg1Edited = UCSBOrganization.builder()
                                .orgCode("t1")
                                .orgTranslationShort("TST2")
                                .orgTranslation("TEST2")
                                .inactive(false)
                                .build();

                String requestBody = mapper.writeValueAsString(testOrg1Edited);

                when(ucsbOrganizationRepository.findById(eq("t1"))).thenReturn(Optional.of(testOrg1));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgCode=t1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("t1");
                verify(ucsbOrganizationRepository, times(1)).save(testOrg1Edited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
                // arrange

                UCSBOrganization editedOrganization = UCSBOrganization.builder()
                                .orgCode("edt")
                                .orgTranslationShort("EDT")
                                .orgTranslation("EDIT")
                                .inactive(true)
                                .build();

                String requestBody = mapper.writeValueAsString(editedOrganization);

                when(ucsbOrganizationRepository.findById("edt")).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgCode=edt")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("edt");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id edt not found", json.get("message"));

        }
}