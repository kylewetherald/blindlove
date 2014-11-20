package test;

import controllers.security.routes;
import models.User;
import models.security.AuthUser;
import models.security.SecurityRole;
import models.security.TokenAction;
import models.security.UserSignup;
import org.junit.*;

import play.Play;
import play.mvc.*;
import play.test.*;
import play.libs.F.*;
import services.authentication.AuthenticationService;
import services.usermanagement.UserManagementService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

public class AccountTest {

    /**
     * Construct or return test user
     */
    public static User testUser() {
        List<User> clients = User.find.where().in("roles", Arrays.asList(SecurityRole.findByRoleName(User.CLIENT_ROLE))).findList();
        if (clients.isEmpty()) {
            UserManagementService msp = new UserManagementService(Play.application());
            UserSignup clientSignup = new UserSignup();
            AuthUser clientAuthIdentity;

            clientSignup.firstName = "Jane";
            clientSignup.lastName = "Doe";
            clientSignup.email = "jdoe@acme.com";
            clientSignup.password = "client";
            clientSignup.repeatPassword = "client";
            clientSignup.gender = "female";
            clientSignup.currentCity = "Pittsburgh";
            clientSignup.hometown = "Pittsburgh";
            clientSignup.educationalLevel = "etc";
            clientSignup.educationalField = "etc1";
            clientSignup.employmentField = "etc2";
            clientSignup.bio = "whatever";
            clientSignup.interests = "none";

            clientAuthIdentity = new AuthUser(clientSignup);

            User client = User.find.byId((Long) msp.save(clientAuthIdentity));
            client.emailValidated = true;
            client.active = true;
            client.save();
            return client;
        }
        return clients.get(0);
    }

    @Test
    public void loginB1() {
       running(fakeApplication(inMemoryDatabase()), new Runnable() {
           public void run() {
               testUser();
               FakeRequest fr = new FakeRequest();
               //Populate login form
               Map<String,String> login = new HashMap<String,String>();
               login.put("email","jdoe@acme.com");
               login.put("password","client");
               //Post login form to controller
               Result result = callAction(routes.ref.Account.doLogin(), fr.withFormUrlEncodedBody(login));
               //Asset that login was successful
               assertThat(status(result)).isEqualTo(SEE_OTHER);
               assertThat(session(result).remove("pa.u.id")).contains("jdoe@acme.com");
           }
       });
   }

   @Test
   public void loginB2() {
     running(fakeApplication(inMemoryDatabase()), new Runnable() {
         public void run() {
           testUser();
           FakeRequest fr = new FakeRequest();
           //Populate login form
           Map<String,String> login = new HashMap<String,String>();
           login.put("email","jdoe@acme.com");
           login.put("password","client");
           //Post login form to controller
           Result result = callAction(routes.ref.Account.doLogin(), fr.withFormUrlEncodedBody(login));
           //Assert that login was successful
           assertThat(status(result)).isEqualTo(SEE_OTHER);
           assertThat(session(result).remove("pa.u.id")).contains("jdoe@acme.com");
           assertThat(contentAsString(result)).contains("");
         }
     });
  }

   @Test
   public void loginB3() {
     running(fakeApplication(inMemoryDatabase()), new Runnable() {
         public void run() {
           testUser();
           FakeRequest fr = new FakeRequest();
           //Populate login form
           Map<String,String> login = new HashMap<String,String>();
           login.put("email","jdoe@acme.com");
           login.put("password","bogus");
           //Post login form to controller
           Result result = callAction(routes.ref.Account.doLogin(), fr.withFormUrlEncodedBody(login));
           //Assert that login was successful
           assertThat(status(result)).isEqualTo(SEE_OTHER);
           assertThat(session(result).remove("pa.u.id")).isNull();
         }
     });
  }

  @Test
   public void loginB4() {
     running(fakeApplication(inMemoryDatabase()), new Runnable() {
         public void run() {
           testUser();
           FakeRequest fr = new FakeRequest();
           //Populate login form
           Map<String,String> login = new HashMap<String,String>();
           login.put("email","jdoe@acme.com");
           login.put("password","");
           //Post login form to controller
           Result result = callAction(routes.ref.Account.doLogin(), fr.withFormUrlEncodedBody(login));
           //Assert that login was successful
           assertThat(status(result)).isEqualTo(SEE_OTHER);
           assertThat(session(result).remove("pa.u.id")).isNull();
         }
     });
  }


    @Test
    public void registerAndConfirmAccountant() {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            public void run() {
                FakeRequest fr = new FakeRequest();
                //Populate registration form
                Map<String,String> register = new HashMap<String,String>();
                register.put("email","test@accountant.com");
                register.put("password","accountant");
                register.put("repeatPassword","accountant");
                register.put("firstName","test");
                register.put("lastName","accountant");
                register.put("currentCity","testaccountants");
                register.put("hometown","testaccountants");
                register.put("gender","male");
                register.put("educationalLevel","etc");
                register.put("educationalField","etc1");
                register.put("employmentField","etc2");
                register.put("bio","my bio");
                register.put("interests","my interests");


                //Post registration data to controller
                Result result = callAction(routes.ref.Account.doSignup(), fr.withFormUrlEncodedBody(register));
                //Assert registration successful
                assertThat(status(result)).isEqualTo(SEE_OTHER);
                User u = User.find.where().eq("email","test@accountant.com").findUnique();
                assertThat(u).isNotNull();

                assertThat(u.emailValidated).isTrue();
            }
        });
    }    
}