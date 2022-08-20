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

package authservice.service;

import authservice.data.UserRepository;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("category.UnitTest")
public class UserDetailsServiceImplTest {
    private static UserRepository userRepository;
    private static CircuitBreaker circuitBreaker;

    private static User user;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeAll
    public static void setUpMocks() {
        userRepository = mock(UserRepository.class);

        circuitBreaker = mock(CircuitBreaker.class);
        when(circuitBreaker.decorateSupplier(any())).then(returnsFirstArg());
        when(circuitBreaker.decorateRunnable(any())).then(returnsFirstArg());
    }

    @BeforeAll
    public static void createUser() {
        user = User.builder()
                .withId(1L)
                .withUsername("user@gmail.com")
                .withPassword("12345678")
                .withRole(User.Role.USER)
                .isBlocked(false)
                .build();
    }

    @BeforeEach
    public void beforeEach() {
        Mockito.reset(userRepository);
        userDetailsService = new UserDetailsServiceImpl(userRepository, circuitBreaker);
    }

    @Test
    public void shouldReturnUserByUsernameWhenContainsIt() {
        String username = user.getUsername();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails saved = userDetailsService.loadUserByUsername(username);
        assertThat(saved, is(equalTo(user)));
    }
}
