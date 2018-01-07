package model;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dbbl.tousifosman.dbbllocationapp.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

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

    private DBBLLocationAPI(Context context){
        this.context = context;
        apiURL = context.getString(R.string.dbbl_location_api_url);
        apiZoneURL = apiURL + "/" + context.getString(R.string.dbbl_location_api_url_zone);
        apiAllZoneURL = apiZoneURL + "?get=all";
    }

    public static DBBLLocationAPI getInstance(Context context){
        if (dbblLocationAPI == null)
            dbblLocationAPI = new DBBLLocationAPI(context);
        return dbblLocationAPI;
    }

    public HashMap<Integer, String> getAllZones(){

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, apiAllZoneURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "onResponse: Zone found");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onResponse: Zone not found");
                error.printStackTrace();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);

//        try {
//            String zonesJSONString = new utils.HttpConnection().readUrl(apiAllZoneURL);
//            Log.d(TAG, String.format("getAllZones: %s", zonesJSONString));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Log.e(TAG, String.format("getAllZones: %s", "Zone data cannot be fetched from server"));
        return null;
    }
}
