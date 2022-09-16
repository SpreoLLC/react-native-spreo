package com.reactlibrary;

import java.util.Arrays;
import java.util.List;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

public class SpreoPackage implements ReactPackage {
    private static ReactApplicationContext reactApplicationContext;
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        reactApplicationContext=reactContext;
        return Arrays.<NativeModule>asList(new SpreoModule(reactContext));

    }
public static ReactApplicationContext getContext(){
        return  reactApplicationContext;
}
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        reactApplicationContext=reactContext;
        MapViewManager mapViewManager =new MapViewManager();
//        MapViewCombinedManager mapViewCombinedManager =new MapViewCombinedManager();
//        mapViewManager.initMap(
//        );
        return Arrays.<ViewManager> asList(
                mapViewManager
//                mapViewCombinedManager
        );
    }
}
