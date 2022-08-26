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

package communityservice.web.community.post.like;

import communityservice.service.community.CommunityService;
import communityservice.service.like.postlike.PostLikeService;
import communityservice.service.like.postlike.PostLike;
import communityservice.service.post.PostService;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/communities/{communityId}/posts/{postId}/likes",
        produces = "application/json")
@CrossOrigin(origins = "*")
public class PostLikeController {
    private final CommunityService communityService;
    private final PostService postService;
    private final PostLikeService likeService;
    private final PostLikeModelAssembler modelAssembler;

    @Autowired
    public PostLikeController(CommunityService communityService,
                              PostService postService,
                              PostLikeService likeService,
                              PostLikeModelAssembler modelAssembler) {
        this.communityService = communityService;
        this.postService = postService;
        this.likeService = likeService;
        this.modelAssembler = modelAssembler;
    }

    @GetMapping
    public CollectionModel<EntityModel<PostLike>> getAll(@PathVariable Long communityId,
                                                         @PathVariable Long postId) {
        checkCommunityExistence(communityId);
        List<PostLike> likes = likeService.findAllByPostId(postId);
        return modelAssembler.toCollectionModel(likes);
    }

    private void checkCommunityExistence(long id) {
        communityService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No community with ID " + id));
    }

    @GetMapping("/{likeId}")
    public EntityModel<PostLike> getById(@PathVariable Long communityId,
                                         @PathVariable Long postId,
                                         @PathVariable Long likeId) {
        checkCommunityExistence(communityId);
        checkPostExistence(postId);
        PostLike like = likeService.findById(likeId)
                .orElseThrow(() -> new NoSuchElementException("No like with ID " + likeId));
        return modelAssembler.toModel(like);
    }

    private void checkPostExistence(long id) {
        postService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No post with ID " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<PostLike> like(@PathVariable Long communityId,
                                      @PathVariable Long postId,
                                      @RequestBody PostLike like) {
        checkCommunityExistence(communityId);
        PostLike saved = likeService.save(like, postId);
        return modelAssembler.toModel(saved);
    }

    @DeleteMapping("/{likeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void dislike(@PathVariable Long communityId,
                        @PathVariable Long postId,
                        @PathVariable Long likeId) {
        checkCommunityExistence(communityId);
        checkPostExistence(postId);
        likeService.deleteById(likeId);
    }
}
