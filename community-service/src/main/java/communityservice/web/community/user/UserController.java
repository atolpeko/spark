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

package communityservice.web.community.user;

import communityservice.service.user.User;
import communityservice.service.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.List;

@RestController
@RequestMapping(value = "/communities/{communityId}/users", produces = "application/json")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;
    private final UserModelAssembler modelAssembler;

    @Autowired
    public UserController(UserService userService,
                          UserModelAssembler modelAssembler) {
        this.userService = userService;
        this.modelAssembler = modelAssembler;
    }

    @GetMapping
    public CollectionModel<EntityModel<User>> getAll(@PathVariable Long communityId) {
        List<User> users = userService.findAllByCommunityId(communityId);
        return modelAssembler.toCollectionModel(users);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<User> subscribe(@PathVariable Long communityId,
                                       @RequestBody @Valid User user) {
        User saved = userService.subscribeUser(communityId, user);
        return modelAssembler.toModel(saved);
    }

    @DeleteMapping(params = "login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable Long communityId,
                            @RequestParam String login) {
        userService.unsubscribeUser(communityId, login);
    }
}
