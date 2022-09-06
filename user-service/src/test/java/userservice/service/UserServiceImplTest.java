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

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.security.crypto.password.PasswordEncoder;

import userservice.data.UserRepository;
import userservice.service.exception.IllegalModificationException;

import javax.validation.Validator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("category.UnitTest")
public class UserServiceImplTest {
    private static UserRepository userRepository;
    private static PasswordEncoder encoder;
    private static Validator validator;
    private static CircuitBreaker circuitBreaker;

    private static User user;
    private static User updatedUser;

    private UserServiceImpl userService;

    @BeforeAll
    public static void setUpMocks() {
        userRepository = mock(UserRepository.class);
        validator = mock(Validator.class);

        encoder = mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).then(returnsFirstArg());
        when(encoder.matches(anyString(), anyString())).then(invocation -> {
            String rawPassword = invocation.getArgument(0);
            String encodedPassword = invocation.getArgument(1);
            return rawPassword.equals(encodedPassword);
        });

        circuitBreaker = mock(CircuitBreaker.class);
        when(circuitBreaker.decorateSupplier(any())).then(returnsFirstArg());
        when(circuitBreaker.decorateRunnable(any())).then(returnsFirstArg());
    }

    @BeforeAll
    public static void createUser() {
        PersonalData data = PersonalData.builder()
                .withEmail("user@gmail.com")
                .withName("Name")
                .withPhone("011334400")
                .withBirthday(LocalDate.now())
                .build();

        user = User.builder()
                .withLogin("login")
                .withPassword("password")
                .withPersonalData(data)
                .build();
    }

    @BeforeAll
    public static void createUpdatedUser() {
        PersonalData data = PersonalData.builder()
                .withEmail("email2@gmail.com")
                .withName("Name2")
                .withPhone("114234400")
                .withBirthday(LocalDate.now())
                .build();

        updatedUser = User.builder()
                .withLogin("login")
                .withPassword("password2")
                .withPersonalData(data)
                .isBlocked(true)
                .build();
    }

    @BeforeEach
    public void beforeEach() {
        Mockito.reset(userRepository, validator);
        userService = new UserServiceImpl(userRepository, encoder, validator, circuitBreaker);
    }

    @Test
    public void shouldReturnUserByLoginWhenContainsIt() {
        when(userRepository.findById(user.getLogin())).thenReturn(Optional.of(user));

        User saved = userService.findByLogin(user.getLogin()).orElseThrow();
        assertThat(saved, is(equalTo(user)));
    }

    @Test
    public void shouldReturnUserByEmailWhenContainsIt() {
        when(userRepository.findByEmail(user.getPersonalData().getEmail())).thenReturn(Optional.of(user));

        User saved = userService.findByEmail(user.getPersonalData().getEmail()).orElseThrow();
        assertThat(saved, is(equalTo(user)));
    }

    @Test
    public void shouldReturnListOfUsersWhenContainsMultipleUsers() {
        List<User> users = List.of(user, user, user);
        when(userRepository.findAll()).thenReturn(users);

        List<User> saved = userService.findAll();
        assertThat(saved, is(equalTo(users)));
    }

    @Test
    public void shouldCount5UsersWhenContains5Users() {
        when(userRepository.count()).thenReturn(5L);

        long count = userService.count();
        assertThat(count, is(equalTo(5L)));
    }

    @Test
    public void shouldSaveUserWhenUserIsValid() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(validator.validate(any(User.class))).thenReturn(Collections.emptySet());

        User saved = userService.save(user);
        assertThat(saved, equalTo(user));
    }

    @Test
    public void shouldThrowExceptionWhenUserIsInvalid() {
        when(validator.validate(any(User.class))).thenThrow(IllegalModificationException.class);
        assertThrows(IllegalModificationException.class, () -> userService.save(new User()));
    }

    @Test
    public void shouldUpdateUserWhenUserIsValid() {
        when(userRepository.findById("login")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(validator.validate(any(User.class))).thenReturn(Collections.emptySet());

        User updated = userService.update(updatedUser);
        assertThat(updated, equalTo(updatedUser));
    }

    @Test
    public void shouldNotContainUserWhenDeletesThisUser() {
        when(userRepository.findById(any(String.class))).thenReturn(Optional.of(user));
        doAnswer(invocation -> when(userRepository.findById("login")).thenReturn(Optional.empty()))
                .when(userRepository).deleteById("login");

        userService.deleteByLogin("login");

        Optional<User> deleted = userService.findByLogin("login");
        assertThat(deleted, is(Optional.empty()));
    }
}
