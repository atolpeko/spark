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

package communityservice.web.community;

import communityservice.service.community.Community;
import communityservice.service.community.CommunityService;

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
public class CommunityAccessHandler {
    private static final Logger logger = LogManager.getLogger(CommunityAccessHandler.class);
    private final CommunityService communityService;

    @Autowired
    public CommunityAccessHandler(CommunityService communityService) {
        this.communityService = communityService;
    }

    /**
     * Decides whether the current user can patch the community with the specified ID.
     *
     * @param id ID of the community being patched
     *
     * @return true if access is available, false otherwise
     */
    public boolean canPatch(long id) {
        try {
            Optional<Community> community = communityService.findById(id);
            if (community.isEmpty()) {
                return false;
            } else {
               return hasAccess(community.get());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    private boolean hasAccess(Community community) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            return false;
        } else if (hasRole(authentication, "ADMIN")) {
            return true;
        } else {
            String login = community.getAdminLogin();
            return authentication.getName().equals(login);
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    /**
     * Decides whether the current user can delete the community with the specified ID.
     *
     * @param resultId ID of the community being deleted
     *
     * @return true if access is available, false otherwise
     */
    public boolean canDelete(long resultId) {
        return canPatch(resultId);
    }
}
