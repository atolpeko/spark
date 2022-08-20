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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Objects;

/**
 * User's personal data.
 */
@Embeddable
public class PersonalData {

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Login is mandatory")
    private String login;

    @Column(nullable = false)
    @NotBlank(message = "Name is mandatory")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Phone is mandatory")
    private String phone;

    @Column(nullable = false)
    @NotNull(message = "Birthday is mandatory")
    private LocalDate birthday;

    /**
     * @return personal data builder
     */
    public static Builder builder() {
        return new PersonalData().new Builder();
    }

    /**
     * Returns a personal data builder with predefined fields copied from the specified personal data.
     *
     * @param other personal data to copy data from
     *
     * @return personal data builder
     */
    public static Builder builder(PersonalData other) {
        return new PersonalData(other).new Builder();
    }

    public PersonalData() {
    }

    /**
     * Constructs a new personal data copying data from the passed one.
     *
     * @param other personal data to copy data from
     */
    public PersonalData(PersonalData other) {
        login = other.login;
        name = other.name;
        phone = other.phone;
        birthday = other.birthday;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate dateOfBirth) {
        this.birthday = dateOfBirth;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        PersonalData that = (PersonalData) other;
        return Objects.equals(login, that.login)
                && Objects.equals(name, that.name)
                && Objects.equals(phone, that.phone)
                && Objects.equals(birthday, that.birthday);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, name, phone, birthday);
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", birthday=" + birthday +
                '}';
    }

    /**
     * User's personal data object builder.
     */
    public class Builder {

        private Builder() {
        }

        public PersonalData build() {
            return PersonalData.this;
        }

        public Builder withLogin(String login) {
            PersonalData.this.login = login;
            return this;
        }

        public Builder withName(String name) {
            PersonalData.this.name = name;
            return this;
        }

        public Builder withPhone(String phone) {
            PersonalData.this.phone = phone;
            return this;
        }

        public Builder withBirthday(LocalDate birthday) {
            PersonalData.this.birthday = birthday;
            return this;
        }

        /**
         * Copies not null fields from the specified personal data.
         *
         * @param other personal data to copy data from
         *
         * @return this builder
         */
        public Builder copyNonNullFields(PersonalData other) {
            if (other.login != null) {
                PersonalData.this.login = other.login;
            }
            if (other.name != null) {
                PersonalData.this.name = other.name;
            }
            if (other.phone != null) {
                PersonalData.this.phone = other.phone;
            }
            if (other.birthday != null) {
                PersonalData.this.birthday = other.birthday;
            }

            return this;
        }
    }
}
