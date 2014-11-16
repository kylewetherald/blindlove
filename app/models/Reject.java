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
public class Reject extends Model implements BasicModel<Long> {

    @Id
    private Long id;
    public Long getKey() { return id; }
    public void setKey(Long id) { this.id = id; }

    public static final Model.Finder<Long, Reject> find = new Model.Finder<>(Long.class, Reject.class);

    @ManyToOne
    private User rejector;

    @ManyToOne
    private User rejectee;

    public User getRejector() { return rejector; }

    public void setRejector(User u) { this.rejector = u; }

    public User getRejectee() { return rejectee; }

    public void setRejectee(User u) { rejectee = u; }

}
