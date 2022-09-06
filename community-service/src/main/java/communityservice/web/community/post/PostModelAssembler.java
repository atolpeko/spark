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

package communityservice.web.community.post;

import communityservice.service.post.Post;
import communityservice.web.community.CommunityController;
import communityservice.web.community.post.comment.CommentController;
import communityservice.web.community.post.like.PostLikeController;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.BasicLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Converts a Post domain class into a RepresentationModel.
 */
@Component
public class PostModelAssembler
        implements RepresentationModelAssembler<Post, EntityModel<Post>> {

    @Override
    public EntityModel<Post> toModel(Post entity) {
        Link userLink = BasicLinkBuilder
                .linkToCurrentMapping()
                .slash("/users?login=" + entity.getUser().getLogin())
                .withRel("user");
        long communityId = entity.getCommunity().getId();
        long postId = entity.getId();

        return EntityModel.of(entity, userLink,
                linkTo(methodOn(CommunityController.class).getById(communityId)).withRel("community"),
                linkTo(methodOn(PostLikeController.class).getAll(communityId, postId)).withRel("likes"),
                linkTo(methodOn(CommentController.class).getAll(communityId, postId)).withRel("comments"),
                linkTo(methodOn(PostController.class).getById(communityId, postId)).withSelfRel(),
                linkTo(methodOn(PostController.class).getAll(communityId)).withRel("all"));
    }

    @Override
    public CollectionModel<EntityModel<Post>> toCollectionModel(Iterable<? extends Post> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}
