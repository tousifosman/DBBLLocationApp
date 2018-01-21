package com.dbbl.tousifosman.dbbllocationapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import model.Location;

public class NearbyActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private final String TAG = "NearbyActivity";

    public interface ACTIVITY_RESPONSE {
        int SUCCESS = 1;
    };

    private model.Location[] branchLocations;
    private model.Location[] nearbyBranchLocations;
    private LatLng curLatLng;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        branchLocations = (model.Location[]) getIntent().getExtras().getSerializable("branchLocationArrayList");
        curLatLng = ((LatLng) getIntent().getExtras().get("curLatLng"));

        nearbyBranchLocations = utils.tools.findNearbyBranch(branchLocations, curLatLng);

        listView = findViewById(R.id.nearby_listView);
        listView.setAdapter(new NearbyBranchAdapter(nearbyBranchLocations, this));
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent();
        intent.putExtra("focusBranchLocation", nearbyBranchLocations[position]);

        setResult(ACTIVITY_RESPONSE.SUCCESS, intent);
        finish();
    }
}
