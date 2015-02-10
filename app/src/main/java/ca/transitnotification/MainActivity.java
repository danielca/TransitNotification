package ca.transitnotification;
/**
 * Main Activyt
 *
 * So here is the main activity where the user enters the stop number and moves on to
 * StopLocation.java.
 * The database handling is also done here
 *
 * Changelog:
 *  Febuary 9:
 *      - Added in a listener for the enter button when entering a stop number.
 *
 * TODO:
 * - Add a fav list so user can save common stops
 * - Add in some preferences for distance, maybe ringtone?
 * - Add animation for the loading of the database
 * - Adds/IAP
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    private EditText enteredStopNumber;
    private DataBaseHelper stopDataBase;

    private static final String TAG = "StopNotification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Open Database
        stopDataBase = new DataBaseHelper(this);


        try {
            stopDataBase.createDataBase();
            Log.i(TAG, "checking to see if DB is needed");
        }catch (IOException e) {
            Log.e(TAG, "Unable to create the database");
        }
        try {
            stopDataBase.openDataBase();
            Log.i(TAG,"Opened the database");
        }catch (SQLiteException e) {
            Log.e(TAG, "Unable to open the database");
        }

        // initialize button and edit text
        Button notificationButton = (Button) findViewById(R.id.startNotification);
        enteredStopNumber = (EditText)findViewById(R.id.stopNumber);

        //handle when the user presses the enter button
        enteredStopNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    notificationClick();
                    return true;
                }
                return false;
            }
        });

        //start an on click listner
        notificationButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notificationClick();
                    }
                }
        );


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void noResults() {
        final Context appContext = MainActivity.this;
        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setMessage("No Stops found")
                .setCancelable(false)
                .setPositiveButton("Go BAck", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "no result found, showing alert dialog");
                        Intent gpsOptionsIntent = new Intent(appContext, MainActivity.class);
                        startActivity(gpsOptionsIntent);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void notificationClick() {

        String selectedStop = enteredStopNumber.getText().toString();

        //Query the database here
        try {
            Cursor result;
            String Query = String.format("SELECT * FROM Stop WHERE Stop_Num = %s", selectedStop);
            result = stopDataBase.getReadableDatabase().rawQuery(Query, null);

            Log.i(TAG,"Found this many results " + result.getCount());
            //Make sure the stop is found in the database
            if (result.getCount() < 1) {
                Log.i(TAG, "No results found");
                noResults();
            }

            while (result.moveToNext()) {
                //int id = result.getInt(0);
                // next values are for debug
                String stopNo = result.getString(1);
                String stopName = result.getString(2);
                String stopLat = result.getString(3);
                String stopLon = result.getString(4);

                // Create the stop
                Stop stop = new Stop();
                stop.setStopNumber(result.getString(1));
                stop.setName(result.getString(2));
                stop.setLat(result.getString(3));
                stop.setLon(result.getString(4));
                Log.d(TAG, "looked for " + selectedStop + " found stop " + stop.toString());

                //assert stop != null;

                //Start the new activity
                Intent intent = new Intent(this, StopLocation.class);
                intent.putExtra("StopName", stopName);
                intent.putExtra("StopNumber", stopNo);
                intent.putExtra("StopLat", stopLat);
                intent.putExtra("StopLon", stopLon);


                startActivity(intent);

            }
        }catch (Exception e) {
            Log.e(TAG, "Error");
            e.printStackTrace();
        }

    }

}



