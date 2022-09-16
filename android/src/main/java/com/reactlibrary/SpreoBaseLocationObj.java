package com.reactlibrary;

import android.graphics.Bitmap;
import android.graphics.PointF;

import com.mlins.utils.GalleryObject;
import com.spreo.nav.interfaces.ILocation;
import com.spreo.nav.interfaces.IPoi;
import com.spreo.sdk.poi.PoisUtils;
import com.spreo.sdk.setting.SettingsProvider;

import org.json.JSONObject;

import java.util.List;

public class SpreoBaseLocationObj implements IPoi {

    public String poiuri = "";
    public String poidescription = "";
    public String poiKeywords = "";
    public String poiID = "";
    public ILocation location = null;
    public String poiNavigationType = "";
    public String campusID = "";
    public String facilityID = "";
    public double poiLatitude = 0;
    public double poiLongitude = 0;
    private boolean visible = true;
    public Bitmap poiIcon;

    @Override
    public List<String> getPoiKeywords() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPoiKeywords(List<String> poiKeywords) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPoiuri() {
        // TODO Auto-generated method stub
        return poiuri;
    }

    @Override
    public void setPoiuri(String poiuri) {
        this.poiuri = poiuri;

    }

    @Override
    public List<String> getPoitype() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPoitype(String poitype) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getpoiDescription() {
        // TODO Auto-generated method stub
        return poidescription;
    }

    @Override
    public void setpoiDescription(String description) {
        // TODO Auto-generated method stub
        poidescription = description;
    }

    @Override
    public PointF getPoint() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPoint(PointF point) {
        // TODO Auto-generated method stub

    }

    @Override
    public double getZ() {
        return location.getZ();
    }

    @Override
    public void setZ(double z) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setUrl(String url) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getDetails() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDetails(String details) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isShowPoiOnMap() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setShowPoiOnMap(boolean showPoiOnMap) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isShowPoiOnSearches() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setShowPoiOnSearches(boolean showPoiOnSearches) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isShowPoiBubble() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setShowPoiBubble(boolean showPoiBubble) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isPoiPlayMultyMedia() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setPoiPlayMultyMedia(boolean poiPlayMultyMedia) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPoiID() {
        // TODO Auto-generated method stub
        return poiID;
    }

    @Override
    public void setPoiID(String poiID) {
        this.poiID = poiID;

    }

    @Override
    public String getPoiNavigationType() {
        return poiNavigationType;
    }

    @Override
    public void setPoiNavigationType(String poiNavigationType) {
        this.poiNavigationType = poiNavigationType;
    }

    @Override
    public double getPoiLatitude() {
        // TODO Auto-generated method stub
        return location != null ? location.getLat() : -1;
    }

    @Override
    public void setPoiLatitude(double poiLatitude) {
        if (location != null) {
            location.setLat(poiLatitude);
        }
    }

    @Override
    public double getPoiLongitude() {
        // TODO Auto-generated method stub
        return location != null ? location.getLon() : -1;
    }

    @Override
    public void setPoiLongitude(double poiLongitude) {
        if (location != null) {
            location.setLon(poiLongitude);
        }

    }

    @Override
    public boolean isShowOnZoomLevel() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setShowOnZoomLevel(boolean showOnZoomLevel) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getX() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getY() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isPoiClickAble() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setPoiClickAble(boolean poiClickAble) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isInstructionsParticipate() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setInstructionsParticipate(boolean instructionsParticipate) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPoiofficeinstuctions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPoiofficeinstuctions(String poiofficeinstuctions) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isPoishowincategory() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setPoishowincategory(boolean poishowincategory) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getMediaurl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMediaurl(String mediaurl) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getKeyWordsAsString() {
        // TODO Auto-generated method stub
        return poiKeywords;
    }

    @Override
    public ILocation getLocation() {
        // TODO Auto-generated method stub
        return location;
    }

    @Override
    public void setLocation(ILocation location) {
        this.location = location;

    }

    @Override
    public List<String> getActivehours() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setActivehours(List<String> poiActivehours) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getPhone2hours() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPhone2hours(List<String> Phone2hours) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getPhone1() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPhone1(List<String> Phone1) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getPhone2() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPhone2(List<String> Phone2) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getEmailaddress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setEmailaddress(String Emailaddress) {
        // TODO Auto-generated method stub
    }

    public String getAssociatedParkingId() {
        // TODO Auto-generated method stub
        return null;
    }



    public void parse(JSONObject jsonobject) {

        try {
            poiID = jsonobject.getString("poiID");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            poiuri = jsonobject.getString("poiuri");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            poidescription = jsonobject.getString("poidescription");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            poiKeywords = jsonobject.getString("poiKeywords");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            JSONObject loc = jsonobject.getJSONObject("location");
            location = new SpreoLocationObj();
            location.parse(loc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            poiNavigationType = jsonobject.getString("poiNavigationType");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            campusID = jsonobject.getString("campusID");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            facilityID = jsonobject.getString("facilityID");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            poiLatitude = jsonobject.getDouble("poiLatitude");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            poiLongitude = jsonobject.getDouble("poiLongitude");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public JSONObject getAsJson() {
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("poiID", poiID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            jsonObj.put("poiuri", poiuri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            jsonObj.put("poidescription", poidescription);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            jsonObj.put("poiKeywords", poiKeywords);
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            jsonObj.put("location", location.getAsJson());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            jsonObj.put("poiNavigationType", poiNavigationType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            jsonObj.put("campusID", campusID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            jsonObj.put("facilityID", facilityID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            jsonObj.put("poiLatitude", poiLatitude);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            jsonObj.put("poiLongitude", poiLongitude);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    public Bitmap getIcon() {
        IPoi poi;
        if (!poiID.contains("_") && SettingsProvider.getInstance().getAppLanguage().equals("spanish")) {
            String z = Double.toString(getZ());
            poi = PoisUtils.getPoiById(poiID + "_" + z.substring(0, z.length() - 2));
        } else if (poiID.contains("_") && SettingsProvider.getInstance().getAppLanguage().equals("english")) {
            poi = PoisUtils.getPoiById(poiID.substring(0,poiID.length()-2));
        } else {
            poi = PoisUtils.getPoiById(poiID);
        }
        return poi.getIcon();
//        IPoi poi = SpreoSearchDataHolder.getInstance().getPoiById(poiID);
//        return poi.getIcon();
    }

    /**
     * @deprecated
     */
    public void setIcon(Bitmap icon) {

    }

    @Override
    public List<GalleryObject> getGallery() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addGalleryImage(GalleryObject g) {
        // TODO Auto-generated method stub

    }

    @Override
    public GalleryObject getHeadImage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setHeadImage(GalleryObject g) {
        // TODO Auto-generated method stub

    }

    @Override
    public void recycleGallery() {
        // TODO Auto-generated method stub

    }

    @Override
    public String getCampusID() {
        return campusID;
    }

    @Override
    public void setCampusID(String campusID) {
        this.campusID = campusID;
    }

    @Override
    public String getFacilityID() {
        return facilityID;
    }

    @Override
    public void setFacilityID(String facilityID) {
        this.facilityID = facilityID;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;

    }

    @Override
    public boolean isNavigable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setLabelVisibility(boolean visible) {

    }

    @Override
    public boolean shouldDisplayLabel() {
        return false;
    }


}
