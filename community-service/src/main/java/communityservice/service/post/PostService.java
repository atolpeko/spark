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

import communityservice.service.exception.IllegalModificationException;
import communityservice.service.exception.RemoteResourceException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Provides post business logic.
 */
public interface PostService {

    /**
     * Looks for all posts with the same community ID in the remote post repository.
     *
     * @return all posts with the same community ID from the remote post repository.
     *
     * @throws NoSuchElementException if such a community does not exist
     * @throws RemoteResourceException if there is any problem with the remote post repository
     */
    List<Post> findAllByCommunityId(long communityId);

    /**
     * Looks for a post with the specified ID in the remote post repository.
     *
     * @param id ID of the post to get
     *
     * @return the post with the specified ID in the remote post repository
     * or Optional#empty() if none found
     *
     * @throws RemoteResourceException if there is any problem with the remote post repository
     */
    Optional<Post> findById(long id);

    /**
     * Saves the specified post in the remote post repository.
     * Use the returned post for further operations as the save operation
     * might have changed the post instance completely.
     *
     * @param post post to save
     * @param communityId ID of the community to save post to
     *
     * @return the saved post
     *
     * @throws IllegalModificationException either if a post has invalid data or already exists
     * @throws RemoteResourceException if there is any problem with the remote post repository
     */
    Post save(Post post, long communityId);

    /**
     * Updates the post with the specified ID in the remote post repository.
     * Use the returned post for further operations as the update operation
     * might have changed the post instance completely.
     *
     * @param post post to update
     *
     * @return the updated post
     *
     * @throws IllegalModificationException either if a post has invalid data or does not exist
     * @throws RemoteResourceException if there is any problem with the remote post repository
     */
    Post update(Post post);

    /**
     * Deletes the post with the specified ID in the remote post repository.
     *
     * @param id ID of the post to be deleted
     *
     * @throws IllegalModificationException if such a post does not exist
     * @throws RemoteResourceException if there is any problem with the remote post repository
     */
    void deleteById(long id);
}
