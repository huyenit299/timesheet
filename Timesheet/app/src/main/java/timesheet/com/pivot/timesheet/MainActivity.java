package timesheet.com.pivot.timesheet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final int START = 1;
    public static final int END = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    private void writeFile(String time, int type,String dateInput) {
        try {
            Date date=new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String fileName = sdf.format(date);
            File file = new File(fileName + ".csv");
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(fileName);

            int rowNum = 0;
            System.out.println("Creating excel");
            String []title =new String[]{"日付", "出勤時間", "退社時間", "備考"};
            TimesheetDbHelper timesheetDbHelper =new TimesheetDbHelper(this);
            ArrayList<Timesheet>getTimesheetList =timesheetDbHelper.getAllTimesheet(fileName);
            for (Timesheet timesheet:getTimesheetList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;
                row.createCell(colNum++).setCellValue(timesheet.getDate());
                row.createCell(colNum++).setCellValue(timesheet.getStart());
                row.createCell(colNum++).setCellValue(timesheet.getEnd());
                row.createCell(colNum++).setCellValue(timesheet.getReasonOff());
            }
            try {
                FileOutputStream outputStream = new FileOutputStream(fileName);
                workbook.write(outputStream);
                workbook.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class TimesheetEntry implements BaseColumns {
        public static final String TABLE_NAME = "timesheet";
        public static final String COLUMN_DATE = "date_entry";
        public static final String COLUMN_START = "start";
        public static final String COLUMN_END = "end";
        public static final String IS_OFF = "is_off";
        public static final String NOTE = "note";
    }


    public class TimesheetDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "timesheet.db";
        String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TimesheetEntry.TABLE_NAME + " (" +
                        TimesheetEntry._ID + " INTEGER PRIMARY KEY," +
                        TimesheetEntry.COLUMN_DATE + " TEXT," +
                        TimesheetEntry.COLUMN_START +" TEXT," +
                        TimesheetEntry.COLUMN_END +" TEXT,"+
                        TimesheetEntry.IS_OFF+" INTEGER,"+
                        TimesheetEntry.NOTE + " TEXT)";

       String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TimesheetEntry.TABLE_NAME;

        public TimesheetDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        public long insertRecord(String date,String start,String end, boolean isOff, String note){
            // Gets the data repository in write mode
            SQLiteDatabase db = getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(TimesheetEntry.COLUMN_DATE,date);
            values.put(TimesheetEntry.COLUMN_START, start);
            values.put(TimesheetEntry.COLUMN_END, end);
            values.put(TimesheetEntry.IS_OFF,isOff);
            values.put(TimesheetEntry.NOTE,note);

            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insertWithOnConflict(TimesheetEntry.TABLE_NAME, null, values,SQLiteDatabase.CONFLICT_REPLACE);
            return newRowId;
        }

        public long updateRecord(String date,String start,String end, boolean isOff, String note) {
            SQLiteDatabase db = getReadableDatabase();

            // New value for one column
            ContentValues values = new ContentValues();
            values.put(TimesheetEntry.COLUMN_DATE,date);
            values.put(TimesheetEntry.COLUMN_START, start);
            values.put(TimesheetEntry.COLUMN_END, end);
            values.put(TimesheetEntry.IS_OFF,isOff);
            values.put(TimesheetEntry.NOTE,note);

            // Which row to update, based on the title
            String selection = TimesheetEntry.COLUMN_DATE + " LIKE ?";
            String[] selectionArgs = { date };

            int count = db.update(
                    TimesheetEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
            return count;
        }

        /*
         Getting record, if dateinput != "", will query the month inputted
         if dateInput="", query this month
          */
        public ArrayList<Timesheet> getAllTimesheet(String month) {
            ArrayList<Timesheet> timesheetList = new ArrayList<Timesheet>();
            String[]startEndDate = Helper.getStartEndDateOfMonth(month);
            String selectQuery = "SELECT * FROM " + TimesheetEntry.TABLE_NAME+" WHERE "+
                    TimesheetEntry.COLUMN_START+">="+startEndDate[0]+" AND "+
                    TimesheetEntry.COLUMN_END+"<="+startEndDate[1]+ " ODER BY "+
                    TimesheetEntry.COLUMN_DATE;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    Timesheet timesheet = new Timesheet();
                    timesheet.setDate(cursor.getString(cursor.getColumnIndex(TimesheetEntry.COLUMN_DATE)));
                    timesheet.setStart(cursor.getString(cursor.getColumnIndex(TimesheetEntry.COLUMN_START)));
                    timesheet.setEnd(cursor.getString(cursor.getColumnIndex(TimesheetEntry.COLUMN_END)));
                    timesheet.setOff(cursor.getInt(cursor.getColumnIndex(TimesheetEntry.IS_OFF))==1?true:false);
                    timesheet.setReasonOff(cursor.getString(cursor.getColumnIndex(TimesheetEntry.NOTE)));
                    timesheetList.add(timesheet);
                } while (cursor.moveToNext());
            }
            return timesheetList;
        }
    }
}
