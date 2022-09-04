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

package communityservice.web.community.post.like;

import communityservice.service.like.postlike.PostLike;
import communityservice.service.like.postlike.PostLikeService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostLikeAccessHandler {
    private static final Logger logger = LogManager.getLogger(PostLikeAccessHandler.class);
    private final PostLikeService likeService;

    @Autowired
    public PostLikeAccessHandler(PostLikeService likeService) {
        this.likeService = likeService;
    }

    /**
     * Decides whether the current user can delete the like with the specified ID.
     *
     * @param id ID of the like being deleted
     *
     * @return true if access is available, false otherwise
     */
    public boolean canDelete(long id) {
        try {
            Optional<PostLike> like = likeService.findById(id);
            if (like.isEmpty()) {
                return false;
            } else {
                return hasAccess(like.get());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    private boolean hasAccess(PostLike like) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            return false;
        } else {
            String login = like.getUser().getLogin();
            return authentication.getName().equals(login);
        }
    }
}
