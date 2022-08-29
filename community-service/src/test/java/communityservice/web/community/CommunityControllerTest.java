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

package communityservice.web.community;

import communityservice.config.IntegrationTestConfig;
import communityservice.service.community.Community;
import communityservice.service.community.CommunityService;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

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
@ContextConfiguration(classes = IntegrationTestConfig.class)
@AutoConfigureMockMvc
public class CommunityControllerTest {
    private static String newCommunityJson;
    private static String updatedCommunityJson;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CommunityService communityService;

    @BeforeAll
    public static void createNewCommunityJson() {
        newCommunityJson = "{" +
                "\"name\": \"new-name\"," +
                "\"description\": \"new-description\"," +
                "\"adminLogin\" : \"login1\"" +
                "}";
    }

    @BeforeAll
    private static void createUpdatedCommunityJson() {
        updatedCommunityJson = "{" +
                "\"name\": \"updated-name\"," +
                "\"description\": \"updated-description\"" +
                "}";
    }

    @Test
    public void shouldReturnAllCommunitiesOnCommunitiesGetRequest() throws Exception {
        mvc.perform(get("/communities"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldReturnCommunityOnCommunityGetByIdRequest() throws Exception {
        mvc.perform(get("/communities/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldReturnCommunityOnCommunityGetByNameRequest() throws Exception {
        mvc.perform(get("/communities").param("name", "name1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldReturnSavedCommunityOnCommunitiesPostRequest() throws Exception {
        int initialCount = communityService.findAll().size();
        postAndExpect(newCommunityJson, status().isCreated());

        int newCount = communityService.findAll().size();
        assertThat(newCount, is(initialCount + 1));
    }

    private void postAndExpect(String data, ResultMatcher status) throws Exception {
        mvc.perform(post("/communities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldReturnUpdatedCommunityOnCommunitiesPatchRequest() throws Exception {
        Community initial = communityService.findById(2).orElseThrow();
        patchByIdAndExpect(2, updatedCommunityJson, status().isOk());

        Community updated = communityService.findById(2).orElseThrow();
        assertThat(updated, is(not(equalTo(initial))));
    }

    private void patchByIdAndExpect(long id, String data, ResultMatcher status) throws Exception {
        mvc.perform(patch("/communities/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldDeleteCommunityOnCommunitiesDeleteRequest() throws Exception {
        deleteByIdAndExpect(3, status().isNoContent());

        Optional<Community> deleted = communityService.findById(3);
        assertThat(deleted, is(Optional.empty()));
    }

    private void deleteByIdAndExpect(long id, ResultMatcher status) throws Exception {
        mvc.perform(delete("/communities/" + id))
                .andDo(print())
                .andExpect(status);
    }
}
