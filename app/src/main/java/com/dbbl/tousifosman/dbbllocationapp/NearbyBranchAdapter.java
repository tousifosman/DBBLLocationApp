package com.dbbl.tousifosman.dbbllocationapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import model.Location;

/**
 * Created by osman.tousif on 1/21/2018.
 */

public class NearbyBranchAdapter extends BaseAdapter {

    private model.Location[] locations;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public NearbyBranchAdapter(Location[] locations, Context mContext) {
        this.locations = locations;
        this.mContext = mContext;
        this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return locations.length;
    }

    @Override
    public Location getItem(int position) {
        return locations[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View nearbyItemView = mLayoutInflater.inflate(R.layout.nearby_item, null);
        ((TextView)nearbyItemView.findViewById(R.id.tvNotifBName)).setText(getItem(position).getName());
        ((TextView)nearbyItemView.findViewById(R.id.tvNotifBAddr)).setText(getItem(position).getAddress());
        return nearbyItemView;
    }
}
