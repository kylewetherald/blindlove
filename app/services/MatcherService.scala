package services

import com.avaje.ebean.Expr
import edu.cmu.lti.ws4j.impl.{Resnik, Lin}
import edu.cmu.lti.ws4j.util.StopWordRemover

import scala.collection.JavaConversions
import JavaConversions._
import edu.cmu.lti.lexical_db.NictWordNet
import models.{User, Match}

/**
 * Created by Adam on 11/15/2014.
 */
object MatcherService {
  val db = new NictWordNet
  val rc = new Resnik(db) // new Resnik Lesk WuPalmer JiangConrath
  def matchesJ(in:User):java.util.Map[User, java.lang.Float] = matches(in).map(f => f._1 -> new java.lang.Float(f._2))
  def matches(in:User) = {
      val alreadyMatched = Match.find.where().eq("targetUser.id", in.id).findList.map(_.getMatchedUser).toList :::
      Match.find.where().eq("matchedUser.id", in.id).findList.map(_.getTargetUser).toList
      User.find.all().filter(_.id != in.id).map { u => // could add user weightings easily here
        u -> List((u.bio, in.bio), (u.interests, in.interests), (u.educationalField, in.educationalField), (u.employmentField, in.employmentField)).foldLeft(0f) { (score, text) =>
          score + docSim(text._1, text._2)
        }
      }.toMap.filter(f => alreadyMatched.find(u => u.id == f._1.id).isEmpty)
  }
  val sw = StopWordRemover.getInstance
  def docSim(d1:String,d2:String) = {
    val x= sw.removeStopWords(d1.split(" ")).toList.foldLeft(0f) { (score, d1w) =>
      score + sw.removeStopWords(d2.split(" ")).toList.foldLeft(0f) { (score2, d2w) =>
        if (!d1w.equals(d2w)) {
          val y = rc.calcRelatednessOfWords(d1w, d2w)
          score2 + y.toFloat
        } else score2
      }
    }
    //play.Logger.info(d1 + "\n" + d2 + "\n\t----------### " + x + " ###----------")
    x
  }
}

