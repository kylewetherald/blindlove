package models;

import play.db.ebean.Model;
import play.utils.dao.BasicModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Adam on 11/15/2014.
 */
@Entity
public class VideoChat extends Model implements BasicModel<Long>, Dated {

    @Id
    private Long id;
    public Long getKey() { return id; }
    public void setKey(Long id) { this.id = id; }

    @ManyToOne
    private User sender;

    @ManyToOne
    private User recipient;

    private Boolean accepted;

    private String message;

    private Date created;

    public Date getCreated() { return created; }

    public void setCreated(Date d) { created = d; }

    public VideoChat() { setCreated(new Date()); }

    public Boolean getAccepted() { return accepted; }

    public void setAccepted(Boolean b) { accepted = b; }

    public User getSender() { return sender; }

    public void setSender(User u) { this.sender = u; }

    public User getRecipient() { return recipient; }

    public void setRecipient(User u) { recipient = u; }

    public String getMessage() { return message; }

    public void setMessage(String msg) { message = msg; }

    public static final Model.Finder<Long, VideoChat> find = new Model.Finder<>(Long.class, VideoChat.class);

}
