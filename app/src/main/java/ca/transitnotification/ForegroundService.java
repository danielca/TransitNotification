package ca.transitnotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import static java.lang.Double.*;

public class ForegroundService extends Service{
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double stopLat;
    private double stopLon;
    private double selectedDistance = 100; // Hardcoded for now....
    private static final String TAG = "StopNotification";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("deprecation") //Because these are annoying
    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        //get the information
        //String stopName = intent.getStringExtra("StopName");
        String stopNumber;
        stopNumber = intent.getStringExtra("StopNumber");
        stopLat = parseDouble(intent.getStringExtra("StopLat"));
        stopLon = parseDouble(intent.getStringExtra("StopLon"));

        //start the notification
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Notification foregroundNotification = new Notification(R.drawable.ic_launcher,
                "Will notify you of the stop soon",
                System.currentTimeMillis());
        Intent foregroundIntent = new Intent(this, StopLocation.class);
        foregroundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,foregroundIntent,0);
        foregroundNotification.setLatestEventInfo(this, "Stop Notification is running",
                "Will Notify you at stop  + " + stopNumber, pendingIntent);
        foregroundNotification.flags|=Notification.FLAG_NO_CLEAR;
        startForeground(1, foregroundNotification);


        startLocationListener();

        return(START_STICKY);
    }
    @Override
    public void onDestroy() {
        stopCurrentNotification();

    }

    private void stopCurrentNotification() {
        Log.i(TAG, "Canceling the notification from hitting the button");
        locationManager.removeUpdates(locationListener);
        Intent backIntent = new Intent(this, MainActivity.class);
        backIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        stopForeground(true);
        startActivity(backIntent);
    }

    @SuppressWarnings("deprecation") //Because these are annoying
    private void startLocationListener() {

        //start the location listener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double newLat = location.getLatitude();
                double newLon = location.getLongitude();
                Log.i(TAG,"Current Location lat " + newLat + " lon " + newLon + " selected lat " + stopLat + " lon " + stopLon);
                double distacneToStop = getDistance(newLat, stopLat, newLon, stopLon);
                Log.i(TAG, "Current distance is " + distacneToStop);
                if (distacneToStop < selectedDistance) {
                    Log.i(TAG, " In the selected radius");
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

        float minDistance = 25f; // Minumum selected distance
        long minTime = (long) 10 * 1000; //10 seconds in milliseconds
        Log.i(TAG,"Starting the location requests");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener );
    }

    /*private double getDistance(double lat1, double lat2, double lon1, double lon2) {
        /**
         * Method to compute the distance between two locations

        double R = 6371; // Radius of th earth in meters
        double dlat = (lat2 - lat1) * (Math.PI / 180.0);
        double dlon = (lon2 - lon1) * (Math.PI / 180.0);
        double a = Math.sin(dlat/2) * Math.sin(dlat/2) + Math.cos(lat2 * (Math.PI / 180.0)) *
                Math.cos(lat1 * (Math.PI / 180.0)) * Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.atan(Math.sqrt(a));
        //return Math.abs(R * c * 1000);
        return Math.abs(R*c);
    }
    **/
    private double getDistance(double lat1, double lat2, double lon1, double lon2) {
        double R = 6371000.0; // metres
        double theta1 = lat1 * (Math.PI / 180.0);
        double theta2 = lat2*(Math.PI / 180.0);
        double dtheta = (lat2-lat1)*(Math.PI / 180.0);
        double dlambda = (lon2-lon1)*(Math.PI / 180.0);

        double a = Math.sin(dtheta/2) * Math.sin(dtheta/2) +
                Math.cos(theta1) * Math.cos(theta2) *
                        Math.sin(dlambda/2) * Math.sin(dlambda/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return(R * c);
     }

    private void sendNotification() {
        /**
         * Method to notify the user of there stop
         * TODO:
         *  -Set strings to android strings
         *  -get launcher Icon
         */
        Log.i(TAG,"Stop is close, stopping the foreground");
        stopForeground(true);

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

        int NOTIFCATION_ID = 2;
        notficationManager.notify(NOTIFCATION_ID, notificationBuilder.build());

    }


}
