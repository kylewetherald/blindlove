package models;

import play.db.ebean.Model;
import play.utils.dao.BasicModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

/**
 * Created by Adam on 11/15/2014.
 */
@Entity
public class TextChat extends Model implements BasicModel<Long>, Dated {
    @Id 
    private Long id;
    public Long getKey() { return id; }
    public void setKey(Long id) { this.id = id; }

    @ManyToOne
    private User sender;

    @ManyToOne
    private User recipient;

    private Date created;

    public Date getCreated() { return created; }

    public void setCreated(Date d) { created = d; }

    public TextChat() { setCreated(new Date()); }

    public static final Model.Finder<Long, TextChat> find = new Model.Finder<>(Long.class, TextChat.class);

    private Boolean accepted;

    private String message;

    public Boolean getAccepted() { return accepted; }

    public void setAccepted(Boolean b) { accepted = b; }

    public User getSender() { return sender; }

    public void setSender(User u) { this.sender = u; }

    public User getRecipient() { return recipient; }

    public void setRecipient(User u) { recipient = u; }

    public String getMessage() { return message; }

    public void setMessage(String msg) { message = msg; }

    public User getOtherUser(User me) {
        return recipient.id == me.id ? sender : recipient;
    }

    public List<Message> getMessages() { return messages; };

    public void setMessages(List<Message> msgs) { messages = msgs; }

    @OneToMany
    private List<Message> messages;


}
