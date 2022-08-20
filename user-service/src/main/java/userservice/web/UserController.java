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
import java.util.List;
import java.util.NoSuchElementException;

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
        return modelAssembler.toCollectionModel(users);
    }

    @GetMapping("/{id}")
    public EntityModel<User> getById(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new  NoSuchElementException("No user with id " + id));
        return modelAssembler.toModel(user);
    }

    @GetMapping(params = "email")
    public EntityModel<User> getByEmail(@RequestParam String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new  NoSuchElementException("No user with email " + email));
        return modelAssembler.toModel(user);
    }

    @GetMapping(params = "login")
    public EntityModel<User> getByLogin(@RequestParam String login) {
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new  NoSuchElementException("No user with login " + login));
        return modelAssembler.toModel(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<User> post(@RequestBody @Valid User user) {
        User saved = userService.save(user);
        return modelAssembler.toModel(saved);
    }


    @PatchMapping("/{id}")
    public EntityModel<User> patchById(@PathVariable Long id,
                                       @RequestBody User user) {
        user.setId(id);
        User updated = userService.update(user);
        return modelAssembler.toModel(updated);
    }

    @PatchMapping(value = "/{id}", params = "isBlocked")
    public void blockById(@PathVariable Long id,
                          @RequestParam Boolean isBlocked) {
        userService.setBlockedById(id, isBlocked);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        userService.deleteById(id);
    }
}
