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

package communityservice.config;

import communityservice.data.CommunityRepository;
import communityservice.data.UserRepository;
import communityservice.service.user.User;
import communityservice.service.user.UserService;
import communityservice.service.user.UserServiceFeignClient;
import communityservice.service.user.UserServiceImpl;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.validation.Validator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class IntegrationTestConfig {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CircuitBreaker circuitBreaker;

    @Autowired
    private Validator validator;

    @Bean
    @Primary
    public UserService userService() {
        return new UserServiceImpl(communityRepository, userRepository,
                userServiceFeignClient(), validator, circuitBreaker);
    }

    @Bean
    public UserServiceFeignClient userServiceFeignClient() {
        User firstUser = User.builder()
                .withLogin("login1")
                .build();
        User secondUser = User.builder()
                .withLogin("login2")
                .build();
        User thirdUser = User.builder()
                .withLogin("login3")
                .build();

        UserServiceFeignClient feignClient = mock(UserServiceFeignClient.class);
        when(feignClient.findUserByLogin("login1")).thenReturn((firstUser));
        when(feignClient.findUserByLogin("login2")).thenReturn((secondUser));
        when(feignClient.findUserByLogin("login3")).thenReturn((thirdUser));

        return feignClient;
    }
}
