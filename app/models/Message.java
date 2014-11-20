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
public class Message extends Model implements BasicModel<Long>, Dated {

    @Id
    private Long id;
    public Long getKey() { return id; }
    public void setKey(Long id) { this.id = id; }

    private Date created;

    public Date getCreated() { return created; }

    public void setCreated(Date d) { created = d; }

    public Message() { setCreated(new Date()); }

    @ManyToOne
    private User sender;

    @ManyToOne
    private User recipient;

    @ManyToOne
    private TextChat textChat;

    private String text;

    public String getText() { return text; }

    public void setText(String b) { text = b; }

    public User getSender() { return sender; }

    public void setSender(User u) { this.sender = u; }

    public User getRecipient() { return recipient; }

    public void setRecipient(User u) { recipient = u; }

    public TextChat getTextChat() { return textChat; }

    public void setTextChat(TextChat tc) { textChat = tc; }

    public static final Model.Finder<Long, Message> find = new Model.Finder<>(Long.class, Message.class);

}
