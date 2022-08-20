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

import userservice.service.exception.IllegalModificationException;
import userservice.service.exception.RemoteResourceException;

import java.util.List;
import java.util.Optional;

/**
 * Provides user business logic.
 */
public interface UserService {

    /**
     * Looks for all users in the remote user repository.
     *
     * @return all users from the remote user repository
     *
     * @throws RemoteResourceException if there is any problem with the remote user repository
     */
    List<User> findAll();

    /**
     * Looks for a user with the specified ID in the remote user repository.
     *
     * @param id ID of the user to get
     *
     * @return the user with the specified ID in the remote user repository
     * or Optional#empty() if none found
     *
     * @throws RemoteResourceException if there is any problem with the remote user repository
     */
    Optional<User> findById(long id);

    /**
     * Looks for a user with the specified email in the remote user repository.
     *
     * @param email email of the user to get
     *
     * @return the user with the specified email in the remote user repository
     * or Optional#empty() if none found
     *
     * @throws RemoteResourceException if there is any problem with the remote user repository
     */
    Optional<User> findByEmail(String email);

    /**
     * Looks for a user with the specified login in the remote user repository.
     *
     * @param login login of the user to get
     *
     * @return the user with the specified login in the remote user repository
     * or Optional#empty() if none found
     *
     * @throws RemoteResourceException if there is any problem with the remote user repository
     */
    Optional<User> findByLogin(String login);

    /**
     * Counts the number of user in the remote user repository.
     *
     * @return the number of user in the remote user repository
     *
     * @throws RemoteResourceException if there is any problem with the remote user repository
     */
    long count();

    /**
     * Saves the specified user in the remote user repository.
     * Use the returned user for further operations as the save operation
     * might have changed the user instance completely.
     *
     * @param user user to save
     *
     * @return the saved user
     *
     * @throws IllegalModificationException either if a user has invalid data or already exists
     * @throws RemoteResourceException if there is any problem with the remote user repository
     */
    User save(User user);

    /**
     * Updates the user with the specified ID in the remote user repository.
     * Use the returned user for further operations as the update operation
     * might have changed the user instance completely.
     *
     * @param user user to update
     *
     * @return the updated user
     *
     * @throws IllegalModificationException either if a user has invalid data or does not exist
     * @throws RemoteResourceException if there is any problem with the remote user repository
     */
    User update(User user);

    /**
     * Blocks or unblocks the user with the specified ID in the remote user repository.
     *
     * @param id the ID of the user to be blocked / unblocked
     *
     * @throws IllegalModificationException if such a user does not exist
     * @throws RemoteResourceException if there is any problem with the remote user repository
     */
    void setBlockedById(long id, boolean isBlocked);

    /**
     * Deletes the user with the specified ID in the remote user repository.
     *
     * @param id the ID of the user to be deleted
     *
     * @throws IllegalModificationException if such a user does not exist
     * @throws RemoteResourceException if there is any problem with the remote user repository
     */
    void deleteById(long id);
}
