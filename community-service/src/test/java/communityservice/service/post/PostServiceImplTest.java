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

package communityservice.service.post;

import communityservice.data.CommunityRepository;
import communityservice.data.PostRepository;
import communityservice.data.UserRepository;
import communityservice.service.community.Community;
import communityservice.service.exception.IllegalModificationException;
import communityservice.service.user.User;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import javax.validation.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("category.UnitTest")
public class PostServiceImplTest {
    private static PostRepository postRepository;
    private static CommunityRepository communityRepository;
    private static UserRepository userRepository;
    private static Validator validator;
    private static CircuitBreaker circuitBreaker;

    private static Post post;
    private static Post updatedPost;

    private PostServiceImpl postService;

    @BeforeAll
    public static void setUpMocks() {
        postRepository = mock(PostRepository.class);
        validator = mock(Validator.class);

        communityRepository = mock(CommunityRepository.class);
        Community community = Community.builder()
                .withId(1)
                .build();
        when(communityRepository.findById(1L)).thenReturn(Optional.of(community));

        userRepository = mock(UserRepository.class);
        User user = User.builder()
                .withLogin("login")
                .build();
        when(userRepository.findById("login")).thenReturn(Optional.of(user));

        circuitBreaker = mock(CircuitBreaker.class);
        when(circuitBreaker.decorateSupplier(any())).then(returnsFirstArg());
        when(circuitBreaker.decorateRunnable(any())).then(returnsFirstArg());
    }

    @BeforeAll
    public static void createPost() {
        User user = User.builder()
                .withLogin("login")
                .build();
        Community community = Community.builder()
                .withId(1)
                .withName("comm")
                .build();
        post = Post.builder()
                .withId(1L)
                .withMessage("message")
                .withCommunity(community)
                .withUser(user)
                .build();
    }

    @BeforeAll
    public static void createUpdatedPost() {
        User user = User.builder()
                .withLogin("login")
                .build();
        Community community = Community.builder()
                .withId(1)
                .withName("comm")
                .build();
        updatedPost = Post.builder()
                .withId(1L)
                .withMessage("message2")
                .withCommunity(community)
                .withUser(user)
                .build();
    }

    @BeforeEach
    public void beforeEach() {
        Mockito.reset(postRepository, validator);
        postService = new PostServiceImpl(communityRepository, userRepository,
                postRepository, validator, circuitBreaker);
    }

    @Test
    public void shouldReturnPostByIdWhenContainsIt() {
        when(postService.findById(post.getId())).thenReturn(Optional.of(post));

        Post saved = postService.findById(post.getId()).orElseThrow();
        assertThat(saved, is(equalTo(post)));
    }

    @Test
    public void shouldReturnListOfPostsWhenContainsMultiplePosts() {
        List<Post> posts = new ArrayList<>(List.of(post, post, post));
        when(postRepository.findAllByCommunityId(1)).thenReturn(posts);

        List<Post> saved = postService.findAllByCommunityId(1);
        assertThat(saved, is(equalTo(posts)));
    }

    @Test
    public void shouldSavePostWhenPostIsValid() {
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(validator.validate(any(Post.class))).thenReturn(Collections.emptySet());

        Post saved = postService.save(post, 1);
        assertThat(saved, equalTo(post));
    }

    @Test
    public void shouldThrowExceptionWhenPostIsInvalid() {
        when(validator.validate(any(Post.class))).thenThrow(IllegalModificationException.class);
        assertThrows(IllegalModificationException.class, () -> postService.save(new Post(), 1));
    }

    @Test
    public void shouldUpdatePostWhenPostIsValid() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(updatedPost);
        when(validator.validate(any(Post.class))).thenReturn(Collections.emptySet());

        Post updated = postService.update(updatedPost);
        assertThat(updated, equalTo(updatedPost));
    }

    @Test
    public void shouldNotContainPostWhenDeletesThisPost() {
        when(postRepository.findById(any(Long.class))).thenReturn(Optional.of(post));
        doAnswer(invocation -> when(postRepository.findById(1L)).thenReturn(Optional.empty()))
                .when(postRepository).deleteById(1L);

        postService.deleteById(1L);

        Optional<Post> deleted = postService.findById(1L);
        assertThat(deleted, is(Optional.empty()));
    }
}
