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

package communityservice.web.community.post.comment;

import communityservice.service.comment.Comment;
import communityservice.service.comment.CommentService;

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
public class CommentControllerTest {
    private static String newCommentJson;
    private static String updatedCommentJson;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CommentService commentService;

    @BeforeAll
    public static void createNewCommentJson() {
        newCommentJson = "{" +
                "\"message\": \"new-message\"," +
                "\"user\": {\"login\" : \"login1\"}" +
                "}";
    }

    @BeforeAll
    private static void createUpdatedCommentJson() {
        updatedCommentJson = "{ \"message\": \"updated-message\" }";
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnAllCommentsOnCommentsGetRequest() throws Exception {
        mvc.perform(get("/communities/1/posts/1/comments"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnCommentOnCommentGetByIdRequest() throws Exception {
        mvc.perform(get("/communities/1/posts/1/comments/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "login1")
    public void shouldReturnSavedCommentOnCommentsPostRequestWhenUserIsResourceOwner() throws Exception {
        int initialCount = commentService.findAllByPostId(1).size();
        postAndExpect(newCommentJson, status().isCreated());

        int newCount = commentService.findAllByPostId(1).size();
        assertThat(newCount, is(initialCount + 1));
    }

    private void postAndExpect(String data, ResultMatcher status) throws Exception {
        mvc.perform(post("/communities/1/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "login2")
    public void shouldDenyPostingOnCommentsPostRequestWhenUserIsNotResourceOwner() throws Exception {
        postAndExpect(newCommentJson, status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyPostingOnCommentsPostRequestWhenUserIsNotAuthenticated() throws Exception {
        postAndExpect(newCommentJson, status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "login1")
    public void shouldReturnUpdatedCommentOnCommentPatchRequestWhenUserIsResourceOwner() throws Exception {
        Comment initial = commentService.findById(2).orElseThrow();
        patchByIdAndExpect(2, updatedCommentJson, status().isOk());

        Comment updated = commentService.findById(2).orElseThrow();
        assertThat(updated, is(not(equalTo(initial))));
    }

    private void patchByIdAndExpect(long id, String data, ResultMatcher status) throws Exception {
        mvc.perform(patch("/communities/1/posts/1/comments/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "login2")
    public void shouldDenyPatchingOnCommentPatchRequestWhenUserIsNotResourceOwner() throws Exception {
        patchByIdAndExpect(2, updatedCommentJson, status().isUnauthorized());
    }


    @Test
    @WithAnonymousUser
    public void shouldDenyPatchingOnCommentPatchRequestWhenUserIsNotAuthenticated() throws Exception {
        patchByIdAndExpect(2, updatedCommentJson, status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "login2")
    public void shouldDeleteCommentOnCommentsDeleteRequestWhenUserIsResourceOwner() throws Exception {
        deleteByIdAndExpect(3, status().isNoContent());

        Optional<Comment> deleted = commentService.findById(3);
        assertThat(deleted, is(Optional.empty()));
    }

    private void deleteByIdAndExpect(long id, ResultMatcher status) throws Exception {
        mvc.perform(delete("/communities/1/posts/2/comments/" + id))
                .andDo(print())
                .andExpect(status);
    }

    @Test
    @WithMockUser(username = "login1")
    public void shouldDenyDeletionOnCommentsDeleteRequestWhenUserIsNotResourceOwner() throws Exception {
        deleteByIdAndExpect(3, status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    public void shouldDenyDeletionOnCommentsDeleteRequestWhenUserIsNotAuthenticated() throws Exception {
        deleteByIdAndExpect(3, status().isUnauthorized());
    }
}
