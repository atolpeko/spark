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
                "\"password\": \"987654321\"," +
                "\"login\": \"login1\"," +
                "\"name\": \"Mark\"," +
                "\"birthday\": \"2003-07-24\"," +
                "\"phone\": \"87637437984\"" +
                "}";

        updatedUser2Json = "{" +
                "\"password\": \"fkm4454\"," +
                "\"login\": \"login2\"," +
                "\"name\": \"Mark\"," +
                "\"birthday\": \"2003-07-24\"," +
                "\"phone\": \"87637437984\"" +
                "}";
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void shouldReturnAllUsersOnUsersGetRequestWhenUserIsAdmin() throws Exception {
        getAllAndExpect(status().isOk());
    }

    private void getAllAndExpect(ResultMatcher status) throws Exception {
        mvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void shouldDenyAccessToAllUsersWhenUserIsNotAdmin() throws Exception {
        getAllAndExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyAccessToAllUsersWhenUserIsNotAuthenticated() throws Exception {
        getAllAndExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void shouldReturnUserOnUsersGetByIdRequestWhenUserIsAdmin() throws Exception {
        getByIdAndExpect(status().isOk());
    }

    private void getByIdAndExpect(ResultMatcher status) throws Exception {
        mvc.perform(get("/users/1"))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "e@gmail.com", authorities = "USER")
    public void shouldReturnUserOnUsersGetByIdRequestWhenUserIsResourceOwner() throws Exception {
        getByIdAndExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "e2@gmail.com", authorities = "USER")
    public void shouldDenyAccessToUserByIdWhenUserIsNotResourceOwner() throws Exception {
        getByIdAndExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyAccessToUserByIdWhenUserIsNotAuthenticated() throws Exception {
        getByIdAndExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void shouldReturnUserOnUserGetByEmailRequestWhenUserIsAdmin() throws Exception {
        getByEmailAndExpect(status().isOk());
    }

    private void getByEmailAndExpect(ResultMatcher status) throws Exception {
        mvc.perform(get("/users").param("email", "e@gmail.com"))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "e@gmail.com", authorities = "USER")
    public void shouldReturnUserOnUsersGetByEmailRequestWhenUserIsResourceOwner() throws Exception {
        getByEmailAndExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "e2@gmail.com", authorities = "USER")
    public void shouldDenyAccessToUserByEmailWhenUserIsNotResourceOwner() throws Exception {
        getByEmailAndExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyAccessToUserByEmailWhenUserIsNotAuthenticated() throws Exception {
        getByEmailAndExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void shouldReturnUserOnUserGetByLoginRequestWhenUserIsAdmin() throws Exception {
        getByLoginAndExpect(status().isOk());
    }

    private void getByLoginAndExpect(ResultMatcher status) throws Exception {
        mvc.perform(get("/users").param("login", "log"))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "e@gmail.com", authorities = "USER")
    public void shouldReturnUserOnUsersGetByLoginRequestWhenUserIsResourceOwner() throws Exception {
        getByLoginAndExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "e2@gmail.com", authorities = "USER")
    public void shouldDenyAccessToUserByLoginWhenUserIsNotResourceOwner() throws Exception {
        getByLoginAndExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyAccessToUserByLoginWhenUserIsNotAuthenticated() throws Exception {
        getByLoginAndExpect(status().isUnauthorized());
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
        User initial = userService.findById(2).orElseThrow();
        patchByIdAndExpect(2, updatedUser1Json, status().isOk());

        User updated = userService.findById(2).orElseThrow();
        assertThat(updated, is(not(equalTo(initial))));
    }

    private void patchByIdAndExpect(long id, String data, ResultMatcher status) throws Exception {
        mvc.perform(patch("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "e3@gmail.com", authorities = "USER")
    public void shouldReturnUpdatedUserOnUsersPatchRequestWhenUserIsResourceOwner() throws Exception {
        User initial = userService.findById(3).orElseThrow();
        patchByIdAndExpect(3, updatedUser2Json, status().isOk());

        User updated = userService.findById(3).orElseThrow();
        assertThat(updated, is(not(equalTo(initial))));
    }

    @Test
    @WithMockUser(username = "e3@gmail.com", authorities = "USER")
    public void shouldDenyUserPatchingWhenUserIsNotResourceOwner() throws Exception {
        patchByIdAndExpect(2, updatedUser1Json, status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyUserPatchingWhenUserIsNotAuthenticated() throws Exception {
        patchByIdAndExpect(2, updatedUser1Json, status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void shouldBlockUserOnUserBlockRequestWhenUserIsAdmin() throws Exception {
        User initial = userService.findById(6).orElseThrow();
        blockByIdAndExpect(6, status().isOk());

        User blocked = userService.findById(6).orElseThrow();
        assertThat(initial.getBlocked(), is(not(equalTo(blocked.getBlocked()))));
    }

    private void blockByIdAndExpect(long id, ResultMatcher status) throws Exception {
        mvc.perform(patch("/users/" + id).param("isBlocked", "true"))
                .andDo(print())
                .andExpect(status);
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void shouldDenyUserBlockingWhenUserIsNotAdmin() throws Exception {
        blockByIdAndExpect(6, status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyUserBlockingWhenUserIsNotAuthenticated() throws Exception {
        blockByIdAndExpect(6, status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void shouldDeleteUserOnUserDeleteRequestWhenUserIsAdmin() throws Exception {
        deleteByIdAndExpect(4, status().isNoContent());

        Optional<User> deleted = userService.findById(4);
        assertThat(deleted, is(Optional.empty()));
    }

    private void deleteByIdAndExpect(long id, ResultMatcher status) throws Exception {
        mvc.perform(delete("/users/" + id))
                .andDo(print())
                .andExpect(status);
    }

    @Test
    @WithMockUser(username = "e5@gmail.com", authorities = "USER")
    public void shouldDeleteUserOnUserDeleteRequestWhenUserIsResourceOwner() throws Exception {
        deleteByIdAndExpect(5, status().isNoContent());

        Optional<User> deleted = userService.findById(5);
        assertThat(deleted, is(Optional.empty()));
    }

    @Test
    @WithMockUser(username = "e3@gmail.com", authorities = "USER")
    public void shouldDenyUserDeletionWhenUserIsNotResourceOwner() throws Exception {
        deleteByIdAndExpect(5, status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyUserDeletionWhenUserIsNotAuthenticated() throws Exception {
        deleteByIdAndExpect(5, status().isUnauthorized());
    }
}
