package com.dbbl.tousifosman.dbbllocationapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.DBBLLocationAPI;
import utils.PathJSONParser;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private Marker userMarker;
    private MarkerOptions userMarkerOptions;
    private LatLng curLatLng;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;

    private Marker branchMarker;
    private MarkerOptions branchMarkerOptions;
    private LatLng branchLatLng;

    private Polyline distancePolyline;
    private PolylineOptions distancePolylineOptions;

    private String[] zones;
    private ArrayAdapter<String> branchArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        /**
         * Make entire Search View clickable
         */
        final SearchView searchView = (SearchView)findViewById(R.id.svLocations);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

        zones = new String[]{"All", "Dhaka", "Chittaging"};

        branchArrayAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, zones);
        branchArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        Spinner spZones = (Spinner) findViewById(R.id.spZones);
        spZones.setAdapter(branchArrayAdapter);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET}, 1);

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i("Permission", "onCreate: No Location Permission");
            return;
        }
        initMap();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DBBLLocationAPI.getInstance(this).getAllZones();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0)
                    initMap();
                else
                    Toast.makeText(this, "Location Permission Not Given", Toast.LENGTH_LONG);
                break;
            default:
                Toast.makeText(this, "Permission Error", Toast.LENGTH_LONG);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        // Add a marker in Sydney and move the camera
        setBranchLocations();
        if (curLatLng != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curLatLng));
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i("Permission", "initMap: No Location Permission");
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.i("Location", "initMap: Location Found");
                if (location != null) {
                    mLocation = location;
                    updateLocation();
                }
            }
        });
    }

    private void updateLocation() {
        curLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());

        Log.i("Location", "In update location");


        if (userMarkerOptions == null) {

            userMarkerOptions = new MarkerOptions()
                    .title("You")
                    .icon(BitmapDescriptorFactory.fromBitmap(utils.Drawables.getBitmapFromVectorDrawable(this, R.drawable.ic_user)))
                    .position(curLatLng);
            userMarker = mMap.addMarker(userMarkerOptions);
            Log.d("Marker", "updateLocation: Marker Created");

        }

        if (mMap != null) {
            Log.d("Location", "updateLocation: Lat -> " + mLocation.getLatitude() + "Lng -> " + mLocation.getLongitude());
            userMarkerOptions.position(curLatLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curLatLng));
        }
    }

    private void setBranchLocations() {

        branchLatLng = new LatLng(23.793993, 90.404272);

        branchMarkerOptions = new MarkerOptions()
                .title("Bonani Branch")
                .icon(BitmapDescriptorFactory.fromBitmap(utils.Drawables.getBitmapFromVectorDrawable(this, R.drawable.ic_branch)))
                .position(branchLatLng);

        branchMarker = mMap.addMarker(branchMarkerOptions);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("Marker", "onMarkerClick: Marker Clicked");

        if (!marker.equals(userMarker)) {

            Log.d("Marker", "onMarkerClick: Drawing Polyline");
            //distancePolylineOptions = new PolylineOptions().add(curLatLng, branchLatLng);
            //distancePolyline = mMap.addPolyline(distancePolylineOptions);
            new ReadTask().execute(utils.Locations.getMapsApiDirectionsUrl(curLatLng, branchLatLng));

        }

        return false;
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                utils.HttpConnection http = new utils.HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            distancePolylineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                distancePolylineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                distancePolylineOptions.addAll(points);
                distancePolylineOptions.width(10);
                distancePolylineOptions.color(Color.BLUE);
            }
            if(distancePolyline != null)
                distancePolyline.remove();
            distancePolyline = mMap.addPolyline(distancePolylineOptions);
        }
    }

}
