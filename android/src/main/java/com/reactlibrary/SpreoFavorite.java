package com.reactlibrary;

import com.spreo.nav.interfaces.IPoi;

public class SpreoFavorite extends SpreoBaseLocationObj {

    public SpreoFavorite() {

    }

    public SpreoFavorite(IPoi poi) {
        poiID = poi.getPoiID();
        poiuri = poi.getPoiuri();
        poidescription = poi.getpoiDescription();
        poiKeywords = poi.getKeyWordsAsString();
        location = poi.getLocation();
        poiIcon = poi.getIcon();
    }


}
