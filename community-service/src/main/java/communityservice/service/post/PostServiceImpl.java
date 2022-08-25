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

package communityservice.service.post;

import communityservice.data.CommunityRepository;
import communityservice.data.PostRepository;
import communityservice.data.UserRepository;
import communityservice.service.community.Community;
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
public class PostServiceImpl implements PostService {
    private static final Logger logger = LogManager.getLogger(PostServiceImpl.class);

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final Validator validator;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public PostServiceImpl(CommunityRepository communityRepository,
                           UserRepository userRepository,
                           PostRepository postRepository,
                           Validator validator,
                           CircuitBreaker circuitBreaker) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.validator = validator;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public List<Post> findAllByCommunityId(long communityId) {
        try {
            checkCommunityExistence(communityId);
            Supplier<List<Post>> findAll = () -> postRepository.findAllByCommunityId(communityId);
            List<Post> posts = circuitBreaker.decorateSupplier(findAll).get();
            Collections.sort(posts);
            return posts;
        } catch (Exception e) {
            throw new RemoteResourceException("Post database unavailable", e);
        }
    }

    private void checkCommunityExistence(long id) {
        findCommunity(id).orElseThrow(() -> new NoSuchElementException("No community with ID " + id));
    }

    private Optional<Community> findCommunity(long id) {
        Supplier<Optional<Community>> find = () -> communityRepository.findById(id);
        return circuitBreaker.decorateSupplier(find).get();
    }

    @Override
    public Optional<Post> findById(long id) {
        try {
            Supplier<Optional<Post>> findById = () -> postRepository.findById(id);
            return circuitBreaker.decorateSupplier(findById).get();
        } catch (Exception e) {
            throw new RemoteResourceException("Post database unavailable", e);
        }
    }

    @Override
    public Post save(Post post, long communityId) {
        try {
            Post postToSave = prepareSaveData(post, communityId);
            validate(postToSave);
            Post saved = persistPost(postToSave);
            logger.info("Post " + saved.getId() + " saved");
            return saved;
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteResourceException("Post database unavailable", e);
        }
    }

    private void validate(Post post) {
        validatePost(post);
        checkCommunityExistence(post.getCommunity().getId());
        checkUserExistence(post.getUser().getLogin());
    }

    private void validatePost(Post post) {
        Set<ConstraintViolation<Post>> violations = validator.validate(post);
        if (!violations.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (ConstraintViolation<Post> violation : violations) {
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

    private Post prepareSaveData(Post post, long communityId) {
        Community community = findCommunity(communityId)
                .orElseThrow(() -> new NoSuchElementException("No community with ID " + communityId));
        Post postToSave = new Post(post);
        postToSave.setId(null);
        postToSave.setCommunity(community);

        return postToSave;
    }

    private Post persistPost(Post post) {
        Supplier<Post> save = () -> {
            Post saved = postRepository.save(post);
            postRepository.flush();
            return saved;
        };

        return circuitBreaker.decorateSupplier(save).get();
    }

    @Override
    public Post update(Post post) {
        try {
            long id = post.getId();
            Post postToUpdate = findById(id)
                    .orElseThrow(() -> new IllegalModificationException("No post with ID " + id));
            postToUpdate = prepareUpdateData(postToUpdate, post);
            validate(postToUpdate);

            Post updated = persistPost(postToUpdate);
            logger.info("Post " + updated.getId() + " updated");
            return updated;
        } catch (IllegalModificationException | RemoteResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteResourceException("Post database unavailable", e);
        }
    }

    private Post prepareUpdateData(Post savedPost, Post updateData) {
        return Post.builder(savedPost)
                .copyNonNullFields(updateData)
                .build();
    }

    @Override
    public void deleteById(long id) {
        try {
            deletePost(id);
            logger.info("Post " + id + " deleted");
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalModificationException("No post with ID " + id, e);
        } catch (Exception e) {
            throw new RemoteResourceException("Post database unavailable", e);
        }
    }

    private void deletePost(long id) {
        Runnable delete = () -> {
            postRepository.deleteById(id);
            postRepository.flush();
        };

        circuitBreaker.decorateRunnable(delete).run();
    }
}
