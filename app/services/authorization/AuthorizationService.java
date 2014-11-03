package services.authorization;

import models.User;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Http;
import play.mvc.SimpleResult;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.core.models.Subject;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;

/**
 * Authorization service. Provides overrideable handlers for authorization events
 */
public class AuthorizationService extends AbstractDeadboltHandler {

    /**
     * Invoked immediately before controller or view restrictions are checked. This forms the integration with any
     * authentication actions that may need to occur.
     *
     * @param context the HTTP context
     * @return the action result if an action other than the delegate must be taken, otherwise null. For a case where
     *         the user is authenticated (or whatever your test condition is), this will be null otherwise the restriction
     *         won't be applied.
     */
    @Override
    public F.Promise<SimpleResult> beforeAuthCheck(final Http.Context context) {
        if (PlayAuthenticate.isLoggedIn(context.session())) {
            // user is logged in
            return F.Promise.pure(null);
        } else {
            // user is not logged in

            // call this if you want to redirect your visitor to the page that
            // was requested before sending him to the login page
            // if you don't call this, the user will get redirected to the page
            // defined by your resolver
            final String originalUrl = PlayAuthenticate.storeOriginalUrl(context);

            context.flash().put("error", Messages.get("raa.login_first") + originalUrl + "'");
            return F.Promise.promise(new F.Function0<SimpleResult>() {
                @Override
                public SimpleResult apply() throws Throwable {
                    return redirect(PlayAuthenticate.getResolver().login());
                }
            });
        }
    }

    /**
     * Gets the current {@link be.objectify.deadbolt.core.models.Subject}, e.g. the current user.
     *
     * @param context the HTTP context
     * @return the current subject
     */
    @Override
    public Subject getSubject(final Http.Context context) {
        final AuthUserIdentity u = PlayAuthenticate.getUser(context);
        // Caching might be a good idea here
        return User.findByAuthUserIdentity(u);
    }

    /**
     * Gets the handler used for dealing with resources restricted to specific users/groups.
     *
     * @param context the HTTP context
     * @return the handler for restricted resources. May be null.
     */
    @Override
    public DynamicResourceHandler getDynamicResourceHandler(final Http.Context context) {
        return null;
    }

    /**
     * Invoked when an access failure is detected on <i>controllerClassName</i>.
     *
     * @param context the HTTP context
     * @param content the content type hint.  This can be used to return a response in the appropriate content
     *                type, e.g. JSON
     * @return the action result
     */
    @Override
    public F.Promise<SimpleResult> onAuthFailure(final Http.Context context, final String content) {
        // if the user has a cookie with a valid user and the local user has
        // been deactivated/deleted in between, it is possible that this gets
        // shown. You might want to consider to sign the user out in this case.
        return F.Promise.promise(new F.Function0<SimpleResult>() {
            @Override
            public SimpleResult apply() throws Throwable {
                return forbidden("Forbidden");
            }
        });
    }
}