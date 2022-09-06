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

package userservice.service.exception;

/**
 * Thrown to indicate that there was some kind of problem modifying the remote repository.
 */
public class IllegalModificationException extends RuntimeException {

    /**
     * Constructs a new IllegalModificationException with null as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a call to initCause().
     */
    public IllegalModificationException() {
        super();
    }

    /**
     * Constructs a new IllegalModificationException with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a call to initCause().
     *
     * @param message the detail message. The detail message is saved
     *                for later retrieval by the getMessage() method
     */
    public IllegalModificationException(String message) {
        super(message);
    }

    /**
     * Constructs a new IllegalModificationException with the specified detail message and cause.
     *
     * @param message the detail message. The detail message is saved
     *                for later retrieval by the getMessage() method
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public IllegalModificationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new IllegalModificationException with the specified cause and a detail message.
     *
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public IllegalModificationException(Throwable cause) {
        super(cause);
    }
}
