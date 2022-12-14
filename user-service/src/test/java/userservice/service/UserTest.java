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

package userservice.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Tag("category.UnitTest")
public class UserTest {
    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void shouldPassValidationWhenHasValidData() {
        PersonalData data = PersonalData.builder()
                .withEmail("email@gmail.com")
                .withName("Name")
                .withPhone("011334400")
                .withBirthday(LocalDate.now())
                .build();

        User user = User.builder()
                .withLogin("login")
                .withPassword("password")
                .withPersonalData(data)
                .build();

        int errors = validator.validate(user).size();
        assertThat(errors, is(0));
    }

    @Test
    public void shouldNotPassValidationWhenHasInvalidData() {
        PersonalData data = PersonalData.builder()
                .withEmail("not-an-email")
                .build();
        User user = User.builder()
                .withPersonalData(data)
                .build();

        int errors = validator.validate(user).size();
        assertThat(errors, is(6));
    }
}
