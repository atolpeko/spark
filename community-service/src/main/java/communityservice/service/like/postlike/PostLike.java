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

package communityservice.service.like.postlike;

import com.fasterxml.jackson.annotation.JsonIgnore;

import communityservice.service.like.AbstractLike;
import communityservice.service.post.Post;
import communityservice.service.user.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Post like domain class.
 */
@Entity
@DiscriminatorValue("POST_LIKE")
public class PostLike extends AbstractLike {

    @ManyToOne
    @NotNull(message = "Post is mandatory")
    @JsonIgnore
    private Post post;

    /**
     * @return PostLike builder
     */
    public static Builder builder() {
        return new PostLike().new Builder();
    }

    /**
     * Returns a PostLike builder with predefined fields copied from the specified PostLike.
     *
     * @param other PostLike to copy data from
     *
     * @return PostLike builder
     */
    public static Builder builder(PostLike other) {
        return new PostLike(other).new Builder();
    }

    public PostLike() {
    }

    public PostLike(PostLike other) {
        super(other);
        post = other.post;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        // Not using post field to avoid infinite recursion
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        // Not using post field to avoid infinite recursion
        return super.hashCode();
    }

    @Override
    public String toString() {
        // Not using post field to avoid infinite recursion
        return getClass().getName() + "{" +
                "postID=" + post.getId() +
                '}';
    }

    /**
     * PostLike object builder.
     */
    public class Builder {

        private Builder() {
        }

        public PostLike build() {
            return PostLike.this;
        }

        public Builder withId(long id) {
            PostLike.super.setId(id);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            PostLike.super.setCreatedAt(createdAt);
            return this;
        }

        public Builder withUser(User user) {
            PostLike.super.setUser(user);
            return this;
        }

        public Builder withPost(Post post) {
            PostLike.this.post = post;
            return this;
        }

        /**
         * Copies not null fields from the specified PostLike.
         *
         * @param other PostLike to copy data from
         *
         * @return this builder
         */
        public Builder copyNonNullFields(PostLike other) {
            if (other.getId() != null) {
                PostLike.super.setId(other.getId());
            }
            if (other.getCreatedAt() != null) {
                PostLike.super.setCreatedAt(other.getCreatedAt());
            }
            if (other.getUser() != null) {
                PostLike.super.setUser(other.getUser());
            }
            if (other.post != null) {
                PostLike.this.post = other.post;
            }

            return this;
        }
    }
}
