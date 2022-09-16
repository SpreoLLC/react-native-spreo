package com.reactlibrary;

import com.spreo.nav.interfaces.IPoi;

public class SpreoHistory extends SpreoBaseLocationObj {
    public SpreoHistory(IPoi poi) {
        poiID = poi.getPoiID();
        poiuri = poi.getPoiuri();
        poidescription = poi.getpoiDescription();
        poiKeywords = poi.getKeyWordsAsString();
        location = poi.getLocation();
    }

    public SpreoHistory() {

    }
}
