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

package communityservice.service.like.postlike;

import communityservice.data.PostLikeRepository;
import communityservice.data.PostRepository;
import communityservice.data.UserRepository;
import communityservice.service.exception.IllegalModificationException;
import communityservice.service.post.Post;
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

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("category.UnitTest")
public class PostLikeServiceImplTest {
    private static PostLikeRepository likeRepository;
    private static PostRepository postRepository;
    private static UserRepository userRepository;
    private static Validator validator;
    private static CircuitBreaker circuitBreaker;

    private static PostLike like;

    private PostLikeService likeService;

    @BeforeAll
    public static void setUpMocks() {
        likeRepository = mock(PostLikeRepository.class);
        validator = mock(Validator.class);

        postRepository = mock(PostRepository.class);
        Post post = Post.builder()
                .withId(1)
                .build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

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
    public static void createLike() {
        User user = User.builder()
                .withLogin("login")
                .build();
        Post post = Post.builder()
                .withId(1L)
                .build();
        like = PostLike.builder()
                .withId(1L)
                .withUser(user)
                .withPost(post)
                .build();
    }

    @BeforeEach
    public void beforeEach() {
        Mockito.reset(likeRepository, validator);
        likeService = new PostLikeServiceImpl(likeRepository, postRepository,
                userRepository, validator, circuitBreaker);
    }

    @Test
    public void shouldReturnLikeByIdWhenContainsIt() {
        when(likeRepository.findById(like.getId())).thenReturn(Optional.of(like));
        PostLike saved = likeService.findById(like.getId()).orElseThrow();
        assertThat(saved, is(equalTo(like)));
    }

    @Test
    public void shouldReturnListOfLikesWhenContainsMultipleLikes() {
        List<PostLike> likes = new ArrayList<>(List.of(like, like, like));
        when(likeRepository.findAllByPostId(1)).thenReturn(likes);

        List<PostLike> saved = likeService.findAllByPostId(1);
        assertThat(saved, is(equalTo(likes)));
    }

    @Test
    public void shouldSaveLikeWhenLikeIsValid() {
        when(likeRepository.save(any(PostLike.class))).thenReturn(like);
        when(validator.validate(any(PostLike.class))).thenReturn(Collections.emptySet());

        PostLike saved = likeService.save(like, 1);
        assertThat(saved, equalTo(like));
    }

    @Test
    public void shouldThrowExceptionWhenLikeIsInvalid() {
        when(validator.validate(any(PostLike.class))).thenThrow(IllegalModificationException.class);
        assertThrows(IllegalModificationException.class, () -> likeService.save(new PostLike(), 1));
    }

    @Test
    public void shouldNotContainLikeWhenDeletesThisLike() {
        when(likeRepository.findById(any(Long.class))).thenReturn(Optional.of(like));
        doAnswer(invocation -> when(likeRepository.findById(1L)).thenReturn(Optional.empty()))
                .when(likeRepository).deleteById(1L);

        likeService.deleteById(1L);

        Optional<PostLike> deleted = likeService.findById(1L);
        assertThat(deleted, is(Optional.empty()));
    }
}
