package models;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import models.security.*;
import models.security.TokenAction.Type;
import play.Play;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import services.NotificationService;
import services.authentication.AuthenticationService;
import services.usermanagement.UserManagementService;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@Table(name = "users")
public class User extends Model implements Subject {

    private static final long serialVersionUID = 1L;

    /**
     * Constants for each user role
     */
    public static final String USER_ROLE        = "user";
    public static final String ACCOUNTANT_ROLE  = "accountant";
    public static final String CLIENT_ROLE      = "client";

    @Id
    public Long id;

    @Constraints.Email
    // if you make this unique, keep in mind that users *must* merge/link their
    // accounts then on signup with additional providers
    // @Column(unique = true)
    @Column(unique = true)
    public String email;

    public String firstName;

    public String lastName;

    public String gender;

    public String currentCity;

    public String hometown;

    public String educationalLevel;

    public String educationalField;

    public String employmentField;

    @Column(columnDefinition = "TEXT")
    public String bio;

    @Column(columnDefinition = "TEXT")
    public String interests;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date lastLogin;

    public boolean active;

    public boolean emailValidated;

    @ManyToMany
    public List<SecurityRole> roles;

    @OneToMany(cascade = CascadeType.ALL)
    public List<LinkedAccount> linkedAccounts;

    public static final Model.Finder<Long, User> find = new Model.Finder<>(Long.class, User.class);

    @Override
    public String getIdentifier()
    {
        return Long.toString(id);
    }

    @Override
    public List<? extends Role> getRoles() {
        return roles;
    }

    @Override
    public List<? extends Permission> getPermissions() {
        return new ArrayList<>();
    }

    /**
     * Determine whether a user exists in the database given a user authentication identity
     */
    public static boolean existsByAuthUserIdentity(final AuthUserIdentity identity) {
        final ExpressionList<User> exp;

        if (identity instanceof UsernamePasswordAuthUser) {
            exp = getUsernamePasswordAuthUserFind((UsernamePasswordAuthUser) identity);
        } else {
            exp = getAuthUserFind(identity);
        }

        return exp.findRowCount() > 0;
    }

    public List<Notification> getNotifications() {
        return NotificationService.getNotifications(this);
    }

    public List<Notification> getRecentNotifications() {
        return NotificationService.getRecentNotifications(this);
    }

    public Date getLastLogin() { return  lastLogin; }
    public void setLastLogin(Date d) { lastLogin = d; }

    public static class Notification {
        public Date receiptDate;
        public Dated obj;
        public User me;
        public String toString() { //TODO have these include HTML to take action (eg accept / respond)
            if (obj instanceof Message) return "New message from " + ((Message) obj).getSender().firstName;
            if (obj instanceof TextChat) return "New text chat request from " + ((TextChat) obj).getSender().firstName;
            if (obj instanceof VideoChat) return "New video chat request from " + ((VideoChat) obj).getSender().firstName;
            if (obj instanceof Match) return "New match with " + ((Match) obj).getOtherUser(me).firstName;
            if (obj instanceof Reject) return "Rejected by " + ((Reject) obj).getOtherUser(me).firstName;
            return "New notification on " + new SimpleDateFormat("dd MMM yyyy").format(receiptDate);
        }
        public Notification(Dated obj, User me) {
            if (obj.getCreated() == null) receiptDate = new Date();
            else receiptDate = obj.getCreated();
        }
    }

    /**
     * Construct a finder for any user with a given user authentication identity (interface)
     */
    private static ExpressionList<User> getAuthUserFind(final AuthUserIdentity identity) {

        return find.where().eq("active", true)
                .eq("linkedAccounts.providerUserId", identity.getId())
                .eq("linkedAccounts.providerKey", identity.getProvider());
    }

    /**
     * Return a user corresponding to a given user authentication identity (interface)
     */
    public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
        if (identity == null) {
            return null;
        }

