package models.security;

import com.feth.play.module.pa.user.AuthUser;
import models.User;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Represents an account a user may utilize to authenticate (e.g. a username/password combination)
 * Hashed passwords persisted here
 */
@Entity
public class LinkedAccount extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @ManyToOne
    public User user;

    /**
     * Hashed password is persisted here
     */
    public String providerUserId;

    /**
     * Provider used for authentication -- e.g. "password" or "facebook"
     */
    public String providerKey;

    public static final Finder<Long, LinkedAccount> find = new Finder<>(Long.class, LinkedAccount.class);

    public static LinkedAccount findByProviderKey(final User user, String key) {
        return find.where().eq("user", user).eq("providerKey", key).findUnique();
    }

    public static LinkedAccount create(final AuthUser authUser) {
        final LinkedAccount ret = new LinkedAccount();
        ret.update(authUser);
        return ret;
    }

    public void update(final AuthUser authUser) {
        this.providerKey = authUser.getProvider();
        this.providerUserId = authUser.getId();
    }

    public static LinkedAccount create(final LinkedAccount acc) {
        final LinkedAccount ret = new LinkedAccount();
        ret.providerKey = acc.providerKey;
        ret.providerUserId = acc.providerUserId;

        return ret;
    }
}