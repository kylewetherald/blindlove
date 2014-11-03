package models.security;

import services.authentication.AuthenticationService;

/**
 * Form class for logging in
 */
public class UserLogin extends AuthenticationService.MyIdentity implements com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.UsernamePassword {

    public String password;

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
