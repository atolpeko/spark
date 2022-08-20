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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("category.UnitTest")
public class AuthProviderServiceImplTest {
    private static UserDetailsService userDetailsService;
    private static PasswordEncoder encoder;

    private static User user;

    private AuthProviderServiceImpl authProviderService;

    @BeforeAll
    public static void setUpMocks() {
        userDetailsService = mock(UserDetailsServiceImpl.class);

        encoder = mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).then(returnsFirstArg());
        when(encoder.matches(anyString(), anyString())).then(invocation -> {
            String rawPassword = invocation.getArgument(0);
            String encodedPassword = invocation.getArgument(1);
            return rawPassword.equals(encodedPassword);
        });
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
        Mockito.reset(userDetailsService);
        authProviderService = new AuthProviderServiceImpl(userDetailsService, encoder);
    }

    @Test
    public void shouldAuthenticateUser() {
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
                user.getUsername(), user.getPassword(), user.getAuthorities()
        );
        userAuth.setAuthenticated(false);

        Authentication authentication = authProviderService.authenticate(userAuth);
        assertThat(authentication.isAuthenticated(), equalTo(true));
    }
}
