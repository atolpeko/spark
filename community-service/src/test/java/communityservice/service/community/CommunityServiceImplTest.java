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

package communityservice.service.community;

import communityservice.data.CommunityRepository;
import communityservice.service.exception.IllegalModificationException;
import communityservice.service.user.User;
import communityservice.service.user.UserServiceFeignClient;

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
public class CommunityServiceImplTest {
    private static CommunityRepository communityRepository;
    private static UserServiceFeignClient feignClient;
    private static Validator validator;
    private static CircuitBreaker circuitBreaker;

    private static Community community;
    private static Community updatedCommunity;

    private CommunityServiceImpl communityService;

    @BeforeAll
    public static void setUpMocks() {
        communityRepository = mock(CommunityRepository.class);
        validator = mock(Validator.class);

        feignClient = mock(UserServiceFeignClient.class);
        User user = User.builder()
                .withLogin("login")
                .build();
        when(feignClient.findUserByLogin("login")).thenReturn(user);

        circuitBreaker = mock(CircuitBreaker.class);
        when(circuitBreaker.decorateSupplier(any())).then(returnsFirstArg());
        when(circuitBreaker.decorateRunnable(any())).then(returnsFirstArg());
    }

    @BeforeAll
    public static void createCommunity() {
        community = Community.builder()
                .withId(1L)
                .withName("name")
                .withAdminLogin("login")
                .withDescription("desc")
                .build();
    }

    @BeforeAll
    public static void createUpdatedCommunity() {
        updatedCommunity = Community.builder()
                .withId(1L)
                .withName("name2")
                .withDescription("desc2")
                .build();
    }

    @BeforeEach
    public void beforeEach() {
        Mockito.reset(communityRepository, validator);
        communityService = new CommunityServiceImpl(communityRepository,
                feignClient, validator, circuitBreaker);
    }

    @Test
    public void shouldReturnCommunityByIdWhenContainsIt() {
        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));

        Community saved = communityService.findById(community.getId()).orElseThrow();
        assertThat(saved, is(equalTo(community)));
    }

    @Test
    public void shouldReturnCommunityByNameWhenContainsIt() {
        when(communityRepository.findByName(community.getName())).thenReturn(Optional.of(community));

        Community saved = communityService.findByName(community.getName()).orElseThrow();
        assertThat(saved, is(equalTo(community)));
    }

    @Test
    public void shouldReturnListOfCommunitiesWhenContainsMultipleCommunities() {
        List<Community> communities = new ArrayList<>(List.of(community, community, community));
        when(communityRepository.findAll()).thenReturn(communities);

        List<Community> saved = communityService.findAll();
        assertThat(saved, is(equalTo(communities)));
    }

    @Test
    public void shouldSaveCommunityWhenCommunityIsValid() {
        when(communityRepository.save(any(Community.class))).thenReturn(community);
        when(validator.validate(any(Community.class))).thenReturn(Collections.emptySet());

        Community saved = communityService.save(community);
        assertThat(saved, equalTo(community));
    }

    @Test
    public void shouldThrowExceptionWhenCommunityIsInvalid() {
        when(validator.validate(any(Community.class))).thenThrow(IllegalModificationException.class);
        assertThrows(IllegalModificationException.class, () -> communityService.save(new Community()));
    }

    @Test
    public void shouldUpdateCommunityWhenCommunityIsValid() {
        when(communityRepository.findById(1L)).thenReturn(Optional.of(community));
        when(communityRepository.save(any(Community.class))).thenReturn(updatedCommunity);
        when(validator.validate(any(Community.class))).thenReturn(Collections.emptySet());

        Community updated = communityService.update(updatedCommunity);
        assertThat(updated, equalTo(updatedCommunity));
    }

    @Test
    public void shouldNotContainCommunityWhenDeletesThisCommunity() {
        when(communityRepository.findById(any(Long.class))).thenReturn(Optional.of(community));
        doAnswer(invocation -> when(communityRepository.findById(1L)).thenReturn(Optional.empty()))
                .when(communityRepository).deleteById(1L);

        communityService.deleteById(1L);

        Optional<Community> deleted = communityService.findById(1L);
        assertThat(deleted, is(Optional.empty()));
    }
}
