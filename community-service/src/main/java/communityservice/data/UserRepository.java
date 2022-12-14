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

package communityservice.data;

import communityservice.service.user.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * A UserRepository abstracts a collection of User objects.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Retrieves all users with the same community ID.
     *
     * @param communityId ID of the community with users to get
     *
     * @return all users with the same community ID
     */
    @Query("SELECT users FROM Community WHERE id = ?1")
    List<User> findAllByCommunityId(long communityId);
}
