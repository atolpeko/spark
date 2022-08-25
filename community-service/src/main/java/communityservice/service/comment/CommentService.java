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

import communityservice.service.exception.IllegalModificationException;
import communityservice.service.exception.RemoteResourceException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Provides comment business logic.
 */
public interface CommentService {

    /**
     * Looks for all comments with the same post ID in the remote comment repository.
     *
     * @param postId ID of the post with comment to find
     *
     * @return all comments with the same post ID from the remote comment repository.
     *
     * @throws NoSuchElementException if such a post does not exist
     * @throws RemoteResourceException if there is any problem with the remote comment repository
     */
    List<Comment> findAllByPostId(long postId);

    /**
     * Looks for a comment with the specified ID in the remote comment repository.
     *
     * @param id ID of the comment to get
     *
     * @return the comment with the specified ID in the remote comment repository
     * or Optional#empty() if none found
     *
     * @throws RemoteResourceException if there is any problem with the remote comment repository
     */
    Optional<Comment> findById(long id);

    /**
     * Saves the specified comment in the remote comment repository.
     * Use the returned comment for further operations as the save operation
     * might have changed the comment instance completely.
     *
     * @param comment comment to save
     * @param postId ID of the post to save comment to
     *
     * @return the saved comment
     *
     * @throws IllegalModificationException either if a comment has invalid data or already exists
     * @throws RemoteResourceException if there is any problem with the remote comment repository
     */
    Comment save(Comment comment, long postId);

    /**
     * Updates the comment with the specified ID in the remote comment repository.
     * Use the returned comment for further operations as the update operation
     * might have changed the comment instance completely.
     *
     * @param comment comment to update
     *
     * @return the updated comment
     *
     * @throws IllegalModificationException either if a comment has invalid data or does not exist
     * @throws RemoteResourceException if there is any problem with the remote comment repository
     */
    Comment update(Comment comment);

    /**
     * Deletes the comment with the specified ID from the remote comment repository.
     *
     * @param id ID of the comment to be deleted
     *
     * @throws IllegalModificationException if such a comment does not exist
     * @throws RemoteResourceException if there is any problem with the remote comment repository
     */
    void deleteById(long id);
}
