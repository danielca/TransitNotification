package ca.transitnotification;

/**
 * Stop Location Class
 *
 * This class handles the UI, and stares the foreground process for the lcoation tracking
 *
 * Created By:Casey Daniel
 *
 * Changelog:
 *  Febuary 9:
 *      -Moved location tracking to foreground service located in ForegroundService.java
 *
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import static java.lang.Double.*;


public class StopLocation extends ActionBarActivity {
    LocationManager locationManager;
    double stopLat;
    double stopLon;
    String stopName;
    String stopNumber;
    private static final String TAG = "StopNotification";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_notification);

        Log.d(TAG, "Starting the stop location activity");

        //Stop stop = null;
        //get the stop
        Intent intent;
        intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            //stop = intent.getParcelableExtra("SelectedStop");
            stopLat = parseDouble(extras.getString("StopLat"));
            stopLon = parseDouble(extras.getString("StopLon"));
            stopName = extras.getString("StopName");
            stopNumber = extras.getString("StopNumber");

        } else {
            Log.e(TAG, "Unable to find the stop object, going back to main activity");
            Toast.makeText(getApplicationContext(), "Something went wrong please try again"
            ,Toast.LENGTH_LONG).show();
            Intent backIntent = new Intent(this, MainActivity.class);
            startActivity(backIntent);
        }
        Log.i(TAG,"Found Name " + stopName + " number " + stopNumber + " at " + stopLat + " " + stopLon);
        //get the data from the stop object
        /*
        My eventual goal is to utilize the stop class, for now this is ignored
        assert stop != null;
        Log.i(TAG, stop.toString());
        stopLat = Double.parseDouble(stop.getLat());
        stopLon = Double.parseDouble(stop.getLon());
        stopName = stop.getStopName();
        stopNumber = stop.getStopNumber();
        */

        //Log.i(TAG,"Found stop " + stop.toString());


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //Check to see if GPS is enabled
        Log.i(TAG, "Checking GPS provider");
        boolean GPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.i(TAG, "GPS enabled: " + GPSEnabled);
        if (!GPSEnabled) {
            showSettingsAlert();
        }

        Log.i(TAG,"Starting the foreground service");
        //start the foreground service
        Intent startForeground = new Intent(this, ForegroundService.class);
        startForeground.putExtra("StopName", stopName);
        startForeground.putExtra("StopNumber", stopNumber);
        startForeground.putExtra("StopLat", String.valueOf(stopLat));
        startForeground.putExtra("StopLon", String.valueOf(stopLon));
        startService(startForeground);
        Log.i(TAG, "Started the foreground service");

        Button notificationButton;
        notificationButton = (Button) findViewById(R.id.cancel);
        //start an on click listner for canceling the notification
        notificationButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopService(new Intent(getApplicationContext(), ForegroundService.class));
                    }
                }
        );

        //set the text view in the layout so that it will display the stop number and name
        TextView textView = (TextView) findViewById(R.id.DisplaySelectedStop);
        textView.setText("Will notify you at stop number " + stopNumber + " at " + stopName);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    //    getMenuInflater().inflate(R.menu.menu_stop_location, menu);
    //    return true;
    //}

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





    public void showSettingsAlert() {
        /*
        This method is built to ask the user to enable GPS if it's currently turned off.
         */

        //final Context appContext = this.getApplicationContext();
        final Context appContext = StopLocation.this;
        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setMessage("GPS is not enabled and is needed. Do you want to enable it now?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Going to go enable GPS, be back soon!");
                        Intent gpsOptionsIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);
                    }
                })
                .setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG,"User did not want to enable GPS, going back to main activity");
                        Intent backIntent = new Intent(appContext, MainActivity.class);
                        startActivity(backIntent);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();


    }


}


