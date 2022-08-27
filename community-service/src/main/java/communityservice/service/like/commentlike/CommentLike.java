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

package communityservice.service.like.commentlike;

import com.fasterxml.jackson.annotation.JsonIgnore;

import communityservice.service.comment.Comment;
import communityservice.service.like.AbstractLike;
import communityservice.service.user.User;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Comment like domain class.
 */
@Entity
@DiscriminatorValue("COMMENT_LIKE")
public class CommentLike extends AbstractLike {

    @ManyToOne
    @NotNull(message = "Comment is mandatory")
    @JsonIgnore
    private Comment comment;

    /**
     * @return CommentLike builder
     */
    public static Builder builder() {
        return new CommentLike().new Builder();
    }

    /**
     * Returns a CommentLike builder with predefined fields copied from the specified CommentLike.
     *
     * @param other CommentLike to copy data from
     *
     * @return CommentLike builder
     */
    public static Builder builder(CommentLike other) {
        return new CommentLike(other).new Builder();
    }

    public CommentLike() {
    }

    public CommentLike(CommentLike other) {
        super(other);
        comment = other.comment;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        if (!super.equals(other)) {
            return false;
        }

        CommentLike that = (CommentLike) other;
        return Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), comment);
    }

    @Override
    public String toString() {
        // Not using comment field to avoid infinite recursion
        return getClass().getName() + "{" +
                "commentID=" + comment.getId() +
                '}';
    }

    /**
     * CommentLike object builder.
     */
    public class Builder {

        private Builder() {
        }

        public CommentLike build() {
            return CommentLike.this;
        }

        public Builder withId(long id) {
            CommentLike.super.setId(id);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            CommentLike.super.setCreatedAt(createdAt);
            return this;
        }

        public Builder withUser(User user) {
            CommentLike.super.setUser(user);
            return this;
        }

        public Builder withComment(Comment comment) {
            CommentLike.this.comment = comment;
            return this;
        }

        /**
         * Copies not null fields from the specified CommentLike.
         *
         * @param other CommentLike to copy data from
         *
         * @return this builder
         */
        public Builder copyNonNullFields(CommentLike other) {
            if (other.getId() != null) {
                CommentLike.super.setId(other.getId());
            }
            if (other.getCreatedAt() != null) {
                CommentLike.super.setCreatedAt(other.getCreatedAt());
            }
            if (other.getUser() != null) {
                CommentLike.super.setUser(other.getUser());
            }
            if (other.comment != null) {
                CommentLike.this.comment = other.comment;
            }

            return this;
        }
    }
}
