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

package communityservice.service.like;

import com.fasterxml.jackson.annotation.JsonProperty;

import communityservice.service.user.User;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Like base class.
 */
@Entity
@Table(name = "likes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class AbstractLike implements Comparable<AbstractLike> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @OneToOne
    @NotNull(message = "User is mandatory")
    @Valid
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    public AbstractLike() {
        createdAt = LocalDateTime.now();
    }

    public AbstractLike(AbstractLike other) {
        id = other.id;
        createdAt = other.createdAt;
        user = other.user;
    }

    @Override
    public int compareTo(AbstractLike other) {
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        AbstractLike that = (AbstractLike) other;
        return Objects.equals(createdAt, that.createdAt)
                && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, user);
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", user=" + user +
                '}';
    }
}
