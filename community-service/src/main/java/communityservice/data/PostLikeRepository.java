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

import communityservice.service.like.postlike.PostLike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * A PostLikeRepository abstracts a collection of PostLike objects.
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * Retrieves all likes with the same post ID.
     *
     * @param postId ID of the post with likes to get
     *
     * @return all likes with the same post ID
     */
    @Query("FROM PostLike WHERE post.id = ?1")
    List<PostLike> findAllByPostId(long postId);

    // The default implementation does not work for an unknown reason
    @Override
    @Query("FROM PostLike WHERE id = ?1")
    Optional<PostLike> findById(Long id);

    // The default implementation does not work for an unknown reason
    @Override
    @Modifying
    @Query("DELETE FROM PostLike WHERE id = ?1")
    void deleteById(Long id);
}
