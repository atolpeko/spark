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

package communityservice.service.like.commentlike;

import communityservice.data.CommentLikeRepository;
import communityservice.data.CommentRepository;
import communityservice.data.UserRepository;
import communityservice.service.comment.Comment;
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

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("category.UnitTest")
public class CommentLikeServiceImplTest {
    private static CommentLikeRepository likeRepository;
    private static CommentRepository commentRepository;
    private static UserRepository userRepository;
    private static Validator validator;
    private static CircuitBreaker circuitBreaker;

    private static CommentLike like;

    private CommentLikeService likeService;

    @BeforeAll
    public static void setUpMocks() {
        likeRepository = mock(CommentLikeRepository.class);
        validator = mock(Validator.class);

        commentRepository = mock(CommentRepository.class);
        Comment comment = Comment.builder()
                .withId(1)
                .build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

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
        Comment comment = Comment.builder()
                .withId(1)
                .build();
        like = CommentLike.builder()
                .withId(1L)
                .withUser(user)
                .withComment(comment)
                .build();
    }

    @BeforeEach
    public void beforeEach() {
        Mockito.reset(likeRepository, validator);
        likeService = new CommentLikeServiceImpl(likeRepository, commentRepository,
                userRepository, validator, circuitBreaker);
    }

    @Test
    public void shouldReturnLikeByIdWhenContainsIt() {
        when(likeRepository.findById(like.getId())).thenReturn(Optional.of(like));
        CommentLike saved = likeService.findById(like.getId()).orElseThrow();
        assertThat(saved, is(equalTo(like)));
    }

    @Test
    public void shouldReturnListOfLikesWhenContainsMultipleLikes() {
        List<CommentLike> likes = new ArrayList<>(List.of(like, like, like));
        when(likeRepository.findAllByCommentId(1)).thenReturn(likes);

        List<CommentLike> saved = likeService.findAllByCommentId(1);
        assertThat(saved, is(equalTo(likes)));
    }

    @Test
    public void shouldSaveLikeWhenLikeIsValid() {
        when(likeRepository.save(any(CommentLike.class))).thenReturn(like);
        when(validator.validate(any(CommentLike.class))).thenReturn(Collections.emptySet());

        CommentLike saved = likeService.save(like, 1);
        assertThat(saved, equalTo(like));
    }

    @Test
    public void shouldThrowExceptionWhenLikeIsInvalid() {
        when(validator.validate(any(CommentLike.class))).thenThrow(IllegalModificationException.class);
        assertThrows(IllegalModificationException.class, () -> likeService.save(new CommentLike(), 1));
    }

    @Test
    public void shouldNotContainLikeWhenDeletesThisLike() {
        when(likeRepository.findById(any(Long.class))).thenReturn(Optional.of(like));
        doAnswer(invocation -> when(likeRepository.findById(1L)).thenReturn(Optional.empty()))
                .when(likeRepository).deleteById(1L);

        likeService.deleteById(1L);

        Optional<CommentLike> deleted = likeService.findById(1L);
        assertThat(deleted, is(Optional.empty()));
    }
}
