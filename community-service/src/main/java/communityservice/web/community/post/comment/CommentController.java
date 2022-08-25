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

import communityservice.service.comment.CommentService;
import communityservice.service.community.CommunityService;
import communityservice.service.comment.Comment;
import communityservice.service.post.PostService;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/communities/{communityId}/posts/{postId}/comments",
        produces = "application/json")
@CrossOrigin(origins = "*")
public class CommentController {
    private final CommunityService communityService;
    private final PostService postService;
    private final CommentService commentService;
    private final CommentModelAssembler modelAssembler;

    @Autowired
    public CommentController(CommunityService communityService,
                             PostService postService,
                             CommentService commentService,
                             CommentModelAssembler modelAssembler) {
        this.communityService = communityService;
        this.postService = postService;
        this.commentService = commentService;
        this.modelAssembler = modelAssembler;
    }

    @GetMapping
    public CollectionModel<EntityModel<Comment>> getAll(@PathVariable Long communityId,
                                                        @PathVariable Long postId) {
        checkCommunityExistence(communityId);
        List<Comment> comments = commentService.findAllByPostId(postId);
        return modelAssembler.toCollectionModel(comments);
    }

    private void checkCommunityExistence(long communityId) {
        communityService.findById(communityId)
                .orElseThrow(() -> new NoSuchElementException("No community with ID " + communityId));
    }

    @GetMapping("/{commentId}")
    public EntityModel<Comment> getById(@PathVariable Long communityId,
                                        @PathVariable Long postId,
                                        @PathVariable Long commentId) {
        checkCommunityExistence(communityId);
        checkPostExistence(postId);
        Comment comment = commentService.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("No comment with ID " + commentId));
        return modelAssembler.toModel(comment);
    }

    private void checkPostExistence(long postId) {
        postService.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post with ID " + postId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<Comment> post(@PathVariable Long communityId,
                                     @PathVariable Long postId,
                                     @RequestBody Comment comment) {
        checkCommunityExistence(communityId);
        Comment saved = commentService.save(comment, postId);
        return modelAssembler.toModel(saved);
    }

    @PatchMapping("/{commentId}")
    public EntityModel<Comment> patch(@PathVariable Long communityId,
                                      @PathVariable Long postId,
                                      @PathVariable Long commentId,
                                      @RequestBody Comment comment) {
        checkCommunityExistence(communityId);
        checkPostExistence(postId);
        comment.setId(commentId);
        Comment updated = commentService.update(comment);
        return modelAssembler.toModel(updated);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long communityId,
                           @PathVariable Long postId,
                           @PathVariable Long commentId) {
        checkCommunityExistence(communityId);
        checkPostExistence(postId);
        commentService.deleteById(commentId);
    }
}
