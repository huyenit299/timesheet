package timesheet.com.pivot.timesheet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by DELL on 6/5/2017.
 */

public class Helper {
    public final static String DATE_FORMAT = "yyyy-MM-dd";
    public static String[] getStartEndDateOfMonth(String dateInput){
        String []startEnd = new String[2];
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String []dateStr = dateInput.split("-");
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        try {
            year=Integer.parseInt(dateStr[0]);
            month=Integer.parseInt(dateStr[1]);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        int day = 1;
        c.set(year, month, day);
        int numOfDaysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        startEnd[0] = sdf.format(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, numOfDaysInMonth - 1);
        startEnd[1] =sdf.format(c.getTime());
        return startEnd;
    }
}
