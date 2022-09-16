package com.reactlibrary;


import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.mlins.nav.utils.MultiNavUtils;
import com.mlins.project.Campus;
import com.mlins.project.ProjectConf;
import com.mlins.utils.gis.Location;
import com.spreo.nav.enums.LocationMode;
import com.spreo.nav.interfaces.ILocation;

import org.json.JSONObject;


public class AdvancedLocation extends Location {

    public AdvancedLocation() {
    }

    public AdvancedLocation(ILocation iloc) {
        super(iloc);
    }

    public AdvancedLocation(String facility, String campus, float x, float y, float z) {
        super(facility, campus, x, y, z);
    }

    public AdvancedLocation(JSONObject locationJSON) {
        parse(locationJSON);
    }

    @Nullable
    public static LatLng getRealLatLng(ILocation location) {
        Campus campus = ProjectConf.getInstance().getCampus(location.getCampusId());

        LatLng latLng = null;

        if (location.getLocationType() == LocationMode.INDOOR_MODE) {
            latLng = MultiNavUtils.convertToLatlng(location.getX(), location.getY(), campus.getFacilityConf(location.getFacilityId()));
        } else if (location.getLocationType() == LocationMode.OUTDOOR_MODE) {
            latLng = new LatLng(location.getLat(), location.getLon());
        }
        return latLng;

    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj instanceof AdvancedLocation) {

            AdvancedLocation comparedLocation = (AdvancedLocation) obj;
            if (!isSameObject(this.getLocationType(), comparedLocation.getLocationType()))
                return false;
            else if ((this.getX() != comparedLocation.getX()))
                return false;
            else if (this.getLon() != comparedLocation.getLon())
                return false;
            else if (this.getY() != comparedLocation.getY())
                return false;
            else if (this.getZ() != comparedLocation.getZ())
                return false;
            else if (this.getLat() != comparedLocation.getLat())
                return false;
            else if (!isSameObject(this.getCampusId(), comparedLocation.getCampusId()))
                return false;
            else if (!isSameObject(this.getFacilityId(), comparedLocation.getFacilityId()))
                return false;
            else
                return true;
        } else
            return false;
    }

    public boolean equals(Object obj, double radius) {
        AdvancedLocation another = (AdvancedLocation) obj;
        if (equals(obj)) {
            return !(another.getX() - this.getX() >= radius || another.getY() - this.getY() >= radius);
        } else
            return false;
    }


    private boolean isSameObject(Object firstComparedObject, Object secondComparedObject) {
        if (firstComparedObject == secondComparedObject || (firstComparedObject != null && firstComparedObject.equals(secondComparedObject))) {
            return true;
        } else
            return false;
    }


}
