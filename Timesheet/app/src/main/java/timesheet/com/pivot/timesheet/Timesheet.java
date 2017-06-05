package timesheet.com.pivot.timesheet;

/**
 * Created by DELL on 6/5/2017.
 */

public class Timesheet {
    String date="";
    String start="";
    String end="";
    boolean isOff=false;
    String reasonOff="";

    public String getDate() {
        return date;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public boolean isOff() {
        return isOff;
    }

    public String getReasonOff() {
        return reasonOff;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public void setOff(boolean off) {
        isOff = off;
    }

    public void setReasonOff(String reasonOff) {
        this.reasonOff = reasonOff;
    }
}
