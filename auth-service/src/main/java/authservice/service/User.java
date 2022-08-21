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

package authservice.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * User domain class.
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    /**
     * An enumeration denoting user role.
     */
    public enum Role { USER, ADMIN }

    @Id
    private String login;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10) default 'USER'")
    private Role role;

    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked;

    /**
     * @return user builder
     */
    public static Builder builder() {
        return new User().new Builder();
    }

    /**
     * Returns a user builder with predefined fields copied from the specified user.
     *
     * @param other user to copy data from
     *
     * @return user builder
     */
    public static Builder builder(User other) {
        return new User(other).new Builder();
    }

    public User() {
    }

    public User(User other) {
        login = other.login;
        password = other.password;
        isBlocked = other.isBlocked;
        role = other.role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        GrantedAuthority authority = new SimpleGrantedAuthority(role.toString());
        return List.of(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return login;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !isBlocked;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isBlocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !isBlocked;
    }

    @Override
    public boolean isEnabled() {
        return !isBlocked;
    }

    public void setLogin(String username) {
        this.login = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getBlocked() {
        return isBlocked;
    }

    public void setBlocked(Boolean blocked) {
        isBlocked = blocked;
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
        return Objects.equals(login, user.login)
                && Objects.equals(password, user.password)
                && Objects.equals(isBlocked, user.isBlocked)
                && role == user.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password, role, isBlocked);
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", isBlocked=" + isBlocked +
                '}';
    }

    /**
     * User object builder.
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

        public Builder withPassword(String password) {
            User.this.password = password;
            return this;
        }

        public Builder withRole(Role role) {
            User.this.role = role;
            return this;
        }

        public Builder isBlocked(Boolean isBlocked) {
            User.this.isBlocked = isBlocked;
            return this;
        }
    }
}
