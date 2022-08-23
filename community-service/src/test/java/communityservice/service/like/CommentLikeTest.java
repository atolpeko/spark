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

package communityservice.service.like;

import communityservice.service.comment.Comment;
import communityservice.service.user.User;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Tag("category.UnitTest")
public class CommentLikeTest {
    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void shouldPassValidationWhenHasValidData() {
        User user = User.builder()
                .withLogin("login")
                .build();
        CommentLike commentLike = CommentLike.builder()
                .withUser(user)
                .withComment(new Comment())
                .build();

        int errors = validator.validate(commentLike).size();
        assertThat(errors, is(0));
    }

    @Test
    public void shouldNotPassValidationWhenHasInvalidData() {
        User user = new User();
        CommentLike commentLike = CommentLike.builder()
                .withUser(user)
                .build();

        int errors = validator.validate(commentLike).size();
        assertThat(errors, is(2));
    }
}
