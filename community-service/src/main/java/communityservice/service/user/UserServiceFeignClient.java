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

import feign.FeignException.FeignClientException;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Provides access to user microservice.
 */
@FeignClient(name = "user-service", url = "${feign.user-service-url}")
public interface UserServiceFeignClient {

    /**
     * Looks for a user with the specified login.
     *
     * @param login login of the user to get
     *
     * @return user with the specified login
     *
     * @throws FeignClientException if there is any problem with feign client
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/users?login={login}",
            consumes = "application/json")
    User findUserByLogin(@PathVariable String login);
}
