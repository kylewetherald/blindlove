package services.authentication;

import com.feth.play.module.mail.Mailer;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import controllers.routes;
import models.*;
import models.security.*;
import play.Application;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Call;
import play.mvc.Http;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static play.data.Form.form;

/**
 * Authentication service contains business logic for login, signup, verification, forgot-password
 */
public class AuthenticationService
        extends
        UsernamePasswordAuthProvider<String, LoginAuthUser, AuthUser, UserLogin, UserSignup> {

    //constants for client-side alert types
    public static final String FLASH_MESSAGE_KEY = "message";
    public static final String FLASH_ERROR_KEY = "error";

    private static final String SETTING_KEY_VERIFICATION_LINK_SECURE = SETTING_KEY_MAIL
            + "." + "verificationLink.secure";
    private static final String SETTING_KEY_PASSWORD_RESET_LINK_SECURE = SETTING_KEY_MAIL
            + "." + "passwordResetLink.secure";
    private static final String SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET = "loginAfterPasswordReset";

    private static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";

    /**
     * Throws exception if the provided setting keys are not found
     */
    @Override
    protected List<String> neededSettingKeys() {
        final List<String> needed = new ArrayList<String>(
                super.neededSettingKeys());
        needed.add(SETTING_KEY_VERIFICATION_LINK_SECURE);
        needed.add(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
        needed.add(SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
        return needed;
    }

    public static AuthenticationService getProvider() {
        return (AuthenticationService) PlayAuthenticate
                .getProvider(UsernamePasswordAuthProvider.PROVIDER_KEY);
    }

    /**
     * Domain classes for constructing/validating Play forms
     */

    /**
     * Base form class
     */
    //TODO create own file, change name to UserIdentity
    public static class MyIdentity {

        public MyIdentity() {
        }

        public MyIdentity(final String email) {
            this.email = email;
        }

        @Constraints.Required
        @Constraints.Email
        public String email;
    }

    public static final Form<UserSignup> SIGNUP_FORM = form(UserSignup.class);
    public static final Form<UserLogin> LOGIN_FORM = form(UserLogin.class);

    public AuthenticationService(Application app) {
        super(app);
    }

    protected Form<UserSignup> getSignupForm() {
        return SIGNUP_FORM;
    }

    protected Form<UserLogin> getLoginForm() {
        return LOGIN_FORM;
    }

    /**
     * Business logic for creating a persisted User from an user authentication identity
     */
    @Override
    protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.SignupResult signupUser(final AuthUser user) {
        final User u = User.findByUsernamePasswordIdentity(user);
        if (u != null) {
            if (u.emailValidated) {
                // This user exists, has its email validated and is active
                return SignupResult.USER_EXISTS;
            } else {
                // this user exists, is active but has not yet validated its
                // email
                return SignupResult.USER_EXISTS_UNVERIFIED;
            }
        }
        // The user either does not exist or is inactive - create a new one
        @SuppressWarnings("unused")
        final User newUser = User.create(user);
        // Usually the email should be verified before allowing login, however
        // if you return
        // return SignupResult.USER_CREATED;
        // then the user gets logged in directly
        return SignupResult.USER_CREATED_UNVERIFIED;
    }

    /**
     * Business logic for logging in a user based on an user authentication identity
     */
    @Override
    protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.LoginResult loginUser(
            final LoginAuthUser authUser) {
        final User u = User.findByUsernamePasswordIdentity(authUser);
        if (u == null) {
            return LoginResult.NOT_FOUND;
        } else {
            if (!u.emailValidated) {
                return LoginResult.USER_UNVERIFIED;
            } else {
                for (final LinkedAccount acc : u.linkedAccounts) {
                    if (getKey().equals(acc.providerKey)) {
                        if (authUser.checkPassword(acc.providerUserId,
                                authUser.getPassword())) {
                            // Password was correct
                            u.setLastLogin(new Date());
                            u.save();
                            return LoginResult.USER_LOGGED_IN;
                        } else {
                            // if you don't return here,
                            // you would allow the user to have
                            // multiple passwords defined
                            // usually we don't want this
                            return LoginResult.WRONG_PASSWORD;
                        }
                    }
                }
                return LoginResult.WRONG_PASSWORD;
            }
        }
    }

    /**
     * Determines route for an unsuccessful signup due to the user authentication identity already existing in the database
     */
    @Override
    protected Call userExists(final UsernamePasswordAuthUser authUser) {
        return controllers.security.routes.Account.exists();
    }

    /**
     * Determines route for a successful signup resulting in an unverified user
     */
    @Override
    protected Call userUnverified(final UsernamePasswordAuthUser authUser) {
        return controllers.security.routes.Account.login();
    }

    /**
     * Construct user authentication identity from submitted signup form data
     */
    @Override
    protected AuthUser buildSignupAuthUser(
            final UserSignup signup, final Http.Context ctx) {
        return new AuthUser(signup);
    }

    /**
     * Construct user authentication identity from submitted login form data
     */
    @Override
    protected LoginAuthUser buildLoginAuthUser(
            final UserLogin login, final Http.Context ctx) {
        return new LoginAuthUser(login.getPassword(),
                login.getEmail());
    }

    /**
     * Construct user authentication identity for logging in from a user authentication identity generated during signup
     */
    @Override
    protected LoginAuthUser transformAuthUser(final AuthUser authUser, final Http.Context context) {
        return new LoginAuthUser(authUser.getEmail());
    }

    /**
     * Construct subject line for email verification message
     */
    @Override
    protected String getVerifyEmailMailingSubject(
            final AuthUser user, final Http.Context ctx) {
        return Messages.get("playauthenticate.password.verify_signup.subject");
    }

    /**
     * Handler for unsuccessful login; returns redirect URL
     */
    @Override
    protected String onLoginUserNotFound(final Http.Context context) {
        context.flash()
                .put(FLASH_ERROR_KEY,
                        Messages.get("playauthenticate.password.login.unknown_user_or_pw"));
        return super.onLoginUserNotFound(context);
    }

    /**
     * Construct body for email verification message
     */
    @Override
    protected Mailer.Mail.Body getVerifyEmailMailingBody(final String token,final AuthUser user, final Http.Context ctx) {

        final boolean isSecure = getConfiguration().getBoolean(SETTING_KEY_VERIFICATION_LINK_SECURE);
        final String url = controllers.security.routes.Account.verify(token).absoluteURL(ctx.request(), isSecure);

        final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
        final String langCode = lang.code();

        final String html = getEmailTemplate(
                "views.html.account.signup.email.verify_email", langCode, url,
                token, user.signup.firstName + " " + user.signup.lastName, user.getEmail()
        );

        final String text = getEmailTemplate(
                "views.txt.account.signup.email.verify_email", langCode, url,
                token, user.signup.firstName + " " + user.signup.lastName, user.getEmail()
        );

        return new Mailer.Mail.Body(text, html);
    }

    /**
     * Generate random UUID for email verification token
     */
    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Construct and persist email verification token
     */
    @Override
    protected String generateVerificationRecord(
            final AuthUser user) {
        return generateVerificationRecord(User.findByAuthUserIdentity(user));
    }

    /**
     * Construct and persist email verification token
     */
    protected String generateVerificationRecord(final User user) {
        final String token = generateToken();
        // Do database actions, etc.

        TokenAction.create(TokenAction.Type.EMAIL_VERIFICATION, token, user);
        return token;
    }

    /**
     * Construct and persist password reset token
     */
    protected String generatePasswordResetRecord(final User u) {
        final String token = generateToken();
        TokenAction.create(TokenAction.Type.PASSWORD_RESET, token, u);
        return token;
    }

    /**
     * Construct subject for email verification message
     */
    protected String getPasswordResetMailingSubject(final User user, final Http.Context ctx) {
        return Messages.get("playauthenticate.password.reset_email.subject");
    }

    /**
     * Construct subject for email verification message
     */
    protected Mailer.Mail.Body getPasswordResetMailingBody(final String token,final User user, final Http.Context ctx) {

        final boolean isSecure = getConfiguration().getBoolean(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
        final String url =  controllers.security.routes.Account.resetPassword(token).absoluteURL(ctx.request(), isSecure);

        final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
        final String langCode = lang.code();

        final String html = getEmailTemplate(
                "views.html.account.email.password_reset", langCode, url,
                token, user.firstName + " " + user.lastName, user.email
        );

        final String text = getEmailTemplate(
                "views.txt.account.email.password_reset", langCode, url, token,
                user.firstName + " " + user.lastName, user.email
        );

        return new Mailer.Mail.Body(text, html);
    }

    /**
     * Send password reset message
     */
    public void sendPasswordResetMailing(final User user, final Http.Context ctx) {
        final String token = generatePasswordResetRecord(user);
        final String subject = getPasswordResetMailingSubject(user, ctx);
        final Mailer.Mail.Body body = getPasswordResetMailingBody(token, user, ctx);
     //   sendMail(subject, body, getEmailName(user));
    }

    /**
     * Retrieve configuration variable indicating whether to automatically login the user after password reset
     */
    public boolean isLoginAfterPasswordReset() {
        return getConfiguration().getBoolean(
                SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
    }

    /**
     * Construct subject for email verification message immediately following registration
     */
    protected String getVerifyEmailMailingSubjectAfterSignup(final User user, final Http.Context ctx) {
        return Messages.get("playauthenticate.password.verify_email.subject");
    }

    /**
     * Construct an email template given the template and expected parameters.
     * Uses reflection to search for the provided template class name and populates with remaining parameters
     */
    protected String getEmailTemplate(final String template,final String langCode, final String url, final String token,
                                      final String name, final String email) {
        Class<?> cls = null;
        String ret = null;

        try {
            cls = Class.forName(template + "_" + langCode);
        } catch (ClassNotFoundException e) {
            Logger.warn("Template: '"
                    + template
                    + "_"
                    + langCode
                    + "' was not found! Trying to use English fallback template instead.");
        }

        if (cls == null) {
            try {
                cls = Class.forName(template + "_"
                        + EMAIL_TEMPLATE_FALLBACK_LANGUAGE);
            } catch (ClassNotFoundException e) {
                Logger.error("Fallback template: '" + template + "_"
                        + EMAIL_TEMPLATE_FALLBACK_LANGUAGE
                        + "' was not found either!");
            }
        }

        if (cls != null) {
            Method htmlRender = null;
            try {
                htmlRender = cls.getMethod("render", String.class,
                        String.class, String.class, String.class);
                ret = htmlRender.invoke(null, url, token, name, email)
                        .toString();

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Construct body for email verification message immediately following registration
     */
    protected Mailer.Mail.Body getVerifyEmailMailingBodyAfterSignup(final String token, final User user, final Http.Context ctx) {

        final boolean isSecure = getConfiguration().getBoolean(SETTING_KEY_VERIFICATION_LINK_SECURE);
        final String url = controllers.security.routes.Account.verify(token).absoluteURL(ctx.request(), isSecure);

        final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
        final String langCode = lang.code();

        final String html = getEmailTemplate(
                "views.html.account.email.verify_email", langCode, url, token,
                user.firstName + " " + user.lastName, user.email
        );

        final String text = getEmailTemplate(
                "views.txt.account.email.verify_email", langCode, url, token,
                user.firstName + " " + user.lastName, user.email
        );

        return new Mailer.Mail.Body(text, html);
    }

    /**
     * Send email verification message immediately following registration
     */
    public void sendVerifyEmailMailingAfterSignup(final User user, final Http.Context ctx) {

        final String subject = getVerifyEmailMailingSubjectAfterSignup(user, ctx);
        final String token = generateVerificationRecord(user);
        final Mailer.Mail.Body body = getVerifyEmailMailingBodyAfterSignup(token, user, ctx);
       // sendMail(subject, body, getEmailName(user));
    }

    private String getEmailName(final User user) {
        return getEmailName(user.email, user.firstName + " " + user.lastName);
    }

    /**
     * Resolves correct routes for various authentication scenarios
     */
    //TODO create own file
    public static class AuthenticationResolver extends PlayAuthenticate.Resolver {

        @Override
        public Call login() {
            // Your login page
            return controllers.security.routes.Account.login();
        }

        @Override
        public Call afterAuth() {
            // The user will be redirected to this page after authentication
            // if no original URL was saved
            return routes.Application.index();
        }

        @Override
        public Call afterLogout() {
            return routes.Application.index();
        }

        @Override
        public Call auth(final String provider) {
            // You can provide your own authentication implementation,
            // however the default should be sufficient for most cases
            return com.feth.play.module.pa.controllers.routes.Authenticate
                    .authenticate(provider);
        }

        @Override
        public Call askMerge() {
            return controllers.security.routes.Account.askMerge();
        }

        @Override
        public Call askLink() {
            return controllers.security.routes.Account.askLink();
        }

        @Override
        public Call onException(final AuthException e) {
            if (e instanceof AccessDeniedException) {
                return controllers.security.routes.Account
                        .oAuthDenied(((AccessDeniedException) e)
                                .getProviderKey());
            }
            //handle errors
            return super.onException(e);
        }

    }
}
