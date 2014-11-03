package models.security;

import models.User;
import play.data.validation.Constraints;
import play.i18n.Messages;

/**
 * Form class for signing up
 */
public class UserSignup extends UserLogin {

    public Long id;

    public String repeatPassword;

    @Constraints.Required
    public String firstName;

    @Constraints.Required
    public String lastName;

    public String gender;

    public String currentCity;

    public String hometown;

    public String educationalLevel;

    public String educationalField;

    public String employmentField;

    public String bio;

    public String interests;

    /**
     * Validates the signup form
     */
    public String validate() {
        if (id == null) { // if registering a new user
            if (password == null || !password.equals(repeatPassword)) { //ensure password is not null and is equal to the repeat password
                //TODO -- these messages are configured in conf/messages.en
                return Messages
                        .get("playauthenticate.password.signup.error.passwords_not_same");
            }
            if (!User.find.where().eq("email", email).findList().isEmpty()) { //ensure email is unique
                return Messages
                        .get("raa.signup.error.email_not_unique");
            }
        }

        return null;
    }
}
