package timesheet.com.pivot.timesheet;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
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

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
//            bw.write(content);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFile()
    {
        BufferedReader br = null;
        try {
            Date date=new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String fileName = sdf.format(date);
            String sCurrentLine;
            br = new BufferedReader(new FileReader(fileName+".csv"));
            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
    }
}
