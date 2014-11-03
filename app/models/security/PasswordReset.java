package models.security;

import play.data.validation.Constraints;
import play.i18n.Messages;

/**
 * Domain model for PasswordReset form input
 */
public class PasswordReset {

    public PasswordReset() {
    }

    public PasswordReset(final String token) {
        this.token = token;
    }

    public String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Constraints.Required
    public String password;

    @Constraints.Required
    public String repeatPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    public String validate() {
        if (password == null || !password.equals(repeatPassword)) {
            return Messages
                    .get("playauthenticate.change_password.error.passwords_not_same");
        }
        return null;
    }
}
