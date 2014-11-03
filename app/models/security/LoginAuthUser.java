package models.security;

import com.feth.play.module.pa.providers.password.DefaultUsernamePasswordAuthUser;

/**
 * Transient model represents the authenticated user identity (for logging in)
 */
public class LoginAuthUser extends DefaultUsernamePasswordAuthUser {

    private static final long serialVersionUID = 1L;

    /**
     * The session timeout in seconds
     * Defaults to two weeks
     */
    private final static long SESSION_TIMEOUT = 24 * 14 * 3600;
    private long expiration;

    /**
     * For logging the user in automatically
     *
     * @param email
     */
    public LoginAuthUser(final String email) {
        this(null, email);
    }

    public LoginAuthUser(final String clearPassword, final String email) {
        super(clearPassword, email);

        expiration = System.currentTimeMillis() + 1000 * SESSION_TIMEOUT;
    }

    @Override
    public long expires() {
        return expiration;
    }
}
