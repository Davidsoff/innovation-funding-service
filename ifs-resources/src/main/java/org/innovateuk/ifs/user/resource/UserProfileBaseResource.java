package org.innovateuk.ifs.user.resource;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Base class for user profile DTOs
 */
public abstract class UserProfileBaseResource {

    @NotEmpty(message = "{validation.standard.firstname.required}")
    @Pattern(regexp = "[\\p{L} -]*", message = "{validation.standard.firstname.required}")
    @Size.List({
            @Size(min = 2, message = "{validation.standard.firstname.length.min}"),
            @Size(max = 70, message = "{validation.standard.firstname.length.max}"),
    })
    private String firstName;

    @NotEmpty(message = "{validation.standard.lastname.required}")
    @Pattern(regexp = "[\\p{L} -]*", message = "{validation.standard.lastname.required}")
    @Size.List({
            @Size(min = 2, message = "{validation.standard.lastname.length.min}"),
            @Size(max = 70, message = "{validation.standard.lastname.length.max}"),
    })
    private String lastName;

    @NotEmpty(message = "{validation.standard.phonenumber.required}")
    @Size.List({
            @Size(min = 8, message = "{validation.standard.phonenumber.length.min}"),
            @Size(max = 20, message = "{validation.standard.phonenumber.length.max}")
    })
    @Pattern(regexp = "([0-9\\ +-])+", message = "{validation.standard.phonenumber.format}")
    private String phoneNumber;

    @NotNull(message = "{validation.standard.address.required}")
    @Valid
    private AddressResource address;

    private String email;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public AddressResource getAddress() {
        return address;
    }

    public void setAddress(AddressResource address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserProfileBaseResource that = (UserProfileBaseResource) o;

        return new EqualsBuilder()
                .append(firstName, that.firstName)
                .append(lastName, that.lastName)
                .append(phoneNumber, that.phoneNumber)
                .append(address, that.address)
                .append(email, that.email)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(firstName)
                .append(lastName)
                .append(phoneNumber)
                .append(address)
                .append(email)
                .toHashCode();
    }
}
