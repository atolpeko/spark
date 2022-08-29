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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

import javax.validation.Valid;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/communities", produces = "application/json")
@CrossOrigin(origins = "*")
public class CommunityController {
    private final CommunityService communityService;
    private final CommunityModelAssembler modelAssembler;

    @Autowired
    public CommunityController(CommunityService communityService,
                               CommunityModelAssembler modelAssembler) {
        this.communityService = communityService;
        this.modelAssembler = modelAssembler;
    }

    @GetMapping
    public CollectionModel<EntityModel<Community>> getAll() {
        List<Community> communities = communityService.findAll();
        return modelAssembler.toCollectionModel(communities);
    }

    @GetMapping("/{id}")
    public EntityModel<Community> getById(@PathVariable Long id) {
        Community community = communityService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No community with ID " + id));
        return modelAssembler.toModel(community);
    }

    @GetMapping(params = "name")
    public EntityModel<Community> getByName(@RequestParam String name) {
        Community community = communityService.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("No community with name " + name));
        return modelAssembler.toModel(community);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER') and #community.adminLogin == authentication.name" +
            " or hasAuthority('ADMIN')")
    public EntityModel<Community> post(@RequestBody @Valid Community community) {
        Community saved = communityService.save(community);
        return modelAssembler.toModel(saved);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@communityAccessHandler.canPatch(#id)")
    public EntityModel<Community> patchById(@PathVariable Long id,
                                            @RequestBody Community community) {
        community.setId(id);
        Community updated = communityService.update(community);
        return modelAssembler.toModel(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@communityAccessHandler.canDelete(#id)")
    public void deleteById(@PathVariable Long id) {
        communityService.deleteById(id);
    }
}
