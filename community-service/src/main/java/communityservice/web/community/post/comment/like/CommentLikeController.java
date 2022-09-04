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

import communityservice.service.comment.CommentService;
import communityservice.service.community.CommunityService;
import communityservice.service.like.commentlike.CommentLike;
import communityservice.service.like.commentlike.CommentLikeService;
import communityservice.service.post.PostService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/communities/{communityId}/posts/{postId}/comments/{commentId}/likes",
        produces = "application/json")
@CrossOrigin(origins = "*")
public class CommentLikeController {
    private final CommunityService communityService;
    private final PostService postService;
    private final CommentService commentService;
    private final CommentLikeService commentLikeService;
    private final CommentLikeModelAssembler modelAssembler;

    @Autowired
    public CommentLikeController(CommunityService communityService,
                                 PostService postService,
                                 CommentService commentService,
                                 CommentLikeService commentLikeService,
                                 CommentLikeModelAssembler modelAssembler) {
        this.communityService = communityService;
        this.postService = postService;
        this.commentService = commentService;
        this.commentLikeService = commentLikeService;
        this.modelAssembler = modelAssembler;
    }

    @GetMapping
    public CollectionModel<EntityModel<CommentLike>> getAll(@PathVariable Long communityId,
                                                            @PathVariable Long postId,
                                                            @PathVariable Long commentId) {
        checkCommunityExistence(communityId);
        checkPostExistence(postId);
        List<CommentLike> likes = commentLikeService.findAllByCommentId(commentId);
        return modelAssembler.toCollectionModel(likes);
    }

    private void checkCommunityExistence(long id) {
        communityService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No community with ID " + id));
    }

    private void checkPostExistence(long id) {
        postService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No post with ID " + id));
    }

    @GetMapping("/{likeId}")
    public EntityModel<CommentLike> getById(@PathVariable Long communityId,
                                            @PathVariable Long postId,
                                            @PathVariable Long commentId,
                                            @PathVariable Long likeId) {
        checkCommunityExistence(communityId);
        checkPostExistence(postId);
        checkCommentExistence(commentId);
        CommentLike like = commentLikeService.findById(likeId)
                .orElseThrow(() -> new NoSuchElementException("No like with ID " + likeId));
        return modelAssembler.toModel(like);
    }

    private void checkCommentExistence(long id) {
        commentService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No comment with ID " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#like.user.login == authentication.name")
    public EntityModel<CommentLike> like(@PathVariable Long communityId,
                                         @PathVariable Long postId,
                                         @PathVariable Long commentId,
                                         @RequestBody CommentLike like) {
        checkCommunityExistence(communityId);
        checkPostExistence(postId);
        CommentLike saved = commentLikeService.save(like, commentId);
        return modelAssembler.toModel(saved);
    }

    @DeleteMapping("/{likeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@commentLikeAccessHandler.canDelete(#likeId)")
    public void dislike(@PathVariable Long communityId,
                        @PathVariable Long postId,
                        @PathVariable Long commentId,
                        @PathVariable Long likeId) {
        checkCommunityExistence(communityId);
        checkPostExistence(postId);
        checkCommentExistence(commentId);
        commentLikeService.deleteById(likeId);
    }
}
