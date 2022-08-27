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

package communityservice.service.comment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import communityservice.service.like.commentlike.CommentLike;
import communityservice.service.user.User;
import communityservice.service.post.Post;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Comment domain class.
 */
@Entity
@Table(name = "comments")
public class Comment implements Comparable<Comment> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Message is mandatory")
    private String message;

    @Column(nullable = false)
    private LocalDate createdAt;

    @ManyToOne
    @NotNull(message = "Post is mandatory")
    @JsonIgnore
    private Post post;

    @ManyToOne
    @NotNull(message = "User is mandatory")
    @Valid
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.MERGE,
            fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnore
    private Set<CommentLike> likes;

    /**
     * @return comment builder
     */
    public static Builder builder() {
        return new Comment().new Builder();
    }

    /**
     * Returns a comment builder with predefined fields copied from the specified comment.
     *
     * @param other comment to copy data from
     *
     * @return comment builder
     */
    public static Builder builder(Comment other) {
        return new Comment(other).new Builder();
    }

    public Comment() {
        createdAt = LocalDate.now();
        likes = new TreeSet<>();
    }

    public Comment(Comment other) {
        id = other.id;
        message = other.message;
        createdAt = other.createdAt;
        post = other.post;
        user = other.user;
        likes = new TreeSet<>(other.likes);
    }

    @Override
    public int compareTo(Comment other) {
        if (createdAt.isBefore(other.createdAt)) {
            return -1;
        } else if (createdAt.isAfter(other.createdAt)) {
            return 1;
        } else {
            return 0;
        }
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

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post target) {
        this.post = target;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<CommentLike> getLikes() {
        return new TreeSet<>(likes);
    }

    public void setLikes(Set<CommentLike> likes) {
        this.likes = new TreeSet<>(likes);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Comment comment = (Comment) other;
        return Objects.equals(message, comment.message)
                && Objects.equals(createdAt, comment.createdAt)
                && Objects.equals(user, comment.user)
                && Objects.equals(post, comment.post);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, createdAt, user, post);
    }

    @Override
    public String toString() {
        // Not using post and user fields to avoid infinite recursion
        return getClass().getName() + "{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                ", postId=" + post.getId() +
                ", userLogin=" + user.getLogin() +
                ", likes=" + likes +
                '}';
    }

    /**
     * Comment object builder.
     */
    public class Builder {

        private Builder() {
        }

        public Comment build() {
            return Comment.this;
        }

        public Builder withId(long id) {
            Comment.this.id = id;
            return this;
        }

        public Builder withMessage(String message) {
            Comment.this.message = message;
            return this;
        }

        public Builder createdAt(LocalDate date) {
            Comment.this.createdAt = date;
            return this;
        }

        public Builder withUser(User user) {
            Comment.this.user = user;
            return this;
        }

        public Builder withPost(Post post) {
            Comment.this.post = post;
            return this;
        }

        public Builder withLikes(Set<CommentLike> likes) {
            Comment.this.likes = new TreeSet<>(likes);
            return this;
        }

        /**
         * Copies not null fields from the specified comment.
         *
         * @param other comment to copy data from
         *
         * @return this builder
         */
        public Builder copyNonNullFields(Comment other) {
            if (other.id != null) {
                Comment.this.id = other.id;
            }
            if (other.message != null) {
                Comment.this.message = other.message;
            }
            if (other.createdAt != null) {
                Comment.this.createdAt = other.createdAt;
            }
            if (other.user != null) {
                Comment.this.user = other.user;
            }
            if (other.post != null) {
                Comment.this.post = other.post;
            }
            if (other.likes != null) {
                Comment.this.likes = new TreeSet<>(other.likes);
            }

            return this;
        }
    }
}
