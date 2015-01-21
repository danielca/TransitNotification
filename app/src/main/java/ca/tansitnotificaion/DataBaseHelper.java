package ca.tansitnotificaion;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Casey on 14/12/2014.
 * This class is required for SQLite databases. Custom methods have been used to move my
 * pre-created database from the assets folder to the database folder.
 *
 * Look into removing the /data/data hard code.
 */

public class DataBaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH = "/data/data/ca.stopnotification/databases/";
    private static String DB_NAME = "stops.db";
    private SQLiteDatabase myDataBase;
    private final Context myContext;
    private static String TAG = "StopNotificationMainActivity";

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{
        boolean dbExist = checkDataBase();
        Log.i(TAG, " In the create database method " + dbExist);

        if(dbExist) {
            // do nothing
            Log.i(TAG, "found database?");
        }else{
            this.getReadableDatabase();
            try{
                copyDataBase();
                Log.i(TAG, "moving DB");
            } catch(IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }catch (SQLiteException e) {
            // Database does not yet exists
        }

        if(checkDB != null) {
            checkDB.close();
        }

        return checkDB !=null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException {
        InputStream inputStream = myContext.getAssets().open(DB_NAME);

        String outFileName = DB_PATH + DB_NAME;
        Log.i(TAG, "Moving database to " + outFileName);

        OutputStream outputStream = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while((length=inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
    }

    public void openDataBase() throws SQLiteException {
        String path = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close() {
        if(myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Not needed as database is created elsewhere
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Not needed as database is created elsewhere
    }

}
