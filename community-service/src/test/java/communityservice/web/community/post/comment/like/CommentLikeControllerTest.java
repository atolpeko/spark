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

package communityservice.web.community.post.comment.like;

import communityservice.service.like.commentlike.CommentLike;
import communityservice.service.like.commentlike.CommentLikeService;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Optional;

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
@AutoConfigureMockMvc
public class CommentLikeControllerTest {
    private static String newLikeJson;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CommentLikeService likeService;

    @BeforeAll
    public static void createNewLikeJson() {
        newLikeJson = "{ \"user\" : {\"login\" : \"login3\"} }";
    }

    @Test
    public void shouldReturnAllLikesOnLikesGetRequest() throws Exception {
        mvc.perform(get("/communities/1/posts/1/comments/1/likes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldReturnLikeOnLikeGetByIdRequest() throws Exception {
        mvc.perform(get("/communities/1/posts/1/comments/1/likes/4"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldReturnSavedLikeOnLikesPostRequest() throws Exception {
        int initialCount = likeService.findAllByCommentId(1).size();
        postAndExpect(newLikeJson, status().isCreated());

        int newCount = likeService.findAllByCommentId(1).size();
        assertThat(newCount, is(initialCount + 1));
    }

    private void postAndExpect(String data, ResultMatcher status) throws Exception {
        mvc.perform(post("/communities/1/posts/1/comments/1/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldDeleteLikeOnLikesDeleteRequest() throws Exception {
        deleteByIdAndExpect(2, status().isNoContent());

        Optional<CommentLike> deleted = likeService.findById(3);
        assertThat(deleted, is(Optional.empty()));
    }

    private void deleteByIdAndExpect(long id, ResultMatcher status) throws Exception {
        mvc.perform(delete("/communities/1/posts/2/comments/2/likes/" + id))
                .andDo(print())
                .andExpect(status);
    }
}
