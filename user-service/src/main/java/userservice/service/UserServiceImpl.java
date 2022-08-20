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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import userservice.data.UserRepository;
import userservice.service.exception.IllegalModificationException;
import userservice.service.exception.RemoteResourceException;

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

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public UserServiceImpl(UserRepository repository,
                           PasswordEncoder passwordEncoder,
                           Validator validator,
                           CircuitBreaker circuitBreaker) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public List<User> findAll() {
        try {
            Supplier<List<User>> findAll = repository::findAll;
            return circuitBreaker.decorateSupplier(findAll).get();
        } catch (Exception e) {
            throw new RemoteResourceException("User database unavailable", e);
        }
    }

    @Override
    public Optional<User> findById(long id) {
        try {
            Supplier<Optional<User>> findById = () -> repository.findById(id);
            return circuitBreaker.decorateSupplier(findById).get();
        } catch (Exception e) {
            throw new RemoteResourceException("User database unavailable", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            Supplier<Optional<User>> findByEmail = () -> repository.findByEmail(email);
            return circuitBreaker.decorateSupplier(findByEmail).get();
        } catch (Exception e) {
            throw new RemoteResourceException("User database unavailable", e);
        }
    }

    @Override
    public Optional<User> findByLogin(String login) {
        try {
            Supplier<Optional<User>> findByLogin = () -> repository.findByLogin(login);
            return circuitBreaker.decorateSupplier(findByLogin).get();
        } catch (Exception e) {
            throw new RemoteResourceException("User database unavailable", e);
        }
    }

    @Override
    public long count() {
        try {
            Supplier<Long> count = repository::count;
            return circuitBreaker.decorateSupplier(count).get();
        } catch (Exception e) {
            throw new RemoteResourceException("User database unavailable", e);
        }
    }

    @Override
    public User save(User user) {
        try {
            validate(user);
            User userToSave = prepareSaveData(user);
            User saved = persistUser(userToSave);
            logger.info("User " + saved.getEmail() + " saved. ID - " + saved.getId());
            return saved;
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            String msg = "Such a user already exists: " + user.getEmail();
            throw new IllegalModificationException(msg, e);
        } catch (Exception e) {
            throw new RemoteResourceException("User database unavailable", e);
        }
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

    private User prepareSaveData(User user) {
        String password = passwordEncoder.encode(user.getPassword());
        User userToSave = new User(user);
        userToSave.setId(null);
        userToSave.setPassword(password);

        return userToSave;
    }

    private User persistUser(User user) {
        Supplier<User> save = () -> {
            User saved = repository.save(user);
            repository.flush();
            return saved;
        };

        return circuitBreaker.decorateSupplier(save).get();
    }

    @Override
    public User update(User user) {
        try {
            long id = user.getId();
            User userToUpdate = findById(id)
                    .orElseThrow(() -> new IllegalModificationException("No user with id " + id));
            userToUpdate = prepareUpdateData(userToUpdate, user);
            validate(userToUpdate);

            User updated = persistUser(userToUpdate);
            logger.info("User " + updated.getId() + " updated");
            return updated;
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            String msg = "Such a user already exists: " + user.getEmail();
            throw new IllegalModificationException(msg, e);
        } catch (Exception e) {
            throw new RemoteResourceException("User database unavailable", e);
        }
    }

    private User prepareUpdateData(User savedUser, User updateData) {
        return User.builder(savedUser)
                .copyNonNullFields(updateData)
                .build();
    }

    @Override
    public void setBlockedById(long id, boolean isBlocked) {
        try {
            User userToUpdate = findById(id)
                    .orElseThrow(() -> new IllegalModificationException("No user with id " + id));
            userToUpdate.setBlocked(isBlocked);
            persistUser(userToUpdate);
            logger.info("User " + id + (isBlocked ? " blocked" : " unblocked"));
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteResourceException("User database unavailable", e);
        }
    }

    @Override
    public void deleteById(long id) {
        try {
            deleteUser(id);
            logger.info("User " + id + " deleted");
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalModificationException("No user with id " + id, e);
        } catch (Exception e) {
            throw new RemoteResourceException("User database unavailable", e);
        }
    }

    private void deleteUser(long id) {
        Runnable delete = () -> {
            repository.deleteById(id);
            repository.flush();
        };

        circuitBreaker.decorateRunnable(delete).run();
    }
}
