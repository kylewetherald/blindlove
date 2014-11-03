package models.security;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;

/**
 * Transient model represents a user authentication identity (for signing up and password reset)
 */
public class AuthUser extends UsernamePasswordAuthUser {

    private static final long serialVersionUID = 1L;

    public UserSignup signup;

    public AuthUser(final UserSignup signup) {
       super(signup.password, signup.email);
       this.signup = signup;
    }

    /**
     * Used for password reset only - do not use this to signup a user!
     * @param password
     */
    public AuthUser(final String password) {
        super(password, null);

    }

}