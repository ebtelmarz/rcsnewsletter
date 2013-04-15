package com.rcs.newsletter.core.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author Ariel Parra <ariel@rotterdam-cs.com>
 */
@Entity
@Table(name = "newsletter_subscriptor")
public class NewsletterSubscriptor extends NewsletterEntity {

    public static final String EMAIL = "email";

    private static final long serialVersionUID = 1L;

    private String firstName;

    private String lastName;

    @NotBlank
    //@Pattern(regexp = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]([_A-Za-z0-9-]+)+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$", flags={Flag.CASE_INSENSITIVE})
    @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
