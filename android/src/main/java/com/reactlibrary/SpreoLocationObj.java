package com.reactlibrary;

import com.spreo.nav.enums.LocationMode;
import com.spreo.nav.interfaces.ILocation;

import org.json.JSONException;
import org.json.JSONObject;

public class SpreoLocationObj implements ILocation {

    public static final int TYPE_INTERNAL = 0;
    public static final int TYPE_EXTERNAL = 1;
    double x;
    double y;
    double z;
    private double lat;
    private double lon;
    private int type = TYPE_INTERNAL;
    private String campusId = null;
    private String facilityId = null;

    public SpreoLocationObj() {

    }

    @Override
    public JSONObject getAsJson() {
        JSONObject jsonObj = new JSONObject();
        try {
            try {
                jsonObj.put("type", type);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            try {
                jsonObj.put("campusid", campusId);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            try {
                jsonObj.put("facilityid", facilityId);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            try {
                jsonObj.put("x", x);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            try {
                jsonObj.put("y", y);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            try {
                jsonObj.put("z", z);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            try {
                jsonObj.put("lat", lat);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            try {
                jsonObj.put("lon", lon);
            } catch (Throwable t) {
                t.printStackTrace();
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }

        return jsonObj;
    }


    @Override
    public void parse(JSONObject jsonobject) {
        try {
            type = jsonobject.getInt("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            campusId = jsonobject.getString("campusid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            facilityId = jsonobject.getString("facilityid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            x = jsonobject.getDouble("x");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            y = jsonobject.getDouble("y");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            z = jsonobject.getDouble("z");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            lat = jsonobject.getDouble("lat");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            lon = jsonobject.getDouble("lon");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    public double getX() {
        return x;
    }

    @Override
    public void setX(double x) {
        this.x = x;

    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public double getLat() {
        return lat;
    }

    @Override
    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public double getLon() {
        return lon;
    }

    @Override
    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public LocationMode getLocationType() {
        LocationMode l = LocationMode.OUTDOOR_MODE;
        if (type == 0) {
            l = LocationMode.INDOOR_MODE;
        }
        return l;
    }

    @Override
    public String getCampusId() {
        return campusId;
    }

    @Override
    public void setCampusId(String campusId) {
        this.campusId = campusId;
    }

    @Override
    public String getFacilityId() {
        return facilityId;
    }

    @Override
    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    @Override
    public void setType(LocationMode locationMode) {
        this.type = locationMode.getValue();
    }

}
