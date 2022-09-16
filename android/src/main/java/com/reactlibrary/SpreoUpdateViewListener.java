package com.reactlibrary;

import com.facebook.react.bridge.Callback;
import com.spreo.nav.interfaces.IPoi;

public interface SpreoUpdateViewListener {
    void updateFloorPicker(int presentFloorId, Callback callback);
}
