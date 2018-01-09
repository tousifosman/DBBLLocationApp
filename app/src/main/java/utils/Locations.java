package utils;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by tousifosman on 5/1/18.
 */

public class Locations {

    final static String TAG = "util.Locations";

    public static String getMapsApiDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        Log.d(TAG, String.format("getMapsApiDirectionsUrl: %s", url));

        return url;

    }

}
