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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import userservice.service.User;
import userservice.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users", produces = "application/json")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;
    private final UserModelAssembler modelAssembler;

    @Autowired
    public UserController(UserService userService, UserModelAssembler modelAssembler) {
        this.userService = userService;
        this.modelAssembler = modelAssembler;
    }

    @GetMapping
    public CollectionModel<EntityModel<User>> getAll() {
        List<User> users = userService.findAll();
        users.forEach(this::filter);
        return modelAssembler.toCollectionModel(users);
    }

    private void filter(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!hasRole(authentication, "ADMIN") && !userIsOwner(authentication, user)) {
            user.setPersonalData(null);
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    private boolean userIsOwner(Authentication authentication, User user) {
        String login = user.getLogin();
        return authentication.getName().equals(login);
    }

    @GetMapping(params = "login")
    public EntityModel<User> getByLogin(@RequestParam String login) {
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new  NoSuchElementException("No user with login " + login));
        filter(user);
        return modelAssembler.toModel(user);
    }

    @GetMapping(params = "email")
    public EntityModel<User> getByEmail(@RequestParam String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new  NoSuchElementException("No user with email " + email));
        filter(user);
        return modelAssembler.toModel(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<User> post(@RequestBody @Valid User user) {
        User saved = userService.save(user);
        return modelAssembler.toModel(saved);
    }

    @PatchMapping("/{login}")
    @PreAuthorize("hasAuthority('USER') and #login == authentication.name or hasAuthority('ADMIN')")
    public EntityModel<User> patchByLogin(@PathVariable String login,
                                          @RequestBody User user) {
        user.setLogin(login);
        User updated = userService.update(user);
        return modelAssembler.toModel(updated);
    }

    @PatchMapping(value = "/{login}", params = "isBlocked")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void blockByLogin(@PathVariable String login,
                             @RequestParam Boolean isBlocked) {
        userService.setBlockedByLogin(login, isBlocked);
    }

    @DeleteMapping("{login}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('USER') and #login == authentication.name or hasAuthority('ADMIN')")
    public void deleteByLogin(@PathVariable String login) {
        userService.deleteByLogin(login);
    }
}
