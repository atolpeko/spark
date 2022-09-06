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

package communityservice.service.community;

import communityservice.service.exception.IllegalModificationException;
import communityservice.service.exception.RemoteResourceException;

import java.util.List;
import java.util.Optional;

/**
 * Provides community business logic.
 */
public interface CommunityService {

    /**
     * Looks for all communities in the remote community repository.
     *
     * @return all communities from the remote community repository
     *
     * @throws RemoteResourceException if there is any problem with the remote community repository
     */
    List<Community> findAll();

    /**
     * Looks for a community with the specified ID in the remote community repository.
     *
     * @param id ID of the community to get
     *
     * @return the community with the specified ID in the remote community repository
     * or Optional#empty() if none found
     *
     * @throws RemoteResourceException if there is any problem with the remote community repository
     */
    Optional<Community> findById(long id);

    /**
     * Looks for a community with the specified name in the remote community repository.
     *
     * @param name name of the community to get
     *
     * @return the community with the specified name in the remote community repository
     * or Optional#empty() if none found
     *
     * @throws RemoteResourceException if there is any problem with the remote community repository
     */
    Optional<Community> findByName(String name);

    /**
     * Saves the specified community in the remote community repository.
     * Use the returned community for further operations as the save operation
     * might have changed the community instance completely.
     *
     * @param community community to save
     *
     * @return the saved community
     *
     * @throws IllegalModificationException either if a community has invalid data or already exists
     * @throws RemoteResourceException if there is any problem with the remote community repository
     */
    Community save(Community community);

    /**
     * Updates the community with the specified ID in the remote community repository.
     * Use the returned community for further operations as the update operation
     * might have changed the community instance completely.
     *
     * @param community community to update
     *
     * @return the updated community
     *
     * @throws IllegalModificationException either if a community has invalid data or does not exist
     * @throws RemoteResourceException if there is any problem with the remote community repository
     */
    Community update(Community community);

    /**
     * Deletes the community with the specified ID in the remote community repository.
     *
     * @param id ID of the community to be deleted
     *
     * @throws IllegalModificationException if such a community does not exist
     * @throws RemoteResourceException if there is any problem with the remote community repository
     */
    void deleteById(long id);
}
