package com.dbbl.tousifosman.dbbllocationapp;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import model.DBBLLocationAPI;
import model.Request;
import model.Zone;
import utils.PathJSONParser;
import utils.tools;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private final String TAG = "MapsActivity";

    private MapsActivity mapsActivityInstance;

    private GoogleMap mMap;

    private Marker userMarker;
    private MarkerOptions userMarkerOptions;
    private LatLng curLatLng;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;


    private model.Location[] branchLocations;
    private Marker[] branchMarkers;
    private MarkerOptions[] branchMarkerOptions;
    private LatLng focusBranchLatLng;
    private model.Location focusBranchLocation;
    private double focusBranchDistance;


    private Polyline distancePolyline;
    private PolylineOptions distancePolylineOptions;

    private Zone[] zonesList;
    private ArrayAdapter<Zone> branchArrayAdapter;
    private Spinner spZones;

    private ListView searchResultListView;
    private SearchBranchAdapter searchBranchAdapter;
    private ArrayList<model.Location> branchLocationArrayList;

    private LinearLayout lyMapConstraintLayout;
    private TextView tvNotifBName;
    private TextView tvNotifBAddr;
    private TextView tvNotifBDistance;
    private Button btnNotifBClose;

    private FloatingActionButton fabFocusUser;
    private FloatingActionButton fabFocusNearestBranch;

    /**
     * O is considered as the id for All zone
     * 'all' is the keyward for all locations
     */
    private int selectedZoneID = 0;
    private String branchSearch = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapsActivityInstance = this;
        setContentView(R.layout.activity_maps);

        initSPZones();

        /**
         * Make entire Search View clickable
         */
        final SearchView searchView = (SearchView) findViewById(R.id.svLocations);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchBranchAdapter.filter(newText);
                lyMapConstraintLayout.setVisibility(View.GONE);
                searchResultListView.setVisibility(View.VISIBLE);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchResultListView.setVisibility(View.GONE);
                spZones.setLayoutParams(new LinearLayout.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spZones.setLayoutParams(new LinearLayout.LayoutParams(0, AbsListView.LayoutParams.WRAP_CONTENT));
            }
        });

