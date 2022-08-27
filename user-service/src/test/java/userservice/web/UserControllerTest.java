/*
 * Copyright 2002-2021 Alexander Tolpeko.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package userservice.web;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import userservice.service.User;
import userservice.service.UserService;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("category.IntegrationTest")
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    private static String newUserJson;
    private static String updatedUser1Json;
    private static String updatedUser2Json;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @BeforeAll
    public static void createNewUserJson() {
        newUserJson = "{" +
                "\"login\": \"new-login\"," +
                "\"email\": \"email@gmail.com\"," +
                "\"password\": \"123456789\"," +
                "\"login\": \"login\"," +
                "\"name\": \"Alexander\"," +
                "\"birthday\": \"1995-01-22\"," +
                "\"phone\": \"32343434\"" +
                "}";
    }

    @BeforeAll
    private static void createUpdatedUserJsons() {
        updatedUser1Json = "{" +
                "\"login\": \"new-login2\"," +
                "\"password\": \"987654321\"," +
                "\"login\": \"login1\"," +
                "\"name\": \"Mark\"," +
                "\"birthday\": \"2003-07-24\"," +
                "\"phone\": \"87637437984\"" +
                "}";

        updatedUser2Json = "{" +
                "\"login\": \"new-login3\"," +
                "\"password\": \"fkm4454\"," +
                "\"login\": \"login2\"," +
                "\"name\": \"Mark\"," +
                "\"birthday\": \"2003-07-24\"," +
                "\"phone\": \"87637437984\"" +
                "}";
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnAllUsersOnUsersGetRequest() throws Exception {
        mvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnUserOnUserGetByLoginRequest() throws Exception {
        mvc.perform(get("/users").param("login", "log"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnUserOnUserGetByEmailRequest() throws Exception {
        mvc.perform(get("/users").param("email", "e@gmail.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldReturnSavedDoctorOnDoctorsPostRequest() throws Exception {
        int initialCount = userService.findAll().size();
        postAndExpect(newUserJson, status().isCreated());

        int newCount = userService.findAll().size();
        assertThat(newCount, is(initialCount + 1));
    }

    private void postAndExpect(String data, ResultMatcher status) throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void shouldReturnUpdatedUserOnUsersPatchRequestWhenUserIsAdmin() throws Exception {
        User initial = userService.findByLogin("log2").orElseThrow();
        patchByLoginAndExpect("log2", updatedUser1Json, status().isOk());

        User updated = userService.findByLogin("log2").orElseThrow();
        assertThat(updated, is(not(equalTo(initial))));
    }

    private void patchByLoginAndExpect(String login, String data, ResultMatcher status) throws Exception {
        mvc.perform(patch("/users/" + login)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "log3", authorities = "USER")
    public void shouldReturnUpdatedUserOnUsersPatchRequestWhenUserIsResourceOwner() throws Exception {
        User initial = userService.findByLogin("log3").orElseThrow();
        patchByLoginAndExpect("log3", updatedUser2Json, status().isOk());

        User updated = userService.findByLogin("log3").orElseThrow();
        assertThat(updated, is(not(equalTo(initial))));
    }

    @Test
    @WithMockUser(username = "log4", authorities = "USER")
    public void shouldDenyUserPatchingWhenUserIsNotResourceOwner() throws Exception {
        patchByLoginAndExpect("log2", updatedUser1Json, status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyUserPatchingWhenUserIsNotAuthenticated() throws Exception {
        patchByLoginAndExpect("log2", updatedUser1Json, status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void shouldBlockUserOnUserBlockRequestWhenUserIsAdmin() throws Exception {
        User initial = userService.findByLogin("log6").orElseThrow();
        blockByLoginAndExpect("log6", status().isOk());

        User blocked = userService.findByLogin("log6").orElseThrow();
        assertThat(initial.getBlocked(), is(not(equalTo(blocked.getBlocked()))));
    }

    private void blockByLoginAndExpect(String login, ResultMatcher status) throws Exception {
        mvc.perform(patch("/users/" + login).param("isBlocked", "true"))
                .andDo(print())
                .andExpect(status);
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void shouldDenyUserBlockingWhenUserIsNotAdmin() throws Exception {
        blockByLoginAndExpect("log6", status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyUserBlockingWhenUserIsNotAuthenticated() throws Exception {
        blockByLoginAndExpect("log6", status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void shouldDeleteUserOnUserDeleteRequestWhenUserIsAdmin() throws Exception {
        deleteByLoginAndExpect("log4", status().isNoContent());

        Optional<User> deleted = userService.findByLogin("log4");
        assertThat(deleted, is(Optional.empty()));
    }

    private void deleteByLoginAndExpect(String login, ResultMatcher status) throws Exception {
        mvc.perform(delete("/users/" + login))
                .andDo(print())
                .andExpect(status);
    }

    @Test
    @WithMockUser(username = "log5", authorities = "USER")
    public void shouldDeleteUserOnUserDeleteRequestWhenUserIsResourceOwner() throws Exception {
        deleteByLoginAndExpect("log5", status().isNoContent());

        Optional<User> deleted = userService.findByLogin("log5");
        assertThat(deleted, is(Optional.empty()));
    }

    @Test
    @WithMockUser(username = "log6", authorities = "USER")
    public void shouldDenyUserDeletionWhenUserIsNotResourceOwner() throws Exception {
        deleteByLoginAndExpect("log5", status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyUserDeletionWhenUserIsNotAuthenticated() throws Exception {
        deleteByLoginAndExpect("log5", status().isUnauthorized());
    }
}
