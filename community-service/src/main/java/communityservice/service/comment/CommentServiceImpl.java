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

package communityservice.service.comment;

import communityservice.data.CommentRepository;
import communityservice.data.PostRepository;
import communityservice.data.UserRepository;
import communityservice.service.exception.IllegalModificationException;
import communityservice.service.exception.RemoteResourceException;
import communityservice.service.post.Post;
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
public class CommentServiceImpl implements CommentService {
    private static final Logger logger = LogManager.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final Validator validator;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              UserRepository userRepository,
                              Validator validator,
                              CircuitBreaker circuitBreaker) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.validator = validator;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public List<Comment> findAllByPostId(long postId) {
        try {
            checkPostExistence(postId);
            Supplier<List<Comment>> findAll = () -> commentRepository.findAllByPostId(postId);
            List<Comment> comments = circuitBreaker.decorateSupplier(findAll).get();
            Collections.sort(comments);
            return comments;
        } catch (Exception e) {
            throw new RemoteResourceException("Comment database unavailable", e);
        }
    }

    private void checkPostExistence(long id) {
        findPost(id).orElseThrow(() -> new NoSuchElementException("No post with ID " + id));
    }

    private Optional<Post> findPost(long id) {
        Supplier<Optional<Post>> find = () -> postRepository.findById(id);
        return circuitBreaker.decorateSupplier(find).get();
    }

    @Override
    public Optional<Comment> findById(long id) {
        try {
            Supplier<Optional<Comment>> findById = () -> commentRepository.findById(id);
            return circuitBreaker.decorateSupplier(findById).get();
        } catch (Exception e) {
            throw new RemoteResourceException("Comment database unavailable", e);
        }
    }

    @Override
    public Comment save(Comment comment, long postId) {
        try {
            Comment commentToSave = prepareSaveData(comment, postId);
            validate(commentToSave);
            Comment saved = persistComment(commentToSave);
            logger.info("Comment " + saved.getId() + " saved");
            return saved;
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteResourceException("Comment database unavailable", e);
        }
    }

    private Comment prepareSaveData(Comment comment, long postId) {
        Post post = findPost(postId)
                .orElseThrow(() -> new NoSuchElementException("No post with ID " + postId));
        Comment commentToSave = new Comment(comment);
        commentToSave.setId(null);
        commentToSave.setPost(post);

        return commentToSave;
    }

    private void validate(Comment comment) {
        validateComment(comment);
        checkPostExistence(comment.getPost().getId());
        checkUserExistence(comment.getUser().getLogin());
    }

    private void validateComment(Comment comment) {
        Set<ConstraintViolation<Comment>> violations = validator.validate(comment);
        if (!violations.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (ConstraintViolation<Comment> violation : violations) {
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

    private Comment persistComment(Comment comment) {
        Supplier<Comment> save = () -> {
            Comment saved = commentRepository.save(comment);
            commentRepository.flush();
            return saved;
        };

        return circuitBreaker.decorateSupplier(save).get();
    }

    @Override
    public Comment update(Comment comment) {
        try {
            long id = comment.getId();
            Comment commentToUpdate = findById(id)
                    .orElseThrow(() -> new IllegalModificationException("No comment with ID " + id));
            commentToUpdate = prepareUpdateData(commentToUpdate, comment);
            validate(commentToUpdate);

            Comment updated = persistComment(commentToUpdate);
            logger.info("Comment " + updated.getId() + " updated");
            return updated;
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteResourceException("Comment database unavailable", e);
        }
    }

    private Comment prepareUpdateData(Comment savedComment, Comment updateData) {
        return Comment.builder(savedComment)
                .copyNonNullFields(updateData)
                .build();
    }

    @Override
    public void deleteById(long id) {
        try {
            deleteComment(id);
            logger.info("Comment " + id + " deleted");
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalModificationException("No comment with ID " + id, e);
        } catch (Exception e) {
            throw new RemoteResourceException("Comment database unavailable", e);
        }
    }

    private void deleteComment(long id) {
        Runnable delete = () -> {
            commentRepository.deleteById(id);
            commentRepository.flush();
        };

        circuitBreaker.decorateRunnable(delete).run();
    }
}
