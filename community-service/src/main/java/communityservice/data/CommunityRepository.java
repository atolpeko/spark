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

import communityservice.service.community.Community;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * A CommunityRepository abstracts a collection of Community objects.
 */
@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {

    /**
     * Retrieves a community by its name.
     *
     * @param name name of the community to get
     *
     * @return the community with the given name or Optional#empty() if none found.
     */
    @Query("FROM Community WHERE name=?1")
    Optional<Community> findByName(String name);
}
