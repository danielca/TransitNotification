package ca.tansitnotificaion;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class StopLocation extends ActionBarActivity {
    LocationManager locationManager;
    LocationListener locationListener;
    double stopLat;
    double stopLon;
    double selectedDistance = 100; // Hardcoded for now....
    long minTime = (long) 0.5;
    float minDistance = 50;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_location);

        //get the stop
        Intent extras = getIntent();
        if (extras != null) {
            stopLat = Double.parseDouble(extras.getStringExtra("StopLat"));
            stopLon = Double.parseDouble(extras.getStringExtra("StopLon"));
        } else {
            // Something here
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double newLat = location.getLatitude();
                double newLon = location.getLongitude();

                double distacneToStop = getDistance(newLat, stopLat, newLon, stopLon);
                if (distacneToStop < selectedDistance) {
                    //Notification goes here
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stop_location, menu);
        return true;
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

    private double getDistance(double lat1, double lat2, double lon1, double lon2) {
        double R = 6371; // Radius of th earth in meters
        double dlat = (lat2 - lat1) * (Math.PI / 180.0);
        double dlon = (lon2 - lon1) * (Math.PI / 180.0);
        double a = Math.sin(dlat/2) * Math.sin(dlat/2) + Math.cos(lat2 * (Math.PI / 180.0)) *
                Math.cos(lat1 * (Math.PI / 180.0)) * Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.atan(Math.sqrt(a));
        return Math.abs(R * c * 1000);
    }

}


