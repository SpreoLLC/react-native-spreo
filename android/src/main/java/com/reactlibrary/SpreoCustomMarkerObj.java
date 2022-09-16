package com.reactlibrary;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.spreo.interfaces.ICustomMarker;
import com.spreo.nav.enums.LocationMode;
import com.spreo.nav.interfaces.ILocation;
import com.spreo.nav.interfaces.IPoi;

public class SpreoCustomMarkerObj implements ICustomMarker {
    private Bitmap icon = null;
    private String id = null;
    private String CampusID = null;
    private String facilityID = null;
    private LocationMode locationType = null;
    private LatLng latlng = null;
    private int z = 0;
    private float X = 0;
    private float Y = 0;

    public SpreoCustomMarkerObj(IPoi poi, String id, Bitmap icon) {
        this.id = id;
        this.icon = icon;
        CampusID = poi.getCampusID();
        facilityID = poi.getFacilityID();
        locationType = poi.getLocation().getLocationType();
        latlng = new LatLng(poi.getPoiLatitude(), poi.getPoiLongitude());
        z = (int)poi.getZ();
        X = poi.getX();
        Y = poi.getY();
    }

    public SpreoCustomMarkerObj(ILocation loc, String id, Bitmap icon) {
        this.id = id;
        this.icon = icon;
        CampusID = loc.getCampusId();
        facilityID = loc.getFacilityId();
        locationType = loc.getLocationType();
        latlng = new LatLng(loc.getLat(), loc.getLon());
        z = (int)loc.getZ();
        X = (float)loc.getX();
        Y = (float)loc.getY();
    }

    @Override
    public void SetIcon(Bitmap bitmap) {
        icon = bitmap;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String s) {
            id = s;
    }

    @Override
    public String getProjectId() {
        return null;
    }

    @Override
    public void setProjectId(String s) {

    }

    @Override
    public String getCampusId() {
        return CampusID;
    }

    @Override
    public void setCampusId(String s) {

    }

    @Override
    public String getFacilityId() {
        return facilityID;
    }

    @Override
    public void setFacilityId(String s) {

    }

    @Override
    public LocationMode getLocationMode() {
        return locationType;
    }

    @Override
    public void setLocationMode(LocationMode locationMode) {

    }

    @Override
    public float getX() {
        return X;
    }

    @Override
    public void setX(float v) {

    }

    @Override
    public float getY() {
        return Y;
    }

    @Override
    public void setY(float v) {

    }

    @Override
    public int getFloor() {
        return z;
    }

    @Override
    public void setFloor(int i) {

    }

    @Override
    public LatLng getLatLng() {
        return latlng;
    }

    @Override
    public void setLatLng(LatLng latLng) {

    }

    @Override
    public Bitmap getIcon() {
        return icon;
    }
}
