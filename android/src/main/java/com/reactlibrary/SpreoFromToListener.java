package com.reactlibrary;

import com.spreo.nav.interfaces.IPoi;

public interface SpreoFromToListener {
    void navigate(IPoi from, IPoi to);
    void navigationCanceled();
    void presentDestination(IPoi destination);
}
