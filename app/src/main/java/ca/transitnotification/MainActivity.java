package ca.transitnotification;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    private EditText enteredStopNumber;
    private DataBaseHelper stopDataBase;

    private static final String TAG = "StopNotificationMainActivity";

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
        //getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void notificationClick() {

        String selectedStop = enteredStopNumber.getText().toString();

        //Query the database here
        try {
            Cursor result;
            String Query = String.format("SELECT * FROM Stop WHERE Stop_Num = %s", selectedStop);
            result = stopDataBase.getReadableDatabase().rawQuery(Query, null);
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



