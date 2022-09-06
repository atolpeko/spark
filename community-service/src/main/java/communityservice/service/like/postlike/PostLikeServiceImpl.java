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

package communityservice.service.like.postlike;

import communityservice.data.PostLikeRepository;
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
public class PostLikeServiceImpl implements PostLikeService {
    private static final Logger logger = LogManager.getLogger(PostLikeServiceImpl.class);

    private final PostLikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final Validator validator;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public PostLikeServiceImpl(PostLikeRepository likeRepository,
                               PostRepository postRepository,
                               UserRepository userRepository,
                               Validator validator,
                               CircuitBreaker circuitBreaker) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.validator = validator;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public List<PostLike> findAllByPostId(long postId) {
        try {
            checkPostExistence(postId);
            Supplier<List<PostLike>> findAll = () -> likeRepository.findAllByPostId(postId);
            List<PostLike> likes = circuitBreaker.decorateSupplier(findAll).get();
            Collections.sort(likes);
            return likes;
        } catch (Exception e) {
            throw new RemoteResourceException("Like database unavailable", e);
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
    public Optional<PostLike> findById(long id) {
        try {
            Supplier<Optional<PostLike>> findById = () -> likeRepository.findById(id);
            return circuitBreaker.decorateSupplier(findById).get();
        } catch (Exception e) {
            throw new RemoteResourceException("Like database unavailable", e);
        }
    }

    @Override
    public PostLike save(PostLike like, long postId) {
        try {
            PostLike likeToSave = prepareSaveData(like, postId);
            validate(likeToSave);
            PostLike saved = persistLike(likeToSave);
            logger.info("Like " + saved.getId() + " saved");
            return saved;
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteResourceException("Like database unavailable", e);
        }
    }

    private PostLike prepareSaveData(PostLike like, long postId) {
        Post post = findPost(postId)
                .orElseThrow(() -> new NoSuchElementException("No post with ID " + postId));
        PostLike likeToSave = new PostLike(like);
        likeToSave.setId(null);
        likeToSave.setPost(post);

        return likeToSave;
    }

    private void validate(PostLike like) {
        validateLike(like);
        checkPostExistence(like.getPost().getId());
        checkUserExistence(like.getUser().getLogin());
        checkDoubleLike(like);
    }

    private void validateLike(PostLike like) {
        Set<ConstraintViolation<PostLike>> violations = validator.validate(like);
        if (!violations.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (ConstraintViolation<PostLike> violation : violations) {
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

    private void checkDoubleLike(PostLike like) {
        Set<PostLike> likes = like.getPost().getLikes();
        long matches = likes.stream()
                .filter(l -> l.getUser().equals(like.getUser()))
                .count();
        if (matches >= 1) {
            throw new IllegalModificationException("Such a like already exists");
        }
    }

    private PostLike persistLike(PostLike like) {
        Supplier<PostLike> save = () -> {
            PostLike saved = likeRepository.save(like);
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
