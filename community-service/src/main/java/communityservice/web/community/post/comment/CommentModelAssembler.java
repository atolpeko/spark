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

package communityservice.web.community.post.comment;

import communityservice.service.comment.Comment;
import communityservice.web.community.post.PostController;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.BasicLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Converts a Comment domain class into a RepresentationModel.
 */
@Component
public class CommentModelAssembler implements RepresentationModelAssembler<Comment, EntityModel<Comment>> {

    @Override
    public EntityModel<Comment> toModel(Comment entity) {
        Link userLink = BasicLinkBuilder
                .linkToCurrentMapping()
                .slash("/users?login=" + entity.getUser().getLogin())
                .withRel("user");

        long communityId = entity.getPost().getCommunity().getId();
        long postId = entity.getPost().getId();
        long commentId = entity.getId();
        return EntityModel.of(entity, userLink,
                linkTo(methodOn(PostController.class).getById(communityId, postId)).withRel("post"),
                linkTo(methodOn(CommentController.class).getById(communityId, postId, commentId)).withSelfRel(),
                linkTo(methodOn(CommentController.class).getAll(communityId, postId)).withRel("all"));
    }

    @Override
    public CollectionModel<EntityModel<Comment>> toCollectionModel(Iterable<? extends Comment> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}
