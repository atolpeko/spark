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

package communityservice.service.user;

import com.fasterxml.jackson.annotation.JsonIgnore;

import communityservice.service.comment.Comment;
import communityservice.service.community.Community;
import communityservice.service.like.AbstractLike;
import communityservice.service.post.Post;

import javax.persistence.CascadeType;
import javax.persistence.Entity;

import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * User domain class.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @NotBlank(message = "Login is mandatory")
    private String login;

    @ManyToMany(mappedBy = "users", cascade = CascadeType.MERGE)
    @JsonIgnore
    private Set<Community> communities;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<Post> posts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<Comment> comments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<AbstractLike> likes;

    /**
     * @return user builder
     */
    public static Builder builder() {
        return new User().new Builder();
    }

    public User() {
        communities = new TreeSet<>();
        posts = new TreeSet<>();
        comments = new TreeSet<>();
        likes = new TreeSet<>();
    }

    /**
     * Constructs a new User copying data from the passed one.
     *
     * @param other user to copy data from
     */
    public User(User other) {
        login = other.login;
        communities = new TreeSet<>(other.communities);
        posts = new TreeSet<>(other.posts);
        comments = new TreeSet<>(other.comments);
        likes = new TreeSet<>(other.likes);
    }

    /**
     * Adds the specified community to this user.
     * <p>
     * Synchronised for bidirectional mapping.
     *
     * @param community community to add
     */
    public void addCommunity(Community community) {
        communities.add(community);

        // Not using addUser() to avoid infinite recursion
        Set<User> users = community.getUsers();
        users.add(this);
        community.setUsers(users);
    }

    /**
     * Delete the specified community from this user.
     * <p>
     * Synchronised for bidirectional mapping.
     *
     * @param community community to be deleted
     */
    public void deleteCommunity(Community community) {
        communities.remove(community);

        // Not using deleteUser() to avoid infinite recursion
        Set<User> users = community.getUsers();
        users.remove(this);
        community.setUsers(users);
    }

    /**
     * Adds the specified post to this user.
     * <p>
     * Synchronised for bidirectional mapping.
     *
     * @param post post to add
     */
    public void addPost(Post post) {
        posts.add(post);
        post.setUser(this);
    }

    /**
     * Delete the specified post from this user.
     * <p>
     * Synchronised for bidirectional mapping.
     *
     * @param post post to be deleted
     */
    public void deletePost(Post post) {
        posts.remove(post);
        post.setUser(null);
    }

    /**
     * Adds the specified comment to this user.
     * <p>
     * Synchronised for bidirectional mapping.
     *
     * @param comment comment to add
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setUser(this);
    }

    /**
     * Delete the specified comment from this user.
     * <p>
     * Synchronised for bidirectional mapping.
     *
     * @param comment comment to be deleted
     */
    public void deleteComment(Comment comment) {
        comments.remove(comment);
        comment.setUser(null);
    }

    /**
     * Adds the specified like to this user.
     * <p>
     * Synchronised for bidirectional mapping.
     *
     * @param like like to add
     */
    public void addLike(AbstractLike like) {
        likes.add(like);
        like.setUser(this);
    }

    /**
     * Delete the specified like from this user.
     * <p>
     * Synchronised for bidirectional mapping.
     *
     * @param like like to be deleted
     */
    public void deleteLike(AbstractLike like) {
        likes.remove(like);
        like.setUser(null);
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Set<Community> getCommunities() {
        return new TreeSet<>(communities);
    }

    public void setCommunities(Set<Community> community) {
        this.communities = new TreeSet<>(community);
    }

    public Set<Post> getPosts() {
        return new TreeSet<>(posts);
    }

    public void setPosts(Set<Post> posts) {
        this.posts = new TreeSet<>(posts);
    }

    public Set<Comment> getComments() {
        return new TreeSet<>(comments);
    }

    public void setComments(Set<Comment> comments) {
        this.comments = new TreeSet<>(comments);
    }

    public Set<AbstractLike> getLikes() {
        return new TreeSet<>(likes);
    }

    public void setLikes(Set<AbstractLike> likes) {
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

        User user = (User) other;
        return Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        // Not using communities field to avoid infinite recursion
        return getClass().getName() + "{" +
                "login='" + login + '\'' +
                ", posts=" + posts +
                ", comments=" + comments +
                ", likes=" + likes +
                '}';
    }

    /**
     * User object builder
     */
    public class Builder {

        private Builder() {
        }

        public User build() {
            return User.this;
        }

        public Builder withLogin(String login) {
            User.this.login = login;
            return this;
        }

        public Builder withCommunities(Set<Community> communities) {
            User.this.communities = new TreeSet<>(communities);
            return this;
        }

        public Builder withPosts(Set<Post> posts) {
            User.this.posts = new TreeSet<>(posts);
            return this;
        }

        public Builder withComments(Set<Comment> comments) {
            User.this.comments = new TreeSet<>(comments);
            return this;
        }

        public Builder withLikes(Set<AbstractLike> likes) {
            User.this.likes = new TreeSet<>(likes);
            return this;
        }
    }
}
