package controllers.security;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import models.User;
import models.security.*;
import play.data.Form;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import scala.Option;
import services.authentication.AuthenticationService;
import views.html.login;
import views.html.signup;

import static play.data.Form.form;

/**
 * Handles all user management, registration, login, verification, and forgot-password functionality
 */
public class Account extends Controller {

  private static final Form<AuthenticationService.MyIdentity> FORGOT_PASSWORD_FORM = form(AuthenticationService.MyIdentity.class);

  private static final Form<PasswordReset> PASSWORD_RESET_FORM = form(PasswordReset.class);

  @SubjectPresent
  public static Result askMerge() {
      return badRequest();  //account merging unsupported
  }

  @SubjectPresent
  public static Result askLink() {
      return badRequest();  //account linking unsupported
  }

  public static Result oAuthDenied(final String getProviderKey) {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        return badRequest(); //oauth unsupported
  }

  public static Result login() {
        return ok(login.render(AuthenticationService.LOGIN_FORM));
  }

  public static Result signup() {
        return ok(signup.render(AuthenticationService.SIGNUP_FORM));
  }


  public static Result manageProfile() {
      /* User u = getLocalUser(session());
      UserSignup ra = new UserSignup();
      ra.email = u.email;
     ra.name = u.name;


      ra.surname = u.surname;
      ra.id = u.id;
      if (u.roles.contains(SecurityRole.findByRoleName(User.ACCOUNTANT_ROLE))) ra.accountant = "true";
      else ra.accountant = "false";
      ra.city = u.city;
      ra.streetAddress = u.streetAddress;
      ra.po = u.po;
      ra.companyName = u.companyName;
      if (u.assignedAccountant != null) {
          ra.assignedAccountant = u.assignedAccountant.id.toString();
      }

      ra.id = u.id; */
      return ok(views.html.account.manage_profile.render());
  }

  public static Result doLogin() {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final Form<UserLogin> filledForm = AuthenticationService.LOGIN_FORM
                .bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not fill everything properly
            return badRequest(login.render(filledForm));
        } else {
            // Everything was filled
            return UsernamePasswordAuthProvider.handleLogin(ctx());
        }
  }

    public static Result doSignup() {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final Form<UserSignup> filledForm = AuthenticationService.SIGNUP_FORM
                .bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not fill everything properly
            scala.Predef.println(filledForm.errors());
            return badRequest(signup.render(filledForm));
        } else {
            // Everything was filled
            // do something with your part of the form before handling the user
            // signup
            return UsernamePasswordAuthProvider.handleSignup(ctx());
        }
    }

