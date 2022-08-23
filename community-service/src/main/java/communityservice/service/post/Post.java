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

package communityservice.service.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import communityservice.service.comment.Comment;
import communityservice.service.community.Community;
import communityservice.service.like.PostLike;
import communityservice.service.user.User;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Post domain class.
 */
@Entity
@Table(name = "posts")
public class Post implements Comparable<Post> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Message is mandatory")
    private String message;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @ManyToOne
    @NotNull(message = "User is mandatory")
    @Valid
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @ManyToOne
    @NotNull(message = "Community is mandatory")
    @JsonIgnore
    private Community community;

    @OneToMany(mappedBy = "post", cascade = CascadeType.MERGE,
            fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnore
    private Set<PostLike> likes;

    @OneToMany(mappedBy = "post", cascade = CascadeType.MERGE,
            fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnore
    private Set<Comment> comments;

    /**
     * @return post builder
     */
    public static Builder builder() {
        return new Post().new Builder();
    }

    /**
     * Returns a post builder with predefined fields copied from the specified post.
     *
     * @param other post to copy data from
     *
     * @return post builder
     */
    public static Builder builder(Post other) {
        return new Post(other).new Builder();
    }

    public Post() {
        createdAt = LocalDateTime.now();
        likes = new TreeSet<>();
        comments = new TreeSet<>();
    }

    public Post(Post other) {
        id = other.id;
        message = other.message;
        createdAt = other.createdAt;
        user = other.user;
        community = other.community;
        likes = new TreeSet<>(other.likes);
        comments = new TreeSet<>(other.comments);
    }

    @Override
    public int compareTo(Post other) {
        if (createdAt.isBefore(other.createdAt)) {
            return -1;
        } else if (createdAt.isAfter(other.createdAt)) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Adds the specified like to this post.
     * <p>
     * Synchronised for bidirectional mapping.
     *
     * @param like like to add
     */
    public void addLike(PostLike like) {
        likes.add(like);
        like.setPost(this);
    }

    /**
     * Delete the specified like from this post.
     * <p>
     * Synchronised for bidirectional mapping.
     *
     * @param like like to be deleted
     */
    public void deleteLike(PostLike like) {
        likes.remove(like);
        like.setPost(null);
    }

    /**
     * Adds the specified comment to this post.
     * <p>
     * Synchronised for bidirectional mapping.
     *
     * @param comment comment to add
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    /**
     * Delete the specified comment from this post.
     * <p>
     * Synchronised for bidirectional mapping.
     *
     * @param comment comment to be deleted
     */
    public void deleteLike(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    public Set<PostLike> getLikes() {
        return new TreeSet<>(likes);
    }

    public void setLikes(Set<PostLike> likes) {
        this.likes = new TreeSet<>(likes);
    }

    public Set<Comment> getComments() {
        return new TreeSet<>(comments);
    }

    public void setComments(Set<Comment> comments) {
        this.comments = new TreeSet<>(comments);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Post post = (Post) other;
        // Not using community field to avoid infinite recursion
        return Objects.equals(message, post.message)
                && Objects.equals(createdAt, post.createdAt)
                && Objects.equals(user, post.user)
                && Objects.equals(likes, post.likes)
                && Objects.equals(comments, post.comments);
    }

    @Override
    public int hashCode() {
        // Not using community field to avoid infinite recursion
        return Objects.hash(message, createdAt, user, likes, comments);
    }

    @Override
    public String toString() {
        // Not using community field to avoid infinite recursion
        return getClass().getName() + "{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                ", user=" + user +
                ", communityID=" + community.getId() +
                ", likes=" + likes +
                ", comments=" + comments +
                '}';
    }

    /**
     * Post object builder.
     */
    public class Builder {

        private Builder() {
        }

        public Post build() {
            return Post.this;
        }

        public Builder withId(long id) {
            Post.this.id = id;
            return this;
        }

        public Builder withMessage(String message) {
            Post.this.message = message;
            return this;
        }

        public Builder createdAt(LocalDateTime date) {
            Post.this.createdAt = date;
            return this;
        }

        public Builder withUser(User user) {
            Post.this.user = user;
            return this;
        }

        public Builder withCommunity(Community community) {
            Post.this.community = community;
            return this;
        }

        public Builder withLikes(Set<PostLike> likes) {
            Post.this.likes = new TreeSet<>(likes);
            return this;
        }

        public Builder withComments(Set<Comment> comments) {
            Post.this.comments = new TreeSet<>(comments);
            return this;
        }

        /**
         * Copies not null fields from the specified post.
         *
         * @param other post to copy data from
         *
         * @return this builder
         */
        public Builder copyNonNullFields(Post other) {
            if (other.id != null) {
                Post.this.id = other.id;
            }
            if (other.message != null) {
                Post.this.message = other.message;
            }
            if (other.createdAt != null) {
                Post.this.createdAt = other.createdAt;
            }
            if (other.user != null) {
                Post.this.user = other.user;
            }
            if (other.community != null) {
                Post.this.community = other.community;
            }
            if (other.likes != null) {
                Post.this.likes = new TreeSet<>(other.likes);
            }
            if (other.comments != null) {
                Post.this.comments = new TreeSet<>(other.comments);
            }

            return this;
        }
    }
}
