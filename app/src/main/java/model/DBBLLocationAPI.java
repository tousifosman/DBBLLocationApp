package model;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dbbl.tousifosman.dbbllocationapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by DBBL on 1/7/2018.
 */

public class DBBLLocationAPI {

    private final String TAG = "DBBLLocationAPI";

    private static DBBLLocationAPI dbblLocationAPI;
    private Context context;

    private String apiURL;
    private String apiZoneURL;
    private String apiAllZoneURL;
    private String apiLocationURL;
    private String apiLocationURLFormat;

    private RequestQueue requestQueue;

    private DBBLLocationAPI(Context context){
        this.context = context;
        apiURL = context.getString(R.string.dbbl_location_api_url);
        apiZoneURL = apiURL + "/" + context.getString(R.string.dbbl_location_api_url_zone);
        apiAllZoneURL = apiZoneURL + "?get=all";
        apiLocationURL = apiURL + "/" + context.getString(R.string.dbbl_location_api_url_location);
        apiLocationURLFormat = apiLocationURL + "?get=%s&zone=%d";

        requestQueue = Volley.newRequestQueue(context);
    }

    public static DBBLLocationAPI getInstance(Context context){
        if (dbblLocationAPI == null)
            dbblLocationAPI = new DBBLLocationAPI(context);
        return dbblLocationAPI;
    }

    public void requestLocations(final model.Request<Location[]> request, String locationQuery, int zoneID){

        Log.d(TAG, String.format("requestLocations: %s", String.format(apiLocationURLFormat, locationQuery, zoneID)));

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, String.format(apiLocationURLFormat, locationQuery, zoneID), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        LinkedList<Location> locationList = new LinkedList<>();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                locationList.add(new Location(
                                        jsonObject.getInt("id"),
                                        jsonObject.getString("name"),
                                        jsonObject.getString("address"),
                                        jsonObject.getDouble("latitude"),
                                        jsonObject.getDouble("longitude"),
                                        jsonObject.getInt("zone"),
                                        jsonObject.getString("zone_name")
                                ));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        request.onResponse(locationList.toArray(new Location[0]), true);
                        Log.d(TAG, "onResponse: Locations Found");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        request.onResponse(null, false);
                        Log.e(TAG, "onErrorResponse: Locations not found");
                        error.printStackTrace();
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
    }

    public void requestAllZones(final model.Request<Zone[]> request){

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, apiAllZoneURL, null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        LinkedList<Zone> zoneList = new LinkedList<>();

                        for (int i = 0; i < response.length(); i++)
                            try {
                                zoneList.add(new Zone(response.getJSONObject(i).getInt("id"),
                                        response.getJSONObject(i).getString("name")));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        request.onResponse(zoneList.toArray(new Zone[0]), true);

                        Log.d(TAG, "onResponse: Zone found");
                    }
                }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    request.onResponse(null, false);
                    Log.e(TAG, "onResponse: Zone not found");
                    error.printStackTrace();
                }
            }
        );
        requestQueue.add(jsonArrayRequest);
    }
}
