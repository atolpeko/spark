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

import communityservice.service.exception.IllegalModificationException;
import communityservice.service.exception.RemoteResourceException;

import java.util.List;

/**
 * Provides user business logic.
 */
public interface UserService {

    /**
     * Looks for all users with the same community ID in the remote user repository.
     *
     * @return all users with the same community ID from the remote user repository.
     *
     * @throws RemoteResourceException if there is any problem with the remote user repository
     */
    List<User> findAllByCommunityId(long communityId);

    /**
     * Adds the specified user to the community with the specified ID.
     * Saves the user in the remote user repository.
     * Use the returned user for further operations as the save operation
     * might have changed the user instance completely.
     *
     * @param communityId ID of the target community
     * @param user user to add
     *
     * @return the saved user
     *
     * @throws IllegalModificationException either if a user has invalid data or already exists
     * @throws RemoteResourceException if there is any problem with the remote user repository
     */
    User subscribeUser(long communityId, User user);

    /**
     * Deletes the specified user with the specified login from the community with the specified ID.
     *
     * @param communityId ID of the target community
     * @param login login of the user to add
     *
     * @throws IllegalModificationException if such a user does not exist
     * @throws RemoteResourceException if there is any problem with the remote user repository
     */
    void unsubscribeUser(long communityId, String login);
}
