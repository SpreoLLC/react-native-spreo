
package com.reactlibrary;

import android.graphics.Bitmap;
import android.graphics.PointF;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mlins.utils.GalleryObject;
import com.spreo.nav.interfaces.ILocation;
import com.spreo.nav.interfaces.IPoi;

import java.util.List;

public class PointSearchModel{

    @SerializedName("floor")
    @Expose
    String floor;
    @SerializedName("facilityID")
    @Expose
    String facilityID;
    @SerializedName("poi")
    @Expose
    IPoi poi;

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public IPoi getPoi() {
        return poi;
    }

    public void setPoi(IPoi poi) {
        this.poi = poi;
    }

    public String getFacilityID() {
        return facilityID;
    }

    public void setFacilityID(String facilityID) {
        this.facilityID = facilityID;
    }
}
