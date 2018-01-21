package utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import model.Location;

/**
 * Created by DBBL on 1/9/2018.
 */

public class tools {

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static double getEuclideanDistance(Location loc, LatLng latLng) {

        double t = Math.pow(loc.getLatitude() - latLng.latitude, 2);

        return Math.sqrt(
                Math.pow(loc.getLatitude() - latLng.latitude, 2)
                + Math.pow(loc.getLongitude() - latLng.longitude, 2)
        );
    }

    public static int findShortestLocation(Location[] locations, LatLng userLatLng) {

        int shortestLocationIndex = 0;
        double shortestLocationDistance = getEuclideanDistance(locations[0], userLatLng);
        for (int i = 1; i < locations.length; i++) {
            if (locations[i] == null) continue;
            double newLocationDistance = getEuclideanDistance(locations[i],userLatLng);
            if (newLocationDistance < shortestLocationDistance) {
                shortestLocationIndex = i;
                shortestLocationDistance = newLocationDistance;
            }
        }
        return shortestLocationIndex;
        //return  findShortestLocation(new LinkedList<>(Arrays.asList(locations)), userLatLng);
    }

    public static Location[] findNearbyBranch(Location[] locations, LatLng userLatLng){

        Location[] nearbyBranch = new Location[5];

        for (int i = 0; i < 5 && i < locations.length; i++) {
            int shortestLocationIndex = findShortestLocation(locations, userLatLng);
            nearbyBranch[i] = locations[shortestLocationIndex];
            locations[shortestLocationIndex] = null;
        }

        return nearbyBranch;
    }

}
