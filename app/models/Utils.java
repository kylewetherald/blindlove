package models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Michael
 * Date: 01/07/14
 * Time: 09:58
 */
public class Utils {

    public static Date getDate(String dateString){
        // Convert string (YYMMDD) to date
        Date date = new Date();

        try {
            date = new SimpleDateFormat("YYMMDD", Locale.GERMAN).parse(dateString);
        } catch (ParseException e) {
            //e.printStackTrace();
        }

        return date;
    }
}
