package controllers;

import controllers.security.Account;
import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created by Adam on 11/3/2014.
 */
public class Application extends Controller {
    public static Result index() { return redirect(controllers.security.routes.Account.manageProfile()); }

    public static Result jsRoutes() {
        return ok(
                Routes.javascriptRouter("jsRoutes",
                        controllers.security.routes.javascript.Account.forgotPassword()))
                .as("text/javascript");
    }

}