/*    @SubjectPresent
    public static Result doManageProfile() {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final Form<UserSignup> filledForm = AuthenticationService.SIGNUP_FORM
                .bindFromRequest();

        if (filledForm.hasErrors()) {
            // User did not fill everything properly
            return badRequest(views.html.account.manage_profile.render(filledForm));
        } else {
            // Everything was filled
            // do something with your part of the form before handling the user
            // signup
            UserSignup ra = filledForm.get();

            if (ra == null) return badRequest(views.html.account.manage_profile.render(filledForm));

            if (ra.id == null) return badRequest(views.html.account.manage_profile.render(filledForm));

            User u = User.find.byId(ra.id);

            if (u == null) return badRequest(views.html.account.manage_profile.render(filledForm));

            boolean emailChanged = false;
            if (!u.email.equals(ra.email)) emailChanged = true;
            u.email = ra.email;

            if (ra.password != null) {
               if (ra.password.length() > 0) {
                   AuthUser rau = new AuthUser(ra);
                   u.changePassword(rau, false);
               }
            }

            u.name = ra.name;
            u.surname = ra.surname;
            u.companyName = ra.companyName;
            u.streetAddress = ra.streetAddress;
            u.city = ra.city;
            u.po = ra.po;

            boolean accountant = false;
            if (ra.accountant.toLowerCase().equals("true")) accountant = true;
            else accountant = false;


            Long assignedAccountantId = -1l;
            try {
                if (!accountant) assignedAccountantId = Long.parseLong(ra.assignedAccountant);
            } catch (Exception e) {
                play.Logger.error("",e);
            }

            if (ra.assignedAccountant != null)
                u.assignedAccountant = User.find.byId(assignedAccountantId);
            else u.assignedAccountant = null;

            SecurityRole acctRole = SecurityRole.findByRoleName(User.ACCOUNTANT_ROLE);
            SecurityRole clientRole = SecurityRole.findByRoleName(User.CLIENT_ROLE);
            if (u.roles.contains(acctRole) && !accountant) {
                u.roles.remove(acctRole);
                u.roles.add(clientRole);
            } else if (u.roles.contains(clientRole) && accountant) {
                u.roles.remove(clientRole);
                u.roles.add(acctRole);
            }

            u.save();
            u.saveManyToManyAssociations("roles");

            //if email was changed, must login again
            if (emailChanged) return redirect(routes.Account.login());
            return ok(views.html.account.manage_profile.render(filledForm));

        }
    }*/

    public static Result verify(final String token) {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final TokenAction ta = tokenIsValid(token, TokenAction.Type.EMAIL_VERIFICATION);
        if (ta == null) {
            return badRequest("No/invalid token");
        }
        if (ta.targetUser == null) {
            return internalServerError("Unable to find user");
        }
        final String email = ta.targetUser.email;
        User.verify(ta.targetUser);
        flash(AuthenticationService.FLASH_MESSAGE_KEY,
                Messages.get("playauthenticate.verify_email.success", email));
        if (Account.getLocalUser(session()) != null) {
            return Results.redirect(controllers.routes.Application.index());
        } else {
            return redirect(routes.Account.login());
        }
    }

    public static Result exists() {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        return ok(views.html.account.exists.render());
    }

    public static Result unverified() {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        return ok(views.html.account.unverified.render());
    }

    public static User getLocalUser(final Http.Session session) {
        final com.feth.play.module.pa.user.AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
        final User localUser = User.findByAuthUserIdentity(currentAuthUser);
        return localUser;
    }

    public static Option<User> getLocalUserOption() {
        User u = getLocalUser(session());
        if (u != null) return scala.Option.apply(u);
        else return scala.Option.apply(null);
    }

    private static TokenAction tokenIsValid(final String token, final TokenAction.Type type) {
        TokenAction ret = null;
        if (token != null && !token.trim().isEmpty()) {
            final TokenAction ta = TokenAction.findByToken(token, type);
            if (ta != null && ta.isValid()) {
                ret = ta;
            }
        }
        return ret;
    }

    public static Result forgotPassword(final String email) {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        Form<AuthenticationService.MyIdentity> form = FORGOT_PASSWORD_FORM;
        if (email != null && !email.trim().isEmpty()) {
            form = FORGOT_PASSWORD_FORM.fill(new AuthenticationService.MyIdentity(email));
        }
        return ok(views.html.account.password_forgot.render(form));
    }

    public static Result doForgotPassword() {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final Form<AuthenticationService.MyIdentity> filledForm = FORGOT_PASSWORD_FORM
                .bindFromRequest();
        if (filledForm.hasErrors()) {
            // User did not fill in his/her email
            return badRequest(views.html.account.password_forgot.render(filledForm));
        } else {
            // The email address given *BY AN UNKNWON PERSON* to the form - we
            // should find out if we actually have a user with this email
            // address and whether password login is enabled for him/her. Also
            // only send if the email address of the user has been verified.
            final String email = filledForm.get().email;

            // We don't want to expose whether a given email address is signed
            // up, so just say an email has been sent, even though it might not
            // be true - that's protecting our user privacy.
            flash(AuthenticationService.FLASH_MESSAGE_KEY,
                    Messages.get(
                            "playauthenticate.reset_password.message.instructions_sent",
                            email));

            final User user = User.findByEmail(email);
            if (user != null) {
                // yep, we have a user with this email that is active - we do
                // not know if the user owning that account has requested this
                // reset, though.
                final AuthenticationService provider = AuthenticationService
                        .getProvider();
                // User exists
                if (user.emailValidated) {
                    provider.sendPasswordResetMailing(user, ctx());
                    // In case you actually want to let (the unknown person)
                    // know whether a user was found/an email was sent, use,
                    // change the flash message
                } else {
                    // We need to change the message here, otherwise the user
                    // does not understand whats going on - we should not verify
                    // with the password reset, as a "bad" user could then sign
                    // up with a fake email via OAuth and get it verified by an
                    // a unsuspecting user that clicks the link.
                    flash(AuthenticationService.FLASH_MESSAGE_KEY,
                            Messages.get("playauthenticate.reset_password.message.email_not_verified"));

                    // You might want to re-send the verification email here...
                    provider.sendVerifyEmailMailingAfterSignup(user, ctx());
                }
            }

            return redirect(controllers.routes.Application.index());
        }
    }

    public static Result resetPassword(final String token) {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final TokenAction ta = tokenIsValid(token, TokenAction.Type.PASSWORD_RESET);
        if (ta == null) {
            return badRequest(views.html.account.no_token_or_invalid.render());
        }

        return ok(views.html.account.password_reset.render(PASSWORD_RESET_FORM
                .fill(new PasswordReset(token))));
    }

    public static Result doResetPassword() {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        final Form<PasswordReset> filledForm = PASSWORD_RESET_FORM
                .bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(views.html.account.password_reset.render(filledForm));
        } else {
            final String token = filledForm.get().token;
            final String newPassword = filledForm.get().password;

            final TokenAction ta = tokenIsValid(token, TokenAction.Type.PASSWORD_RESET);
            if (ta == null) {
                return badRequest(views.html.account.no_token_or_invalid.render());
            }
            final User u = ta.targetUser;
            try {
                // Pass true for the second parameter if you want to
                // automatically create a password and the exception never to
                // happen
                u.resetPassword(new AuthUser(newPassword),
                        false);
            } catch (final RuntimeException re) {
                flash(AuthenticationService.FLASH_MESSAGE_KEY,
                        Messages.get("playauthenticate.reset_password.message.no_password_account"));
            }
            final boolean login = AuthenticationService.getProvider()
                    .isLoginAfterPasswordReset();
            if (login) {
                // automatically log in
                flash(AuthenticationService.FLASH_MESSAGE_KEY,
                        Messages.get("playauthenticate.reset_password.message.success.auto_login"));

                return PlayAuthenticate.loginAndRedirect(ctx(),
                        new LoginAuthUser(u.email));
            } else {
                // send the user to the login page
                flash(AuthenticationService.FLASH_MESSAGE_KEY,
                        Messages.get("playauthenticate.reset_password.message.success.manual_login"));
            }
            return redirect(routes.Account.login());
        }
    }

}
