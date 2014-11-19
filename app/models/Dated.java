package models;

import java.util.Date;

/**
 * Created by Adam on 11/19/2014.
 */
public interface Dated {
    public Date getCreated();
    public void setCreated(Date d);
}
