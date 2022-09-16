package com.reactlibrary;

import android.graphics.PointF;

import com.spreo.nav.interfaces.IPoi;

public class SpreoMyTrip extends SpreoBaseLocationObj {


    public SpreoMyTrip(IPoi poi) {
        poiID = poi.getPoiID();
        poiuri = poi.getPoiuri();
        poidescription = poi.getpoiDescription();
        poiKeywords = poi.getKeyWordsAsString();
        location = poi.getLocation();
        poiNavigationType = poi.getPoiNavigationType();
        campusID = poi.getCampusID();
        facilityID = poi.getFacilityID();
        poiLatitude = poi.getPoiLatitude();
        poiLongitude = poi.getPoiLongitude();
    }

    public SpreoMyTrip() {

    }

    @Override
    public float getX() {
        float result = 0;
        if (location != null) {
            result = (float) location.getX();
        }
        return result;
    }

    @Override
    public float getY() {
        float result = 0;
        if (location != null) {
            result = (float) location.getY();
        }
        return result;
    }

    @Override
    public double getZ() {
        double result = 0;
        if (location != null) {
            result = location.getZ();
        }
        return result;
    }

    @Override
    public PointF getPoint() {
        PointF result = new PointF(getX(), getY());

        return result;
    }

    public double getPoiLatitude() {
        return poiLatitude;

    }

    @Override
    public void setPoiLatitude(double poiLatitude) {
        this.poiLatitude = poiLatitude;
    }

    public double getPoiLongitude() {
        return poiLongitude;

    }

    @Override
    public void setPoiLongitude(double poiLongitude) {
        this.poiLongitude = poiLongitude;
    }

}
