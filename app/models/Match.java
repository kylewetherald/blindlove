package models;

import play.db.ebean.Model;
import play.utils.dao.BasicModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by Adam on 11/15/2014.
 */
@Entity
public class Match extends Model implements BasicModel<Long> {
    @Id
    private Long id;
    public Long getKey() { return id; }
    public void setKey(Long id) { this.id = id; }

    public static final Model.Finder<Long, Match> find = new Model.Finder<>(Long.class, Match.class);

    @ManyToOne
    private User matchedUser;

    private Boolean accepted;

    public Boolean getAccepted() { return accepted; }

    public void setAccepted(Boolean b) { accepted = b; }

    private Boolean rejected;

    public Boolean getRejected() { return rejected; }

    public void setRejected(Boolean b) { rejected = b; }

    @ManyToOne
    private User targetUser;

    public User getMatchedUser() { return matchedUser; }

    public void setMatchedUser(User u) { this.matchedUser = u; }

    public User getTargetUser() { return targetUser; }

    public void setTargetUser(User u) { targetUser = u; }

}
