package com.reactlibrary;

import com.spreo.nav.interfaces.IPoi;

public interface SpreoSearchClickListener {
    void onGoClickListener(IPoi poi);
    void onItemClickListener(IPoi poi);
    void onShowClickListener(IPoi poi);
    void onParkingClickListener();
    void menuOpened();
    void typeStarted();
    void onCampusSelectionClick();
}
