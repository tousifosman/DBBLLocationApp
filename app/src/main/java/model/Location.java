package model;

import java.io.Serializable;

/**
 * Created by DBBL on 1/8/2018.
 */

public class Location implements Serializable{

    private int id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private int zone;
    private String zone_name;


    public Location(int id, String name, String address, double latitude, double longitude, int zone, String zone_name) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.zone = zone;
        this.zone_name = zone_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public String getZone_name() {
        return zone_name;
    }

    public void setZone_name(String zone_name) {
        this.zone_name = zone_name;
    }

    @Override
    public String toString() {
        return name + " " + address;
    }
}
