import com.feth.play.module.pa.PlayAuthenticate;
import models.User;
import models.security.SecurityRole;
import play.*;
import services.authentication.AuthenticationService;


/**
 * Created with IntelliJ IDEA.
 * User: Michael
 * Date: 01/07/14
 * Time: 08:57
 * To change this template use File | Settings | File Templates.
 */
public class Global extends play.utils.crud.GlobalCRUDSettings {

    @Override
    public void onStart(play.Application app) {

        super.onStart(app);

        PlayAuthenticate.setResolver(new AuthenticationService.AuthenticationResolver());

        SecurityRole.initializeSecurityRoles();

        Logger.info("Application has started");

    }

    @Override
    public void onStop(Application app) {
        super.onStop(app);
        Logger.info("Application shutdown...");
    }

}
