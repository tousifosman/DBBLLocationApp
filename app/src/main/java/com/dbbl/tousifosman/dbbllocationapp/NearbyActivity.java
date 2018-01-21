package com.dbbl.tousifosman.dbbllocationapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import model.Location;

public class NearbyActivity extends AppCompatActivity {

    private model.Location[] branchLocations;
    private LatLng curLatLng;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        branchLocations = (model.Location[]) getIntent().getExtras().getSerializable("branchLocationArrayList");
        curLatLng = ((LatLng) getIntent().getExtras().get("curLatLng"));

        listView = findViewById(R.id.nearby_listView);
        listView.setAdapter(new NearbyBranchAdapter(branchLocations, this));

    }

}
