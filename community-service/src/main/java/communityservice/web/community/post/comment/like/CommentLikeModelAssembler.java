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
import communityservice.service.like.postlike.PostLike;
import communityservice.web.community.post.PostController;
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
 * Converts a Like domain class into a RepresentationModel.
 */
@Component
public class CommentLikeModelAssembler
        implements RepresentationModelAssembler<CommentLike, EntityModel<CommentLike>> {

    @Override
    public EntityModel<CommentLike> toModel(CommentLike entity) {
        Link userLink = BasicLinkBuilder
                .linkToCurrentMapping()
                .slash("/users?login=" + entity.getUser().getLogin())
                .withRel("user");

        long communityId = entity.getComment().getId();
        long commentId = entity.getComment().getId();
        long likeId = entity.getId();
        return EntityModel.of(entity, userLink,
                linkTo(methodOn(PostController.class).getById(communityId, commentId)).withRel("comment"),
                linkTo(methodOn(PostLikeController.class).getById(communityId, commentId, likeId)).withSelfRel(),
                linkTo(methodOn(PostLikeController.class).getAll(communityId, commentId)).withRel("all"));
    }

    @Override
    public CollectionModel<EntityModel<CommentLike>> toCollectionModel(Iterable<? extends CommentLike> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}
