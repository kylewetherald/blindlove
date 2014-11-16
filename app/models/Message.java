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
public class Message extends Model implements BasicModel<Long> {

    @Id
    private Long id;
    public Long getKey() { return id; }
    public void setKey(Long id) { this.id = id; }

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

}
