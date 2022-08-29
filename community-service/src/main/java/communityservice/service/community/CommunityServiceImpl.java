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
import communityservice.data.UserRepository;
import communityservice.service.exception.IllegalModificationException;
import communityservice.service.exception.RemoteResourceException;

import communityservice.service.user.User;
import communityservice.service.user.UserServiceFeignClient;

import feign.FeignException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Service
@Transactional
public class CommunityServiceImpl implements CommunityService {
    private static final Logger logger = LogManager.getLogger(CommunityServiceImpl.class);

    private final CommunityRepository repository;
    private final UserServiceFeignClient userService;
    private final Validator validator;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public CommunityServiceImpl(CommunityRepository repository,
                                UserServiceFeignClient userService,
                                Validator validator,
                                CircuitBreaker circuitBreaker) {
        this.repository = repository;
        this.userService = userService;
        this.validator = validator;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public List<Community> findAll() {
        try {
            Supplier<List<Community>> findAll = repository::findAll;
            List<Community> communities = circuitBreaker.decorateSupplier(findAll).get();
            Collections.sort(communities);
            return communities;
        } catch (Exception e) {
            throw new RemoteResourceException("Community database unavailable", e);
        }
    }

    @Override
    public Optional<Community> findById(long id) {
        try {
            Supplier<Optional<Community>> findById = () -> repository.findById(id);
            return circuitBreaker.decorateSupplier(findById).get();
        } catch (Exception e) {
            throw new RemoteResourceException("Community database unavailable", e);
        }
    }

    @Override
    public Optional<Community> findByName(String name) {
        try {
            Supplier<Optional<Community>> findByName = () -> repository.findByName(name);
            return circuitBreaker.decorateSupplier(findByName).get();
        } catch (Exception e) {
            throw new RemoteResourceException("Community database unavailable", e);
        }
    }

    @Override
    public Community save(Community community) {
        try {
            validate(community);
            Community communityToSave = prepareSaveData(community);
            Community saved = persistCommunity(communityToSave);
            logger.info("Community " + saved.getId() + " saved");
            return saved;
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            String msg = "Such a community already exists: " + community.getId();
            throw new IllegalModificationException(msg, e);
        } catch (Exception e) {
            throw new RemoteResourceException("Community database unavailable", e);
        }
    }

    private Community prepareSaveData(Community community) {
        Community communityToSave = new Community(community);
        communityToSave.setId(null);

        return communityToSave;
    }

    private void validate(Community community) {
        validateCommunity(community);
        checkUserExistence(community.getAdminLogin());
    }

    private void validateCommunity(Community community) {
        Set<ConstraintViolation<Community>> violations = validator.validate(community);
        if (!violations.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (ConstraintViolation<Community> violation : violations) {
                builder.append(violation.getMessage()).append(", ");
            }

            builder.delete(builder.length() - 2, builder.length() - 1);
            String msg = builder.toString().toLowerCase(Locale.ROOT);
            throw new IllegalModificationException(msg);
        }
    }

    private void checkUserExistence(String login) {
        try {
            Supplier<User> findUser = () -> userService.findUserByLogin(login);
            circuitBreaker.decorateSupplier(findUser).get();
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new IllegalModificationException("User not found: " + login);
            } else {
                throw new RemoteResourceException("User microservice unavailable: " + e.getMessage());
            }
        } catch (OAuth2AccessDeniedException e) {
            throw new RemoteResourceException("Auth service unavailable: " + e.getMessage());
        }
    }

    private Community persistCommunity(Community community) {
        Supplier<Community> save = () -> {
            Community saved = repository.save(community);
            repository.flush();
            return saved;
        };

        return circuitBreaker.decorateSupplier(save).get();
    }

    @Override
    public Community update(Community community) {
        try {
            long id = community.getId();
            Community communityToUpdate = findById(id)
                    .orElseThrow(() -> new IllegalModificationException("No community with ID " + id));
            communityToUpdate = prepareUpdateData(communityToUpdate, community);
            validate(communityToUpdate);

            Community updated = persistCommunity(communityToUpdate);
            logger.info("Community " + updated.getId() + " updated");
            return updated;
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            String msg = "Such a community already exists: " + community.getId();
            throw new IllegalModificationException(msg, e);
        } catch (Exception e) {
            throw new RemoteResourceException("Community database unavailable", e);
        }
    }

    private Community prepareUpdateData(Community savedCommunity, Community updateData) {
        return Community.builder(savedCommunity)
                .copyNonNullFields(updateData)
                .build();
    }

    @Override
    public void deleteById(long id) {
        try {
            deleteCommunity(id);
            logger.info("Community " + id + " deleted");
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalModificationException("No community with ID " + id, e);
        } catch (Exception e) {
            throw new RemoteResourceException("Community database unavailable", e);
        }
    }

    private void deleteCommunity(long id) {
        Runnable delete = () -> {
            repository.deleteById(id);
            repository.flush();
        };

        circuitBreaker.decorateRunnable(delete).run();
    }
}
