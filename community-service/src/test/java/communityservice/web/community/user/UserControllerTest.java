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

package communityservice.web.community.user;

import communityservice.config.IntegrationTestConfig;
import communityservice.service.user.UserService;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("category.IntegrationTest")
@SpringBootTest
@ContextConfiguration(classes = IntegrationTestConfig.class)
@AutoConfigureMockMvc
public class UserControllerTest {
    private static String userJson;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @BeforeAll
    public static void createUserJson() {
        userJson = "{ \"login\": \"new-login\" }";
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnAllUsersOnUsersGetRequest() throws Exception {
        mvc.perform(get("/communities/1/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnUserOnUserGetByLoginRequest() throws Exception {
        mvc.perform(get("/communities/1/users").param("login", "login1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "new-login")
    public void shouldSubscribeUserToCommunityOnUsersPostRequestWhenUserIsResourceOwner() throws Exception {
        int usersCount = userService.findAllByCommunityId(1L).size();
        postAndExpect(userJson, status().isCreated());

        int newCount = userService.findAllByCommunityId(1L).size();
        assertThat(newCount, is(usersCount + 1));
    }

    private void postAndExpect(String data, ResultMatcher status) throws Exception {
        mvc.perform(post("/communities/1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithAnonymousUser
    public void shouldDenySubscriptionOnUsersPostRequestWhenUserIsAnonymous() throws Exception {
        postAndExpect(userJson, status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "login2")
    public void shouldUnsubscribeUserFromCommunityOnUserDeleteRequestWhenUserIsResourceOwner() throws Exception {
        int userCount = userService.findAllByCommunityId(1L).size();
        deleteByLoginAndExpect("login2", status().isNoContent());

        int newCount = userService.findAllByCommunityId(1L).size();
        assertThat(newCount, is(userCount - 1));
    }

    private void deleteByLoginAndExpect(String login, ResultMatcher status) throws Exception {
        mvc.perform(delete("/communities/1/users").param("login", login))
                .andDo(print())
                .andExpect(status);
    }

    @Test
    @WithMockUser(authorities = "USER", username = "login1")
    public void shouldDenyUnsubscriptionOnUserDeleteRequestWhenUserIsNotResourceOwner() throws Exception {
        deleteByLoginAndExpect("login2", status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyUnsubscriptionOnUserDeleteRequestWhenUserIsAnonymous() throws Exception {
        deleteByLoginAndExpect("login2", status().isUnauthorized());
    }
}
