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

package communityservice.service.community;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import communityservice.service.user.User;
import communityservice.service.post.Post;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Community domain class.
 */
@Entity
@Table(name = "communities")
public class Community implements Comparable<Community> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Name is mandatory")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Description is mandatory")
    private String description;

    @Column(nullable = false)
    @NotBlank(message = "Admin login is mandatory")
    private String adminLogin;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "community_users",
            joinColumns = @JoinColumn(name = "community_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "user_id", nullable = false))
    @JsonIgnore
    private Set<User> users;

    @OneToMany(mappedBy = "community", cascade = CascadeType.MERGE,
            orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Post> posts;

    /**
     * @return community builder
     */
    public static Builder builder() {
        return new Community().new Builder();
    }

    /**
     * Returns a community builder with predefined fields copied from the specified community.
     *
     * @param other community to copy data from
     *
     * @return community builder
     */
    public static Builder builder(Community other) {
        return new Community(other).new Builder();
    }

    public Community() {
        users = new HashSet<>();
        posts = new TreeSet<>();
        createdAt = LocalDateTime.now();
    }

    public Community(Community other) {
        id = other.id;
        name = other.name;
        description = other.description;
        adminLogin = other.adminLogin;
        createdAt = other.createdAt;
        users = new HashSet<>(other.users);
        posts = new HashSet<>(other.posts);
    }

    @Override
    public int compareTo(Community other) {
        return Integer.compare(other.users.size(), users.size());
    }

    /**
     * Adds the specified user to this community.
     *
     * @param user user to add
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * Deletes the specified user from this community.
     *
     * @param user user to be deleted
     */
    public void deleteUser(User user) {
        users.remove(user);
    }

    /**
     * Adds the specified post to this community.
     * <p>
     * Synchronized for bidirectional mapping.
     *
     * @param post post to add
     */
    public void addPost(Post post) {
        posts.add(post);
        post.setCommunity(this);
    }

    /**
     * Deletes a specified post from this community.
     * <p>
     * Synchronized for bidirectional mapping.
     *
     * @param post post to add
     */
    public void deletePost(Post post) {
        posts.remove(post);
        post.setCommunity(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdminLogin() {
        return adminLogin;
    }

    public void setAdminLogin(String adminLogin) {
        this.adminLogin = adminLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<User> getUsers() {
        return new HashSet<>(users);
    }

    public void setUsers(Set<User> users) {
        this.users = new HashSet<>(users);
    }

    public Set<Post> getPosts() {
        return new TreeSet<>(posts);
    }

    public void setPosts(Set<Post> posts) {
        this.posts = new TreeSet<>(posts);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Community community = (Community) other;
        return Objects.equals(name, community.name)
                && Objects.equals(description, community.description)
                && Objects.equals(adminLogin, community.adminLogin)
                && Objects.equals(createdAt, community.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, adminLogin, createdAt);
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", adminLogin='" + adminLogin + '\'' +
                ", createdAt=" + createdAt +
                ", users=" + users +
                ", posts=" + posts +
                '}';
    }

    /**
     * Community object builder.
     */
    public class Builder {

        private Builder() {
        }

        public Community build() {
            return Community.this;
        }

        public Builder withId(long id) {
            Community.this.id = id;
            return this;
        }

        public Builder withName(String name) {
            Community.this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            Community.this.description = description;
            return this;
        }

        public Builder withAdminLogin(String login) {
            Community.this.adminLogin = login;
            return this;
        }

        public Builder createdAt(LocalDateTime date) {
            Community.this.createdAt = date;
            return this;
        }

        public Builder withUsers(Set<User> users) {
            Community.this.users = new HashSet<>(users);
            return this;
        }

        public Builder withPosts(Set<Post> posts) {
            Community.this.posts = new TreeSet<>(posts);
            return this;
        }

        /**
         * Copies not null fields from the specified community.
         *
         * @param other community to copy data from
         *
         * @return this builder
         */
        public Builder copyNonNullFields(Community other) {
            if (other.id != null) {
                Community.this.id = other.id;
            }
            if (other.name != null) {
                Community.this.name = other.name;
            }
            if (other.description != null) {
                Community.this.description = other.description;
            }
            if (other.adminLogin != null) {
                Community.this.adminLogin = other.adminLogin;
            }
            if (other.createdAt != null) {
                Community.this.createdAt = other.createdAt;
            }
            if (other.users != null) {
                Community.this.users = new HashSet<>(other.users);
            }
            if (other.posts != null) {
                Community.this.posts = new TreeSet<>(other.posts);
            }

            return this;
        }
    }
}
