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
import communityservice.service.community.Community;

import javax.persistence.Entity;

import javax.persistence.Id;
import javax.persistence.ManyToMany;
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

    @ManyToMany(mappedBy = "users")
    @JsonIgnore
    private Set<Community> communities;

    /**
     * @return user builder
     */
    public static Builder builder() {
        return new User().new Builder();
    }

    public User() {
    }

    public User(User other) {
        login = other.login;
        communities = other.communities;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        User user = (User) other;
        // Not using communities field to avoid infinite recursion
        return Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        // Not using communities field to avoid infinite recursion
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        // Not using communities field to avoid infinite recursion
        return getClass().getName() + "{" +
                "login='" + login + '\'' +
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
    }
}
