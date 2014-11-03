package services.usermanagement;

import models.User;
import play.Application;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.service.UserServicePlugin;

/**
 * Service responsible for managing user persistence
 */
public class UserManagementService extends UserServicePlugin {

    public UserManagementService(final Application app) {
        super(app);
    }

    /**
     * Saves auth provider/id combination to a local user
     * @param authUser
     * @return The local identifying object or null if the user existed
     */
    @Override
    public Object save(final AuthUser authUser) {
        final boolean isLinked = User.existsByAuthUserIdentity(authUser);
        if (!isLinked) {
            return User.create(authUser).id;
        } else {
            // we have this user already, so return null
            return null;
        }
    }

    /**
     * Returns the local identifying object if the auth provider/id combination has been linked to a local user account already
     * or null if not.
     * This gets called on any login to check whether the session user still has a valid corresponding local user
     *
     * @param identity
     * @return
     */
    @Override
    public Object getLocalIdentity(final AuthUserIdentity identity) {
        final User u = User.findByAuthUserIdentity(identity);
        if(u != null) {
            return u.id;
        } else {
            return null;
        }
    }

    /**
     * Merges two user accounts after a login with an auth provider/id that is linked to a different account than the login from before
     * Returns the user to generate the session information from
     *
     * @param newUser
     * @param oldUser
     * @return
     */
    @Override
    public AuthUser merge(final AuthUser newUser, final AuthUser oldUser) {
        if (!oldUser.equals(newUser)) {
            User.merge(oldUser, newUser);
        }
        return oldUser;
    }

    /**
     * Links a new account to an exsting local user.
     * Returns the auth user to log in with
     *
     * @param oldUser
     * @param newUser
     */
    @Override
    public AuthUser link(final AuthUser oldUser, final AuthUser newUser) {
        User.addLinkedAccount(oldUser, newUser);
        return newUser;
    }

    /**
     * Gets called when a user logs in - you might make profile updates here with data coming from the login provider
     * or bump a last-logged-in date
     *
     * @param knownUser
     * @return
     */
    @Override
    public AuthUser update(final AuthUser knownUser) {
        // User logged in again, bump last login date
        User.setLastLoginDate(knownUser);
        return knownUser;
    }

}
