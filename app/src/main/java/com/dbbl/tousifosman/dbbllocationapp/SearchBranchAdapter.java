package com.dbbl.tousifosman.dbbllocationapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import model.Location;

/**
 * Created by DBBL on 1/8/2018.
 */

public class SearchBranchAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<Location> locationArrayList;
    private ArrayList<Location> filteredLocationArrayList;

    public SearchBranchAdapter(Context mContext, ArrayList<Location> locationArrayList) {
        this.mContext = mContext;
        this.locationArrayList = locationArrayList;
        filteredLocationArrayList = new ArrayList<>();
        inflater = LayoutInflater.from(mContext);
    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        if (filteredLocationArrayList == null)
            return 0;
        return filteredLocationArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredLocationArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.search_result_item, null);
            // Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(
                ((Location) this.getItem(position)).getName()
                + " - " + ((Location) this.getItem(position)).getAddress());
        return view;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        filteredLocationArrayList.clear();
        if (charText.length() == 0) {
            filteredLocationArrayList.addAll(locationArrayList);
        } else {
            for (Location location : locationArrayList) {
                if (location.toString().toLowerCase(Locale.getDefault()).contains(charText)) {
                    filteredLocationArrayList.add(location);
                }
            }
        }
        if (filteredLocationArrayList.size() <= 0)
           filteredLocationArrayList.add(new Location(0, "No result found", "",0,0, -1, ""));
        notifyDataSetChanged();
    }

    public void resetSearch() {
        filteredLocationArrayList = (ArrayList<Location>) locationArrayList.clone();
    }

}
