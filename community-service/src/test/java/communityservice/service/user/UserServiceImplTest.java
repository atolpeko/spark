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

package communityservice.service.user;

import communityservice.data.CommunityRepository;
import communityservice.data.UserRepository;
import communityservice.service.community.Community;
import communityservice.service.exception.IllegalModificationException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.validation.Validator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("category.UnitTest")
public class UserServiceImplTest {
    private static CommunityRepository communityRepository;
    private static UserRepository userRepository;
    private static UserServiceFeignClient feignClient;
    private static Validator validator;
    private static CircuitBreaker circuitBreaker;

    private static User user;

    private UserServiceImpl userService;

    @BeforeAll
    public static void setUpMocks() {
        userRepository = mock(UserRepository.class);
        communityRepository = mock(CommunityRepository.class);
        validator = mock(Validator.class);

        feignClient = mock(UserServiceFeignClient.class);
        when(feignClient.findUserByLogin("login")).thenReturn(user);

        circuitBreaker = mock(CircuitBreaker.class);
        when(circuitBreaker.decorateSupplier(any())).then(returnsFirstArg());
        when(circuitBreaker.decorateRunnable(any())).then(returnsFirstArg());
    }

    @BeforeAll
    public static void createUser() {
        user = User.builder()
                .withLogin("login")
                .build();
    }

    @BeforeEach
    public void beforeEach() {
        Mockito.reset(communityRepository, userRepository, validator);
        userService = new UserServiceImpl(communityRepository, userRepository,
                feignClient, validator, circuitBreaker);
    }

    @Test
    public void shouldReturnListOfUsersWhenContainsMultipleUsers() {
        List<User> users = List.of(user, user, user);
        when(userRepository.findAllByCommunityId(1L)).thenReturn(users);

        List<User> saved = userService.findAllByCommunityId(1L);
        assertThat(saved, is(equalTo(users)));
    }

    @Test
    public void shouldSaveUserWhenUserIsValid() {
        when(communityRepository.findById(1L)).thenReturn(Optional.of(new Community()));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(validator.validate(any(Community.class))).thenReturn(Collections.emptySet());

        User saved = userService.subscribeUser(1, user);
        assertThat(saved, equalTo(user));
    }

    @Test
    public void shouldThrowExceptionWhenUserIsInvalid() {
        when(communityRepository.findById(1L)).thenReturn(Optional.of(new Community()));
        when(validator.validate(any(User.class))).thenThrow(IllegalModificationException.class);
        assertThrows(IllegalModificationException.class, () -> userService.subscribeUser(1L, user));
    }
}
