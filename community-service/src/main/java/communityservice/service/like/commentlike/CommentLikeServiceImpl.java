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

package communityservice.service.like.commentlike;

import communityservice.data.CommentLikeRepository;
import communityservice.data.CommentRepository;
import communityservice.data.UserRepository;
import communityservice.service.comment.Comment;
import communityservice.service.exception.IllegalModificationException;
import communityservice.service.exception.RemoteResourceException;
import communityservice.service.user.User;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Service
@Transactional
public class CommentLikeServiceImpl implements CommentLikeService {
    private static final Logger logger = LogManager.getLogger(CommentLikeServiceImpl.class);

    private final CommentLikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final Validator validator;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public CommentLikeServiceImpl(CommentLikeRepository likeRepository,
                                  CommentRepository commentRepository,
                                  UserRepository userRepository,
                                  Validator validator,
                                  CircuitBreaker circuitBreaker) {
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.validator = validator;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public List<CommentLike> findAllByCommentId(long commentId) {
        try {
            checkCommentExistence(commentId);
            Supplier<List<CommentLike>> findAll = () -> likeRepository.findAllByCommentId(commentId);
            List<CommentLike> likes = circuitBreaker.decorateSupplier(findAll).get();
            Collections.sort(likes);
            return likes;
        } catch (Exception e) {
            throw new RemoteResourceException("Like database unavailable", e);
        }
    }

    private void checkCommentExistence(long id) {
        Supplier<Optional<Comment>> find = () -> commentRepository.findById(id);
        find.get().orElseThrow(() -> new NoSuchElementException("No comment with ID " + id));
    }

    @Override
    public Optional<CommentLike> findById(long id) {
        try {
            Supplier<Optional<CommentLike>> findById = () -> likeRepository.findById(id);
            return circuitBreaker.decorateSupplier(findById).get();
        } catch (Exception e) {
            throw new RemoteResourceException("Like database unavailable", e);
        }
    }

    @Override
    public CommentLike save(CommentLike like) {
        try {
            validate(like);
            CommentLike likeToSave = prepareSaveData(like);
            CommentLike saved = persistLike(likeToSave);
            logger.info("Like " + saved.getId() + " saved");
            return saved;
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteResourceException("Like database unavailable", e);
        }
    }

    private void validate(CommentLike like) {
        validateLike(like);
        checkCommentExistence(like.getComment().getId());
        checkUserExistence(like.getUser().getLogin());
        checkDoubleLike(like);
    }

    private void validateLike(CommentLike like) {
        Set<ConstraintViolation<CommentLike>> violations = validator.validate(like);
        if (!violations.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (ConstraintViolation<CommentLike> violation : violations) {
                builder.append(violation.getMessage()).append(", ");
            }

            builder.delete(builder.length() - 2, builder.length() - 1);
            String msg = builder.toString().toLowerCase(Locale.ROOT);
            throw new IllegalModificationException(msg);
        }
    }

    private void checkUserExistence(String userLogin) {
        Supplier<Optional<User>> find = () -> userRepository.findById(userLogin);
        find.get().orElseThrow(() -> new IllegalModificationException("No user with login " + userLogin));
    }

    private void checkDoubleLike(CommentLike like) {
        Set<CommentLike> likes = like.getComment().getLikes();
        long matches = likes.stream()
                .filter(l -> l.getUser().equals(like.getUser()))
                .count();
        if (matches >= 1) {
            throw new IllegalModificationException("Such a like already exists");
        }
    }

    private CommentLike prepareSaveData(CommentLike like) {
        CommentLike likeToSave = new CommentLike(like);
        likeToSave.setId(null);

        return likeToSave;
    }

    private CommentLike persistLike(CommentLike like) {
        Supplier<CommentLike> save = () -> {
            CommentLike saved = likeRepository.save(like);
            likeRepository.flush();
            return saved;
        };

        return circuitBreaker.decorateSupplier(save).get();
    }

    @Override
    public void deleteById(long id) {
        try {
            deleteLike(id);
            logger.info("Like " + id + " deleted");
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalModificationException("No like with ID " + id, e);
        } catch (Exception e) {
            throw new RemoteResourceException("Like database unavailable", e);
        }
    }

    private void deleteLike(long id) {
        Runnable delete = () -> {
            likeRepository.deleteById(id);
            likeRepository.flush();
        };

        circuitBreaker.decorateRunnable(delete).run();
    }
}