        if (identity instanceof UsernamePasswordAuthUser) {
            return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
        } else {
            return getAuthUserFind(identity).findUnique();
        }
    }

    /**
     * Return a user corresponding to a given user authentication identity (object)
     */
    public static User findByUsernamePasswordIdentity(final UsernamePasswordAuthUser identity) {
        return getUsernamePasswordAuthUserFind(identity).findUnique();
    }

    /**
     * Construct a finder for any user with a given user authentication identity (object)
     */
    private static ExpressionList<User> getUsernamePasswordAuthUserFind(final UsernamePasswordAuthUser identity) {
        return getEmailUserFind(identity.getEmail()).eq("linkedAccounts.providerKey", identity.getProvider());
    }

    /**
     * Merge two users LinkedAccounts
     */
    public void merge(final User otherUser) {
        for (final LinkedAccount acc : otherUser.linkedAccounts) {
            this.linkedAccounts.add(LinkedAccount.create(acc));
        }
        // do all other merging stuff here - like resources, etc.

        // deactivate the merged user that got added to this one
        otherUser.active = false;
        Ebean.save(Arrays.asList(new User[] { otherUser, this }));
    }

    /**
     * Signup a user given a prepared user authentication identity
     */
    public static User create(final com.feth.play.module.pa.user.AuthUser authUser) {
        final User user = new User();
        user.roles = new ArrayList<>();
        user.roles.add(SecurityRole.findByRoleName(USER_ROLE));

        user.active = true;

        user.lastLogin = new Date();
        user.linkedAccounts = Collections.singletonList(LinkedAccount.create(authUser));

        if (authUser instanceof EmailIdentity) {
            final EmailIdentity identity = (EmailIdentity) authUser;
            // Remember, even when getting them from FB & Co., emails should be
            // verified within the application as a security breach there might
            // break your security as well!
            user.email = identity.getEmail();
            user.emailValidated = false;
        }

        if (authUser instanceof NameIdentity) {
            final NameIdentity identity = (NameIdentity) authUser;
            final String name = identity.getName();
            if (name != null) {
                user.firstName = name;
            }
        }

        if (authUser instanceof AuthUser) {

            final UserSignup signup =  ((AuthUser) authUser).signup;
            user.emailValidated=true;
            user.firstName = signup.firstName;
            user.lastName = signup.lastName;
            user.gender = signup.gender;
            user.currentCity = signup.currentCity;
            user.hometown = signup.hometown;
            user.educationalLevel = signup.educationalLevel;
            user.educationalField = signup.educationalField;
            user.employmentField = signup.employmentField;
            user.bio = signup.bio;
            user.interests = signup.interests;

            user.emailValidated = true;

            if (user.roles == null) user.roles = new ArrayList<SecurityRole>();

        }

        user.save();
        user.saveManyToManyAssociations("roles");
        // user.saveManyToManyAssociations("permissions");
        return user;
    }

    public static void merge(final com.feth.play.module.pa.user.AuthUser oldUser, final com.feth.play.module.pa.user.AuthUser newUser) {
        User.findByAuthUserIdentity(oldUser).merge(User.findByAuthUserIdentity(newUser));
    }

    public static void addLinkedAccount(final com.feth.play.module.pa.user.AuthUser oldUser, final com.feth.play.module.pa.user.AuthUser newUser) {
        final User u = User.findByAuthUserIdentity(oldUser);
        u.linkedAccounts.add(LinkedAccount.create(newUser));
        u.save();
    }

    public static void setLastLoginDate(final com.feth.play.module.pa.user.AuthUser knownUser) {
        final User u = User.findByAuthUserIdentity(knownUser);
        u.lastLogin = new Date();
        u.save();
    }

    public static User findByEmail(final String email) {
        return getEmailUserFind(email).findUnique();
    }

    private static ExpressionList<User> getEmailUserFind(final String email) {
        return find.where().eq("active", true).eq("email", email);
    }

    public LinkedAccount getAccountByProvider(final String providerKey) {
        return LinkedAccount.findByProviderKey(this, providerKey);
    }

    public static void verify(final User unverified) {
        // You might want to wrap this into a transaction
        unverified.emailValidated = true;
        unverified.save();
        TokenAction.deleteByUser(unverified, Type.EMAIL_VERIFICATION);
    }

    public void changePassword(final UsernamePasswordAuthUser authUser, final boolean create) {
        LinkedAccount a = this.getAccountByProvider(authUser.getProvider());

        if (a == null) {
            if (create) {
                a = LinkedAccount.create(authUser);
                a.user = this;
            } else {
                throw new RuntimeException("Account not enabled for password usage");
            }
        }

        a.providerUserId = authUser.getHashedPassword();
        a.save();
    }

    public void resetPassword(final UsernamePasswordAuthUser authUser, final boolean create) {
        this.changePassword(authUser, create);
        TokenAction.deleteByUser(this, Type.PASSWORD_RESET);
    }

    public static List<User> getAccountants() {
        return User.find.where().in("roles", Arrays.asList(SecurityRole.findByRoleName(User.ACCOUNTANT_ROLE))).findList();
    }

}