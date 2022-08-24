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
import communityservice.service.exception.RemoteResourceException;

import feign.FeignException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final UserServiceFeignClient feignClient;
    private final Validator validator;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public UserServiceImpl(CommunityRepository communityRepository,
                           UserRepository userRepository,
                           UserServiceFeignClient feignClient,
                           Validator validator,
                           CircuitBreaker circuitBreaker) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.feignClient = feignClient;
        this.validator = validator;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public List<User> findAllByCommunityId(long communityId) {
        try {
            Supplier<List<User>> findAll = () -> userRepository.findAllByCommunityId(communityId);
            return circuitBreaker.decorateSupplier(findAll).get();
        } catch (Exception e) {
            throw new RemoteResourceException("User database unavailable", e);
        }
    }

    @Override
    public User subscribeUser(long communityId, User user) {
        try {
            Community community = getCommunity(communityId);
            checkUserExistence(user);
            user.addCommunity(community);
            validate(user);
            User saved = persistUser(user);
            logger.info("User " + saved.getLogin() + " joined community " + community.getName());
            return saved;
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            String msg = "This user already joined this community";
            throw new IllegalModificationException(msg, e);
        } catch (Exception e) {
            throw new RemoteResourceException("User database unavailable", e);
        }
    }

    private Community getCommunity(long id) {
        Supplier<Optional<Community>> findById = () -> communityRepository.findById(id);
        return circuitBreaker.decorateSupplier(findById).get()
                .orElseThrow(() -> new IllegalModificationException("No community with ID " + id));
    }

    private void validate(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (ConstraintViolation<User> violation : violations) {
                builder.append(violation.getMessage()).append(", ");
            }

            builder.delete(builder.length() - 2, builder.length() - 1);
            String msg = builder.toString().toLowerCase(Locale.ROOT);
            throw new IllegalModificationException(msg);
        }
    }

    private User persistUser(User user) {
        Supplier<User> save = () -> {
            User saved = userRepository.save(user);
            userRepository.flush();
            return saved;
        };

        return circuitBreaker.decorateSupplier(save).get();
    }

    private void checkUserExistence(User user) {
        try {
            Supplier<User> findUser = () -> feignClient.findUserByLogin(user.getLogin());
            circuitBreaker.decorateSupplier(findUser).get();
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new IllegalModificationException("User not found: " + user.getLogin());
            } else {
                throw new RemoteResourceException("User microservice unavailable: " + e.getMessage());
            }
        } catch (OAuth2AccessDeniedException e) {
            throw new RemoteResourceException("Auth service unavailable: " + e.getMessage());
        }
    }

    @Override
    public void unsubscribeUser(long communityId, String login) {
        try {
            Community community = getCommunity(communityId);
            User user = findByLogin(login)
                    .orElseThrow(() -> new IllegalModificationException("No user with login " + login));
            checkUserExistence(user);
            user.deleteCommunity(community);
            validate(user);
            if (user.getCommunities().isEmpty()) {
                persistUser(user);
            } else {
                deleteUser(login);
            }
            logger.info("User " + login + " left community " + community.getName());
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            String msg = "This user already left this community";
            throw new IllegalModificationException(msg, e);
        } catch (Exception e) {
            throw new RemoteResourceException("User database unavailable", e);
        }
    }

    private Optional<User> findByLogin(String login) {
        Supplier<Optional<User>> findByLogin = () -> userRepository.findById(login);
        return circuitBreaker.decorateSupplier(findByLogin).get();
    }

    private void deleteUser(String login) {
        Runnable delete = () -> {
            userRepository.deleteById(login);
            userRepository.flush();
        };

        circuitBreaker.decorateRunnable(delete).run();
    }
}
