package org.innovateuk.ifs.registration.form;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.innovateuk.ifs.commons.validation.ValidationConstants;
import org.innovateuk.ifs.controller.BaseBindingResultTarget;
import org.innovateuk.ifs.user.resource.Disability;
import org.innovateuk.ifs.user.resource.Gender;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * This object is used for the account registration form. When the form is submitted the data is
 * injected into a RegistrationForm instance, so it is easy to use and you don't need to
 * read all the request attributes to get to the form data.
 */

public class RegistrationForm extends BaseBindingResultTarget {


    @Email(regexp = ValidationConstants.EMAIL_DISALLOW_INVALID_CHARACTERS_REGEX, message = "{validation.standard.email.format}")
    @NotEmpty(message = "{validation.standard.email.required}")
    @Size(max = 256, message = "{validation.standard.email.length.max}")
    private String email;

    @NotEmpty(message = "{validation.standard.password.required}")
    @Size.List ({
        @Size(min=8, message="{validation.standard.password.length.min}"),
    })
    private String password;

    private String title;

    @NotEmpty(message = "{validation.standard.firstname.required}")
    @Pattern(regexp = "[\\p{L} \\-']*", message = "{validation.standard.firstname.invalid}")
    @Size.List ({
        @Size(min=2, message="{validation.standard.firstname.length.min}"),
        @Size(max=70, message="{validation.standard.firstname.length.max}"),
    })
    private String firstName;

    @NotEmpty(message = "{validation.standard.lastname.required}")
    @Pattern(regexp = "[\\p{L} \\-']*", message = "{validation.standard.lastname.invalid}")
    @Size.List ({
        @Size(min=2, message="{validation.standard.lastname.length.min}"),
        @Size(max=70, message="{validation.standard.lastname.length.max}"),
    })
    private String lastName;

    @NotNull(message = "{validation.standard.gender.selectionrequired}")
    private String gender = Gender.NOT_STATED.name();

    @NotNull(message = "{validation.standard.ethnicity.selectionrequired}")
    private String ethnicity = "7";

    @NotNull(message = "{validation.standard.disability.selectionrequired}")
    private String disability = Disability.NOT_STATED.name();

    @NotEmpty(message = "{validation.standard.phonenumber.required}")
    @Size.List ({
        @Size(min=8, message="{validation.standard.phonenumber.length.min}"),
        @Size(max=20, message="{validation.standard.phonenumber.length.max}")
    })
    @Pattern(regexp = "([0-9\\ +-])+",  message= "{validation.standard.phonenumber.format}")
    private String phoneNumber;

    @NotBlank(message = "{validation.account.termsandconditions.required}")
    private String termsAndConditions;

    private Boolean allowMarketingEmails;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RegistrationForm withEmail(String email) {
        this.email = email;
        return this;
    }

    public String getTitle() {
        return title;
    }

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

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public Boolean getAllowMarketingEmails() {
        return allowMarketingEmails;
    }

    public void setAllowMarketingEmails(Boolean allowMarketingEmails) {
        this.allowMarketingEmails = allowMarketingEmails;
    }

    public String getGender() {
        return gender;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public String getDisability() {
        return disability;
    }
}
