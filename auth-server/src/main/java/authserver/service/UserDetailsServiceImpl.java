/*
 * Copyright 2002-2021 the original author or authors.
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

package authserver.service;

import authserver.data.UserRepository;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final CircuitBreaker circuitBreaker;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository,
                                  CircuitBreaker circuitBreaker) {
        this.userRepository = userRepository;
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * Locates the user based on the username.
     *
     * @param username the username identifying the user whose data is required
     *
     * @return a fully populated user record
     *
     * @throws UsernameNotFoundException if the user could not be found
     * @throws IllegalStateException if the user database is not available
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Supplier<Optional<User>> findUser = () -> userRepository.findByUsername(username);
            UserDetails user = circuitBreaker.decorateSupplier(findUser).get().get();
            return user;
        } catch (NoSuchElementException e) {
            throw new UsernameNotFoundException("User not found: " + username, e);
        } catch (Exception e) {
            throw new IllegalStateException("User database unavailable", e);
        }
    }
}
