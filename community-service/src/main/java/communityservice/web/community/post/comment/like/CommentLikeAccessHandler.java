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

package communityservice.web.community.post.comment.like;

import communityservice.service.like.commentlike.CommentLike;
import communityservice.service.like.commentlike.CommentLikeService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class CommentLikeAccessHandler {
    private static final Logger logger = LogManager.getLogger(CommentLikeAccessHandler.class);
    private final CommentLikeService likeService;

    @Autowired
    public CommentLikeAccessHandler(CommentLikeService likeService) {
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
            Optional<CommentLike> like = likeService.findById(id);
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

    private boolean hasAccess(CommentLike like) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            return false;
        } else if (hasRole(authentication, "ADMIN")) {
            return true;
        } else {
            String login = like.getUser().getLogin();
            return authentication.getName().equals(login);
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
