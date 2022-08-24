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

import communityservice.service.exception.IllegalModificationException;
import communityservice.service.exception.RemoteResourceException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Provides post like business logic.
 */
public interface PostLikeService {

    /**
     * Looks for all likes with the same post ID in the remote like repository.
     *
     * @return all like with the same post ID from the remote like repository.
     *
     * @throws NoSuchElementException if such a post does not exist
     * @throws RemoteResourceException if there is any problem with the remote like repository
     */
    List<PostLike> findAllByPostId(long postId);

    /**
     * Looks for a like with the specified ID in the remote like repository.
     *
     * @param id ID of the like to get
     *
     * @return the like with the specified ID in the remote like repository
     * or Optional#empty() if none found
     *
     * @throws RemoteResourceException if there is any problem with the remote like repository
     */
    Optional<PostLike> findById(long id);

    /**
     * Saves the specified like in the remote like repository.
     * Use the returned like for further operations as the save operation
     * might have changed the like instance completely.
     *
     * @param like like to save
     *
     * @return the saved like
     *
     * @throws IllegalModificationException either if a like has invalid data or already exists
     * @throws RemoteResourceException if there is any problem with the remote like repository
     */
    PostLike save(PostLike like);

    /**
     * Deletes the like with the specified ID from the remote like repository.
     *
     * @param id ID of the like to be deleted
     *
     * @throws IllegalModificationException if such a like does not exist
     * @throws RemoteResourceException if there is any problem with the remote like repository
     */
    void deleteById(long id);
}
