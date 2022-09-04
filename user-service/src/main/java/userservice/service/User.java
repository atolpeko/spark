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

package userservice.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * User domain class
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @NotBlank(message = "Login is mandatory")
    private String login;

    @Column(nullable = false)
    @NotBlank(message = "Password is mandatory")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked;

    @Embedded
    @JsonUnwrapped
    @Valid
    private PersonalData personalData;

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

    /**
     * Constructs a new enabled user.
     */
    public User() {
        isBlocked = false;
    }

    /**
     * Constructs a new User copying data from the passed one.
     *
     * @param other user to copy data from
     */
    public User(User other) {
        login = other.login;
        password = other.password;
        isBlocked = other.isBlocked;
        personalData = other.personalData;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getBlocked() {
        return isBlocked;
    }

    public void setBlocked(Boolean blocked) {
        isBlocked = blocked;
    }

    public PersonalData getPersonalData() {
        return personalData;
    }

    public void setPersonalData(PersonalData personalData) {
        this.personalData = personalData;
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
                && Objects.equals(personalData, user.personalData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password, isBlocked, personalData);
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", isBlocked=" + isBlocked +
                ", personalData=" + personalData +
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

        public Builder isBlocked(Boolean isBlocked) {
            User.this.isBlocked = isBlocked;
            return this;
        }

        public Builder withPersonalData(PersonalData personalData) {
            User.this.personalData = personalData;
            return this;
        }

        /**
         * Copies not null fields from the specified user.
         *
         * @param other user to copy data from
         *
         * @return this builder
         */
        public Builder copyNonNullFields(User other) {
            if (other.login != null) {
                User.this.login = other.login;
            }
            if (other.password != null) {
                User.this.password = other.password;
            }
            if (other.isBlocked != null) {
                User.this.isBlocked = other.isBlocked;
            }
            if (other.personalData != null) {
                User.this.personalData = PersonalData.builder(User.this.personalData)
                        .copyNonNullFields(other.personalData)
                        .build();
            }

            return this;
        }
    }
}
