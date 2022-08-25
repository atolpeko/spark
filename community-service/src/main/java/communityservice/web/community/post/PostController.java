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

import communityservice.service.community.CommunityService;
import communityservice.service.post.Post;

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
@RequestMapping(value = "/communities/{communityId}/posts", produces = "application/json")
@CrossOrigin(origins = "*")
public class PostController {
    private final PostService postService;
    private final CommunityService communityService;
    private final PostModelAssembler modelAssembler;

    @Autowired
    public PostController(PostService postService,
                          CommunityService communityService,
                          PostModelAssembler modelAssembler) {
        this.postService = postService;
        this.communityService = communityService;
        this.modelAssembler = modelAssembler;
    }

    @GetMapping
    public CollectionModel<EntityModel<Post>> getAll(@PathVariable long communityId) {
        List<Post> posts = postService.findAllByCommunityId(communityId);
        return modelAssembler.toCollectionModel(posts);
    }

    @GetMapping("/{postId}")
    public EntityModel<Post> getById(@PathVariable Long communityId,
                                     @PathVariable Long postId) {
        checkCommunityExistence(communityId);
        Post post = postService.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post with ID " + postId));
        return modelAssembler.toModel(post);
    }

    private void checkCommunityExistence(long communityId) {
        communityService.findById(communityId)
                .orElseThrow(() -> new NoSuchElementException("No community with ID " + communityId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<Post> post(@PathVariable Long communityId,
                                  @RequestBody Post post) {
        Post saved = postService.save(post, communityId);
        return modelAssembler.toModel(saved);
    }

    @PatchMapping("{postId}")
    public EntityModel<Post> patchById(@PathVariable Long communityId,
                                       @PathVariable Long postId,
                                       @RequestBody Post post) {
        checkCommunityExistence(communityId);
        post.setId(postId);
        Post updated = postService.update(post);
        return modelAssembler.toModel(updated);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long communityId,
                           @PathVariable Long postId) {
        checkCommunityExistence(communityId);
        postService.deleteById(postId);
    }
}
