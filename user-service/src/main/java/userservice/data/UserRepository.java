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

package userservice.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import userservice.service.User;

import java.util.Optional;

/**
 * A UserRepository abstracts a collection of User objects.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a user by its email.
     *
     * @param email email of the user to get
     *
     * @return the user with the given email or Optional#empty() if none found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Retrieves a user by its login.
     *
     * @param login login of the user to get
     *
     * @return the user with the given login or Optional#empty() if none found.
     */
    @Query("FROM User WHERE personalData.login=?1")
    Optional<User> findByLogin(String login);
}
