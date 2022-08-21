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

package userservice.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import userservice.service.User;
import userservice.service.UserService;

import java.util.Collection;
import java.util.Optional;

/**
 * Decides if a user has access to users.
 */
@Component
public class UserAccessHandler {
    private final UserService userService;

    @Autowired
    public UserAccessHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * Decides whether the current user can patch the user with the specified ID.
     *
     * @param id of the user being patched
     *
     * @return true if patch is available, false otherwise
     */
    public boolean canPatch(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated()) {
            return false;
        }

        return isAdmin(auth) || isOwner(id, auth);
    }

    private boolean isAdmin(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
    }

    private boolean isOwner(long ownerId, Authentication authentication) {
        Optional<User> user = userService.findById(ownerId);
        if (user.isEmpty()) {
            return false;
        }

        String ownerEmail = user.get().getEmail();
        return authentication.getName().equals(ownerEmail);
    }

    /**
     * Decides whether the current user can delete the user with the specified ID.
     *
     * @param id of the user being deleted
     *
     * @return true if delete is available, false otherwise
     */
    public boolean canDelete(Long id) {
        return canPatch(id);
    }
}
