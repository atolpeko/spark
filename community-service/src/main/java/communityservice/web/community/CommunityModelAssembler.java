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
import communityservice.web.community.post.PostController;
import communityservice.web.community.user.UserController;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Converts a Community domain class into a RepresentationModel.
 */
@Component
public class CommunityModelAssembler
        implements RepresentationModelAssembler<Community, EntityModel<Community>> {

    @Override
    public EntityModel<Community> toModel(Community entity) {
        long id = entity.getId();
        return EntityModel.of(entity,
                linkTo(methodOn(UserController.class).getAll(id)).withRel("users"),
                linkTo(methodOn(PostController.class).getAll(id)).withRel("posts"),
                linkTo(methodOn(CommunityController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(CommunityController.class).getAll()).withRel("all"));
    }

    @Override
    public CollectionModel<EntityModel<Community>> toCollectionModel(Iterable<? extends Community> entities) {
        CollectionModel<EntityModel<Community>> collectionModel =
                RepresentationModelAssembler.super.toCollectionModel(entities);
        collectionModel.add(linkTo(methodOn(CommunityController.class).getAll()).withSelfRel());
        return collectionModel;
    }
}
