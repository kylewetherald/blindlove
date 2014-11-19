package services

import com.avaje.ebean.Expr
import models._

import scala.collection.JavaConversions._

/**
 * Created by Adam on 11/19/2014.
 */
object NotificationService {
  def getNotifications(mee:User):java.util.List[User.Notification] = {
    val me = User.find.byId(mee.id)
    (Message.find.where().eq("recipient.id", me.id).findList.map(msg => new User.Notification(msg, me)).toList :::
    TextChat.find.where().eq("recipient.id", me.id).findList.map(tc => new User.Notification(tc, me)).toList :::
    VideoChat.find.where().eq("recipient.id", me.id).findList.map(tc => new User.Notification(tc, me)).toList :::
    Match.find.where().eq("targetUser.id", me.id).eq("accepted", false).eq("rejected",false).findList.map(mtc => new User.Notification(mtc,me)).toList :::
    Reject.find.where().eq("rejectee.id", me.id).findList.map(mtc => new User.Notification(mtc,me)).toList ::: List.empty[User.Notification]).sortBy(_.receiptDate)
  }
  def getRecentNotifications(me:User):java.util.List[User.Notification] =
    getNotifications(me) filter(f => f.receiptDate.after(me.getLastLogin))
}
