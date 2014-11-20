package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.Expr;
import controllers.security.Account;
import models.*;
import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;
import services.MatcherService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Adam on 11/3/2014.
 */
public class Application extends Controller {
    public static Result index() { return ok(views.html.index.render()); }

    public static Result jsRoutes() {
        return ok(
                Routes.javascriptRouter("jsRoutes",
                        controllers.security.routes.javascript.Account.forgotPassword()))
                .as("text/javascript");
    }

    public static class UserResult {
        public User u;
        public String field;
        public UserResult(User u, String field) { this.u = u; this.field = field; }
    }

    private static List<UserResult> usersToUserResults(List<User> us, String f) {
        List<UserResult> ur = new ArrayList<UserResult>();
        for (User u:us) {
            ur.add(new UserResult(u, f));
        }
        return ur;
    }

    public static Result notifications() { return ok(views.html.notifications.render()); }

    private static String wrapQuery(String query) { return "%" + query + "%"; }

    @SubjectPresent
    public static Result search() {

        Map<String, String[]> ms = request().body().asFormUrlEncoded();
        if (ms == null) return badRequest(views.html.index.render());
        String[] ss = ms.get("searchText");
        if (ss == null || ss.length < 1) return badRequest(views.html.index.render());
        String query = ss[0];

        List<UserResult> u = usersToUserResults(User.find.where().ilike("bio", wrapQuery(query)).findList(), "bio");
        u.addAll(usersToUserResults(User.find.where().ilike("currentCity", wrapQuery(query)).findList(), "currentCity"));
        u.addAll(usersToUserResults(User.find.where().ilike("educationalField", wrapQuery(query)).findList(), "educationalField"));
        u.addAll(usersToUserResults(User.find.where().ilike("employmentField", wrapQuery(query)).findList(), "employmentField"));
        u.addAll(usersToUserResults(User.find.where().ilike("gender", wrapQuery(query)).findList(), "gender"));
        u.addAll(usersToUserResults(User.find.where().ilike("hometown", wrapQuery(query)).findList(), "hometown"));
        u.addAll(usersToUserResults(User.find.where().ilike("interests", wrapQuery(query)).findList(), "interests"));

        return ok(views.html.search.render(u));
    }

    @SubjectPresent
    public static Result acceptMatch(Long mid) {
        Match m = Match.find.byId(mid);
        User me = controllers.security.Account.getLocalUser(session());
        if (m.getTargetUser().id == me.id) {
            m.setAccepted(true);
            m.save();
        }
        return redirect(routes.Application.matches());
    }

    @SubjectPresent
    public static Result rejectMatch(Long mid) {
        Match m = Match.find.byId(mid);
        User me = controllers.security.Account.getLocalUser(session());
        if (m.getTargetUser().id == me.id) {
            m.setAccepted(false);
            m.setRejected(true);
            Reject r = new Reject();
            r.setRejector(me);
            r.setRejectee(m.getMatchedUser());
            r.save();
            m.save();
        }
        return redirect(routes.Application.matches());
    }

    @SubjectPresent
    public static Result profile(Long uid) {
        User u = User.find.byId(uid);
        if (u==null) return badRequest(views.html.index.render());
        return ok(views.html.profile.render(u));
    }


    @SubjectPresent
    public static Result matches() {
        User me = controllers.security.Account.getLocalUser(session());
        List<Match> incoming = Match.find.where().eq("targetUser.id", me.id).eq("accepted", false).eq("rejected",false).findList();
        List<Match> outgoing = Match.find.where().eq("matchedUser.id", me.id).eq("accepted", false).findList();
        List<Match> love = Match.find.where().or(Expr.eq("targetUser.id", me.id), Expr.eq("matchedUser.id", me.id)).eq("accepted",true).findList();
        List<Reject> rejections = Reject.find.where().eq("rejectee.id", me.id).findList();
        return ok(views.html.matches.render(love, incoming, outgoing, rejections, MatcherService.matchesJ(me)));
    }

    @SubjectPresent
    public static Result createMatch(Long targetId) {
        User me = controllers.security.Account.getLocalUser(session());
        User target = User.find.byId(targetId);
        Match m = new Match();
        m.setMatchedUser(me);
        m.setTargetUser(target);
        m.setAccepted(false);
        m.save();
        return redirect(routes.Application.matches());
    }

    @SubjectPresent
    public static Result createTextChat(Long mid) {
        User me = controllers.security.Account.getLocalUser(session());
        models.Match match = Match.find.byId(mid);
        if (match == null) return matches();
        if (me == null) return index();
        TextChat tc = new TextChat();
        tc.setSender(me);
        tc.setRecipient(match.getMatchedUser().id == me.id ? match.getTargetUser() : match.getMatchedUser());
        tc.setAccepted(false);
        if (request().body().asFormUrlEncoded() != null)
            if (request().body().asFormUrlEncoded().containsKey("message"))
                tc.setMessage(request().body().asFormUrlEncoded().get("message")[0]);
        tc.save();
        return redirect(routes.Application.matches());
    }

    @SubjectPresent
    public static Result acceptTextChat(Long tid) {
        User me = controllers.security.Account.getLocalUser(session());
        TextChat match = TextChat.find.byId(tid);
        if (me == null) return index();
        if (match == null) return notifications();
        match.setAccepted(true);
        match.save();
        return textChat(tid);
    }

    @SubjectPresent
    public static Result rejectTextChat(Long tid) {
        User me = controllers.security.Account.getLocalUser(session());
        TextChat match = TextChat.find.byId(tid);
        if (me == null) return index();
        if (match == null) return notifications();
        match.delete();
        return index();
    }

    @SubjectPresent
    public static Result textChat(Long tid) {
        User me = controllers.security.Account.getLocalUser(session());
        TextChat match = TextChat.find.byId(tid);
        if (me == null) return index();
        if (match == null) return notFound(views.html.index.render());
        return ok(views.html.textChat.render(me, match));
    }

    @SubjectPresent
    public static Result sendMessage(Long tid) {
        User me = controllers.security.Account.getLocalUser(session());
        TextChat match = TextChat.find.byId(tid);
        if (me == null) return index();
        if (match == null) return notFound(views.html.index.render());
        Message m = new Message();
        m.setSender(me);
        m.setRecipient(match.getOtherUser(me));
        if (request().body().asFormUrlEncoded() != null)
            if (request().body().asFormUrlEncoded().containsKey("message"))
                m.setText(request().body().asFormUrlEncoded().get("message")[0]);
        m.setTextChat(match);
        m.save();
        return redirect(routes.Application.textChat(tid));
    }

}
