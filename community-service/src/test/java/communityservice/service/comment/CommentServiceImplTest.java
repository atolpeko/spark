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

package communityservice.service.comment;

import communityservice.data.CommentRepository;
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
public class CommentServiceImplTest {
    private static CommentRepository commentRepository;
    private static PostRepository postRepository;
    private static UserRepository userRepository;
    private static Validator validator;
    private static CircuitBreaker circuitBreaker;

    private static Comment comment;
    private static Comment updatedComment;

    private CommentServiceImpl commentService;

    @BeforeAll
    public static void setUpMocks() {
        commentRepository = mock(CommentRepository.class);
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
    public static void createComment() {
        User user = User.builder()
                .withLogin("login")
                .build();
        Post post = Post.builder()
                .withId(1L)
                .build();
        comment = Comment.builder()
                .withId(1L)
                .withMessage("message")
                .withUser(user)
                .withPost(post)
                .build();
    }

    @BeforeAll
    public static void createUpdatedComment() {
        User user = User.builder()
                .withLogin("login")
                .build();
        Post post = Post.builder()
                .withId(1L)
                .build();
        updatedComment = Comment.builder()
                .withId(1L)
                .withMessage("message2")
                .withUser(user)
                .withPost(post)
                .build();
    }

    @BeforeEach
    public void beforeEach() {
        Mockito.reset(commentRepository, validator);
        commentService = new CommentServiceImpl(commentRepository, postRepository,
                userRepository, validator, circuitBreaker);
    }

    @Test
    public void shouldReturnCommentByIdWhenContainsIt() {
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        Comment saved = commentService.findById(comment.getId()).orElseThrow();
        assertThat(saved, is(equalTo(comment)));
    }

    @Test
    public void shouldReturnListOfCommentsWhenContainsMultipleComments() {
        List<Comment> comments = new ArrayList<>(List.of(comment, comment, comment));
        when(commentRepository.findAllByPostId(1)).thenReturn(comments);

        List<Comment> saved = commentService.findAllByPostId(1);
        assertThat(saved, is(equalTo(comments)));
    }

    @Test
    public void shouldSaveCommentWhenCommentIsValid() {
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(validator.validate(any(Comment.class))).thenReturn(Collections.emptySet());

        Comment saved = commentService.save(comment, 1);
        assertThat(saved, equalTo(comment));
    }

    @Test
    public void shouldThrowExceptionWhenCommentIsInvalid() {
        when(validator.validate(any(Comment.class))).thenThrow(IllegalModificationException.class);
        assertThrows(IllegalModificationException.class, () -> commentService.save(new Comment(), 1));
    }

    @Test
    public void shouldUpdateCommentWhenCommentIsValid() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);
        when(validator.validate(any(Post.class))).thenReturn(Collections.emptySet());

        Comment updated = commentService.update(updatedComment);
        assertThat(updated, equalTo(updatedComment));
    }

    @Test
    public void shouldNotContainCommentWhenDeletesThisComment() {
        when(commentRepository.findById(any(Long.class))).thenReturn(Optional.of(comment));
        doAnswer(invocation -> when(commentRepository.findById(1L)).thenReturn(Optional.empty()))
                .when(commentRepository).deleteById(1L);

        commentService.deleteById(1L);

        Optional<Comment> deleted = commentService.findById(1L);
        assertThat(deleted, is(Optional.empty()));
    }
}