//        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
//
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus)
//                    spZones.setLayoutParams(new LinearLayout.LayoutParams(0, AbsListView.LayoutParams.WRAP_CONTENT));
//                else
//                    spZones.setLayoutParams(new LinearLayout.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
//            }
//        });

        searchResultListView = (ListView) findViewById(R.id.lvSearchResult);

        branchLocationArrayList = new ArrayList<>();
        searchBranchAdapter = new SearchBranchAdapter(this, branchLocationArrayList);
        searchResultListView.setAdapter(searchBranchAdapter);

        searchResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tools.hideKeyboard(mapsActivityInstance);
                searchResultListView.setVisibility(View.GONE);
                focusOnBranch((model.Location) searchBranchAdapter.getItem(position));
            }
        });

        /**
         * Initialize Map Notification
         */
        lyMapConstraintLayout = findViewById(R.id.lyMapNotification);
        tvNotifBName = findViewById(R.id.tvNotifBName);
        tvNotifBAddr = findViewById(R.id.tvNotifBAddr);
        tvNotifBDistance = findViewById(R.id.tvNotifBDistance);

        btnNotifBClose = findViewById(R.id.btnNotifBClose);
        btnNotifBClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lyMapConstraintLayout.setVisibility(View.GONE);
                //updateUserLocation();
            }
        });
        /**
         * Initialize FABs
         */
        fabFocusUser = findViewById(R.id.fabFocusUser);
        fabFocusUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserLocation();
            }
        });

        fabFocusNearestBranch = findViewById(R.id.fabFocusNearestBranch);
        fabFocusNearestBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curLatLng != null && branchLocations != null && branchLocations.length > 0)
                focusOnBranch(branchLocations[tools.findShortestLocation(branchLocations, curLatLng)]);
            }
        });



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
    protected void onResume() {
        super.onResume();
        requestZones();
        requestBranchLocations();
        updateUserLocation();
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
        requestBranchLocations();
        if (curLatLng != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curLatLng));
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //updateUserLocation();
    }

    private void updateUserLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.i("Location", "initMap: Location Found");
                if (location != null) {
                    mLocation = location;
                    //updateLocation();
                    Log.i("Location", "In update location");
                    curLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());

                    if (userMarkerOptions == null) {

                        userMarkerOptions = new MarkerOptions()
                                .title("You")
                                .icon(BitmapDescriptorFactory.fromBitmap(utils.Drawables.getBitmapFromVectorDrawable(mapsActivityInstance, R.drawable.ic_user)))
                                .position(curLatLng);
                        userMarker = mMap.addMarker(userMarkerOptions);
                        Log.d("Marker", "updateLocation: Marker Created");

                    }
                    focusOnUser();
                }
            }
        });
    }

    private void focusOnUser(){
        if (mMap != null) {
            userMarkerOptions.position(curLatLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curLatLng));
        }
    }

    private void initSPZones(){
        branchArrayAdapter = new ArrayAdapter<Zone>(this, R.layout.support_simple_spinner_dropdown_item);
        branchArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spZones = (Spinner) findViewById(R.id.spZones);

        spZones.setAdapter(branchArrayAdapter);

        spZones.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedZoneID = zonesList[position].getId();
                requestBranchLocations();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void requestZones() {
        DBBLLocationAPI.getInstance(this).requestAllZones(new Request<Zone[]>() {
            @Override
            public void onResponse(Zone[] resulList, boolean responseStatus) {
                if (responseStatus) {
                    zonesList = resulList;
                    updateZones();
                }
            }
        });
    }

    private  void updateZones() {
        branchArrayAdapter.clear();
        branchArrayAdapter.addAll(zonesList);
    }

    private void requestBranchLocations() {
        DBBLLocationAPI.getInstance(this).requestLocations(new Request<model.Location[]>() {

            @Override
            public void onResponse(model.Location[] result, boolean responseStatus) {
                branchLocations = result;
                updateBranchLocations();
            }
        }, branchSearch, selectedZoneID);
    }

    private void updateBranchLocations() {
        if (branchMarkers != null)
            for (Marker branMarker : branchMarkers)
                branMarker.remove();

        branchMarkerOptions = new MarkerOptions[branchLocations.length];
        branchMarkers = new Marker[branchLocations.length];
        for (int i = 0; i < branchLocations.length; i++) {
            LatLng branchLatLng = new LatLng(branchLocations[i].getLatitude(), branchLocations[i].getLongitude());
            branchMarkerOptions[i] = new MarkerOptions()
                    //.title(branchLocations[i].getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(utils.Drawables.getBitmapFromVectorDrawable(this, R.drawable.ic_branch)))
                    .position(branchLatLng);

            branchMarkers[i] = mMap.addMarker(branchMarkerOptions[i]);
            branchMarkers[i].setTag(branchLocations[i]);
        }
        branchLocationArrayList.clear();
        branchLocationArrayList.addAll(Arrays.asList(branchLocations));
    }

    private void focusOnBranch(model.Location location){
        if (location.getId() == 0)
            return;

        focusBranchLocation = location;
        focusBranchLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        //showMapNotification();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(focusBranchLatLng));
        new ReadTask().execute(utils.Locations.getMapsApiDirectionsUrl(curLatLng, focusBranchLatLng));

    }

    private void showMapNotification() {
        tvNotifBName.setText(focusBranchLocation.getName());
        tvNotifBAddr.setText(focusBranchLocation.getAddress());
        tvNotifBDistance.setText(String.format("%.2f m",focusBranchDistance/1000));
        lyMapConstraintLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("Marker", "onMarkerClick: Marker Clicked");

        if (!marker.equals(userMarker)) {

            Log.d("Marker", "onMarkerClick: Drawing Polyline");
            //distancePolylineOptions = new PolylineOptions().add(curLatLng, branchLatLng);
            //distancePolyline = mMap.addPolyline(distancePolylineOptions);

            focusOnBranch((model.Location) marker.getTag());

            //new ReadTask().execute(utils.Locations.getMapsApiDirectionsUrl(curLatLng, marker.getPosition()));

        } else {
            if (distancePolyline != null)
                distancePolyline.remove();
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

        PathJSONParser parser;

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                parser = new PathJSONParser();
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

            focusBranchDistance = parser.getDistance();

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
                distancePolylineOptions.color(getColor(R.color.colorPrimaryDark));
            }
            if(distancePolyline != null)
                distancePolyline.remove();
            distancePolyline = mMap.addPolyline(distancePolylineOptions);
            showMapNotification();
        }
    }

}
