package com.reactlibrary;

import com.spreo.sdk.view.SpreoDualMapView;

public class SpreoMapViewInstance  {

    SpreoDualMapView spreoDualMapView;
    int presentFloor = 0;
    private static final SpreoMapViewInstance ourInstance = new SpreoMapViewInstance();

    public static SpreoMapViewInstance getInstance() {
        return ourInstance;
    }

    private SpreoMapViewInstance() {

    }

    public int getPresentFloor() {
        return presentFloor;
    }

    public void setPresentFloor(int presentFloor) {
        this.presentFloor = presentFloor;
    }

    public void setSpreoDualMapView(SpreoDualMapView spreoDualMapView) {
        this.spreoDualMapView = spreoDualMapView;
    }

    public SpreoDualMapView getSpreoDualMapView() {
        return spreoDualMapView;
    }
}
