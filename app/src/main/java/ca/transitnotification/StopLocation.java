package ca.transitnotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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
    LocationListener locationListener;
    private double stopLat;
    private double stopLon;
    private String stopName;
    private String stopNumber;
    double selectedDistance = 100; // Hardcoded for now....
    long minTime = (long) 0.5;
    float minDistance = 50;
    private String TAG = "StopLocation";


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


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double newLat = location.getLatitude();
                double newLon = location.getLongitude();

                double distacneToStop = getDistance(newLat, stopLat, newLon, stopLon);
                Log.i(TAG, "Current distance is " + distacneToStop);
                if (distacneToStop < selectedDistance) {
                    sendNotification();
                    locationManager.removeUpdates(locationListener);
                }


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener );

        Button notificationButton = (Button) findViewById(R.id.cancel);
        //start an on click listner for canceling the notification
        notificationButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelNotification();
                    }
                }
        );

        //set the text view in the layout so that it will display the stop number and name
        TextView textView = (TextView) findViewById(R.id.DisplaySelectedStop);
        textView.setText("Will notify you at stop numnber " + stopNumber + " at " + stopName);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
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

    private double getDistance(double lat1, double lat2, double lon1, double lon2) {
        /**
         * Method to compute the distance between two locations
         */
        double R = 6371; // Radius of th earth in meters
        double dlat = (lat2 - lat1) * (Math.PI / 180.0);
        double dlon = (lon2 - lon1) * (Math.PI / 180.0);
        double a = Math.sin(dlat/2) * Math.sin(dlat/2) + Math.cos(lat2 * (Math.PI / 180.0)) *
                Math.cos(lat1 * (Math.PI / 180.0)) * Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.atan(Math.sqrt(a));
        return Math.abs(R * c * 1000);
    }

    private void sendNotification() {
        /**
         * Method to notify the user of there stop
         * TODO:
         *  -Set strings to android strings
         *  -get launcher Icon
         */

        Log.i(TAG, "Starting the notification");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        notificationBuilder.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL);

        notificationBuilder.setContentTitle("You are close to your stop!")
                .setContentText("Get ready to depart")
                .setTicker("Your stop is close!")
                .setSmallIcon(R.drawable.ic_launcher);

        Intent intent = new Intent(this, StopLocation.class);

        notificationBuilder.setContentIntent(PendingIntent.getActivity(this,0,intent,0));

        NotificationManager notficationManager =
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        int NOTIFCATION_ID = 0;
        notficationManager.notify(NOTIFCATION_ID, notificationBuilder.build());

    }

    private void cancelNotification() {
        Log.i(TAG, "Canceling the notification from hitting the button");
        locationManager.removeUpdates(locationListener);
        Intent backIntent = new Intent(this, MainActivity.class);
        startActivity(backIntent);
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


