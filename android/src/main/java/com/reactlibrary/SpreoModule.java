package com.reactlibrary;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.mlins.aStar.FloorNavigationPath;
import com.mlins.aStar.NavigationPath;
import com.mlins.custom.markers.CustomMarkersManager;
import com.mlins.dualmap.DualMapView;
import com.mlins.dualmap.RouteCalculationHelper;
import com.mlins.instructions.Instruction;
import com.mlins.locationutils.LocationFinder;
import com.mlins.project.Campus;
import com.mlins.project.ProjectConf;
import com.mlins.res.setup.GalleryListener;
import com.mlins.res.setup.GalleryUpdateStatus;
import com.mlins.utils.FacilityConf;
import com.mlins.utils.GalleryObject;
import com.mlins.utils.MathUtils;
import com.mlins.utils.PropertyHolder;
import com.mlins.utils.SortingPoiUtil;
import com.spreo.enums.ResUpdateStatus;
import com.spreo.interfaces.ConfigsUpdaterListener;
import com.spreo.interfaces.ICustomMarker;
import com.spreo.nav.enums.LocationMode;
import com.spreo.nav.enums.MapRotationType;
import com.spreo.nav.interfaces.ILocation;
import com.spreo.nav.interfaces.INavInstruction;
import com.spreo.nav.interfaces.IPoi;
import com.spreo.sdk.data.SpreoDataProvider;
import com.spreo.sdk.data.SpreoResourceConfigsUtils;
import com.spreo.sdk.location.SpreoLocationProvider;
import com.spreo.sdk.poi.PoiCategory;
import com.spreo.sdk.poi.PoisUtils;
import com.spreo.sdk.setting.SettingsProvider;
import com.spreo.sdk.view.SpreoDualMapView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class SpreoModule extends ReactContextBaseJavaModule implements ConfigsUpdaterListener,
        SpreoUpdateListener, SpreoSearchClickListener, SpreoNavViewListener, MapFilterListener, GalleryListener {
    private String guestKey = "0e9f249966454ad6801a32f45d8a78fe1540236485013813319832";
    private String defaultServerName = "https://developer.spreo.co/middle/ios/";
    private String sandboxServerName = "https://sandbox.spreo.co/middle/ios/";
    private SpreoUpdateListener updateListener = null;
    private String userName = null;
    private static final String TAG = "SpreoModule";
    private HashMap<String, PoiCategory> categoriesTabs = new HashMap<String, PoiCategory>();
    List<IPoi> sourceObjects =new ArrayList<>();
    private final ReactApplicationContext reactContext;
    private ProgressDialog dialog = null;
    ConfigsUpdaterListener configsUpdaterListener;
    private boolean waitingForSettingsResponse = false;
    private boolean gpsBool = true;
    boolean staticMsg = false;
    //from mapview
    private SpreoDualMapView mapView;
    private IPoi toPoi = null;
    private boolean simulation = false;
    private IPoi fromPoi = null;
    private boolean navigationState = false;
    private boolean simulationState = false;
    private boolean debugMode = false;
    private  int distanceType = 1;
    private static final String ZONE_TYPE = "zone";
    private ILocation simulatedLocation = null;
    private SpreoFilterDialog spreoFilterDialog = null;
    private String originMarkerId = ICustomMarker.navPrefix + "origin";
    private String destinationMarkerId = ICustomMarker.navPrefix + "dest";
    private boolean locationVisible = false;
    HashMap<String, ResolveInfo> appsmap;
    //temp
    private DualMapView mapViewDual;
    private RecyclerView recyclerView;
    Handler delayhandler;
    Runnable run;
    String userLastStatus;
    private RelativeLayout floorPickerLL;
    private ImageView showmylocation, startNav, selectCampusButton, mapFilterButton;
    private SpreoSearchView seacrhView;
    private SpreoDetailsView detailsView;
    private SpreoCombinedViewListener combinedViewListener = null;
    private String title = "Maps and Directions";
    private Button closeDetailsBtn, markParkingBtn, cancelParkingBtn, TakeMeParkingBtn, deleteParkingBtn, closeParkingBtn;
    private LinearLayout addParkingMenu, removeParkingMenu;
    private SpreoFromToView fromToView;
    private SpreoStaticNavView staticNavView;
    IPoi closestParking;
    private TextView parkingDisableMsg1, parkingDisableMsg2;
    private IPoi preDefinedDestination = null;
    private boolean thirdPartyNavigation = false;
    private IPoi poiForInitNavigation = null;
    private SpreoLiveNavView liveNavView;
    WritableMap writableMap;
    private boolean realMute ;

    public SpreoModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

    }


    //    public void SpreoModule() {
//        mapView=SpreoMapViewInstance.getInstance().getSpreoDualMapView();
//    }
    @Override
    public String getName() {
        return "Spreo";
    }

    @ReactMethod
    public void setVoiceInstruction(boolean  value) {
        PropertyHolder.getInstance().setNavigationInstructionsSoundMute(value);
        SettingsProvider.getInstance().setNavigationInstructionsSoundMute(value);

    }

    @ReactMethod
    public void getPoiCategories(final Callback callback) {
//        mapView.registerListener(this);
        List<PoiCategory> tmplist = PoisUtils.getPoiCategoies();
        // Set categories tabs for filter select
        for (PoiCategory poiCategory : tmplist) {
            if (poiCategory != null) {
                String type = poiCategory.getPoitype();
                if (!type.isEmpty()) {
                    categoriesTabs.put(type, poiCategory);
                }
            }
        }
        Gson gson = new Gson();
        String json= gson.toJson(tmplist,new TypeToken<List<PoiCategory>>() {}.getType());
        callback.invoke(""+json);
    }
    // get the facility of the floor picker (the facility with highest number of floors)
    @ReactMethod
    public void getAllFacilityFloors(Callback callback) {
        List<SpreoNewFloorObject> floorObjects = new ArrayList<>();
        String campusId = SpreoDataProvider.getCampusId();
// get the facility of the floor picker (the facility with highest number of floors)
        String facilityId = SpreoDataProvider.getFloorPickerFacilityId();
// get the floors index list
        HashMap<String, Object> facilityinfo = SpreoDataProvider
                .getFacilityInfo(campusId, facilityId);
        List<Integer> floors = (List<Integer>) facilityinfo.get("floors");
        HashMap<String, NavigationPath> navmap = RouteCalculationHelper.getInstance().getIndoorNavPaths();
        for (NavigationPath facilitypath : navmap.values()) {
            for (FloorNavigationPath floornav : facilitypath.getFullPath()) {
                if (!floors.contains((int)floornav.getZ())) {
                    floors.add((int)floornav.getZ());
                }
            }
        }
        for (Integer floor : floors) {
            String floortitle = SpreoDataProvider.getFloorTitle(campusId, facilityId, floor);
            floorObjects.add(new SpreoNewFloorObject(floor, floortitle));
        }

        Gson gson = new Gson();
        String json= gson.toJson(floorObjects,new TypeToken<List<SpreoNewFloorObject>>() {}.getType());

        callback.invoke(json);

    }

    @ReactMethod
    public void getWebInterfaceURL(Callback callback) {
        String Url = SettingsProvider.getInstance().getWebInterfaceUrl();
        callback.invoke(Url);
    }

    // function for show All Categories
    @ReactMethod
    public void showAllPois() {

        PoisUtils.setAllPoisCategoriesVisible();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mapView=SpreoMapViewInstance.getInstance().getSpreoDualMapView();
            }
        },2000);
    }

    @ReactMethod
    protected void getFilterCategory(String chars, Callback callback) {
        String filterSeq = chars.toLowerCase();
//        FilterResults result = new FilterResults();
        ArrayList<IPoi> filter = new ArrayList<IPoi>();

        ArrayList<IPoi> first = new ArrayList<IPoi>();
        ArrayList<IPoi> second = new ArrayList<IPoi>();

        List<IPoi> alllist = sourceObjects;
//
//        List<PoiCategory> sourceObjects = new ArrayList<>();
//        sourceObjects.addAll(SpreoSearchDataHolder.getInstance().getMenuCategories());

        if (filterSeq != null && filterSeq.length() > 0) {

            for (IPoi object : alllist) {
                // the filtering itself:
                IPoi poi = (IPoi) object;
                if (filterSeq.contains(" ")) {
                    if (poi.getKeyWordsAsString().toLowerCase().contains(filterSeq) || poi.getpoiDescription().toLowerCase().contains(filterSeq)) {
                        first.add(object);
                    }
                } else {
                    String[] desriptionarray = poi.getpoiDescription().split("\\s+");
                    List<String> desriptionlist = Arrays.asList(desriptionarray);
                    if (isStartWith(desriptionlist, filterSeq)) {
                        first.add(object);
                    } else {
                        List<String> keywordslist = poi.getPoiKeywords();
                        if (isStartWith(keywordslist, filterSeq)) {
                            second.add(object);
                        }
                    }
                }


            }

            filter.addAll(first);
            filter.addAll(second);

//            result.count = filter.size();
//            result.values = filter;
        } else {
// add all objects
            synchronized (this) {
//                result.values = sourceObjects;
//                result.count = sourceObjects.size();
            }
        }
        List<IPoi> tmp = new ArrayList<>();
        List<IPoi> pois = new ArrayList<>();
        for (int i = 0, l = filter.size(); i < l; i++)
            tmp.add((IPoi) filter.get(i));
        if (SpreoSearchDataHolder.getInstance().isSortBYLocation() && SpreoLocationProvider.getInstance().getUserLocation() != null) {
            pois.addAll(sortByLocation(tmp));
        } else {
            pois.addAll(sortAlphabetical(tmp));
        }
        List<PointSearchModel> pointSearchModels = new ArrayList<>();
        for (int i=0; i<pois.size();i++){
            setFloorAndFacilityIdInViewIfPossible(pois.get(i),pointSearchModels);
        }

        Gson gson = new Gson();
        String json= gson.toJson(pointSearchModels,new TypeToken<List<PointSearchModel>>() {}.getType());
        callback.invoke(json);
    }


    @ReactMethod
    public void performFiltering(String chars,Callback callback) {
        String filterSeq = chars.toLowerCase();
//        FilterResults result = new FilterResults();
        ArrayList<IPoi> filter = new ArrayList<IPoi>();

        ArrayList<IPoi> first = new ArrayList<IPoi>();
        ArrayList<IPoi> second = new ArrayList<IPoi>();
        List<IPoi> alllist = PoisUtils.getAllCampusPoisList(SpreoDataProvider.getCampusId());

        if (filterSeq != null && filterSeq.length() > 0) {

            for (IPoi object : alllist) {
                // the filtering itself:
                IPoi poi = (IPoi) object;
                if (filterSeq.contains(" ")) {
                    if (poi.getKeyWordsAsString().toLowerCase().contains(filterSeq) || poi.getpoiDescription().toLowerCase().contains(filterSeq)) {
                        first.add(object);
                    }
                } else {
                    String[] desriptionarray = poi.getpoiDescription().split("\\s+");
                    List<String> desriptionlist = Arrays.asList(desriptionarray);
                    if (isStartWith(desriptionlist, filterSeq)) {
                        first.add(object);
                    } else {
                        List<String> keywordslist = poi.getPoiKeywords();
                        if (isStartWith(keywordslist, filterSeq)) {
                            isStartWithKeyword(keywordslist, filterSeq);
                            second.add(object);
                        }
                    }
                }


            }

            filter.addAll(first);
            filter.addAll(second);

//            result.count = filter.size();
//            result.values = filter;
        } else {
// add all objects
            synchronized (this) {
//                result.values = sourceObjects;
//                result.count = sourceObjects.size();
            }
        }
        List<IPoi> tmp = new ArrayList<>();
        List<IPoi> pois = new ArrayList<>();
        for (int i = 0, l = filter.size(); i < l; i++)
            tmp.add((IPoi) filter.get(i));
        if (SpreoSearchDataHolder.getInstance().isSortBYLocation() && SpreoLocationProvider.getInstance().getUserLocation() != null) {
            pois.addAll(sortByLocation(tmp));
        } else {
            pois.addAll(sortAlphabetical(tmp));
        }
        List<PointSearchModel> pointSearchModels = new ArrayList<>();
        for (int i=0; i<pois.size();i++){
            setFloorAndFacilityIdInViewIfPossible(pois.get(i),pointSearchModels);
        }

        Gson gson = new Gson();
        String json= gson.toJson(pointSearchModels,new TypeToken<List<PointSearchModel>>() {}.getType());
        callback.invoke(json);
    }

    private boolean isStartWith(List<String> list, String typed) {
        boolean result = false;
        for (String o : list) {
            if (o.toLowerCase().startsWith(typed.toLowerCase())) {
                result = true;
                break;
            }
        }
        return result;
    }

    private String isStartWithKeyword(List<String> list, String typed) {

        for (String o : list) {
            if (o.toLowerCase().startsWith(typed.toLowerCase())) {
                return o;
            }
        }
        return "";
    }

    public List<IPoi> sortByLocation(List<IPoi> forsort) {
        List<IPoi> result = SortingPoiUtil.getPoisSortedByLocation(forsort, SpreoLocationProvider.getInstance().getUserLocation());
        return result;
    }

    public List<IPoi> sortAlphabetical(List<IPoi> forsort) {
        List<IPoi> result = PoisUtils.getPoiListSortedAlphabetical(forsort);
        return result;
    }

    //  function for get get All Poi Categories
    @ReactMethod
    public void  getAllPoiCategories(Callback callback){
        List<IPoi> alllist = PoisUtils.getAllCampusPoisList(SpreoDataProvider.getCampusId());
        List<IPoi> sortedlistbyLoc = SortingPoiUtil.getPoisSortedByLocation(alllist, SpreoLocationProvider.getInstance().getUserLocation());
//        List<IPoi> alphalist = SortingPoiUtil.getPoisSortedAlphabetical(sortedlistbyLoc);
        List<PointSearchModel> pointSearchModels = new ArrayList<>();
        for (int i=0; i<sortedlistbyLoc.size();i++){
            setFloorAndFacilityIdInViewIfPossible(sortedlistbyLoc.get(i),pointSearchModels);
        }
        Gson gson = new Gson();
        String json= gson.toJson(pointSearchModels,new TypeToken<List<PointSearchModel>>() {}.getType());
        callback.invoke(json);
    }



    private void setUserLocationVisibilty(boolean visible) {
        mapView.setUserLocationVisibilty(visible);
        locationVisible = visible;
    }
    private void setFloorAndFacilityIdInViewIfPossible(IPoi poi, List<PointSearchModel> pointSearchModels) {
        String floorTitle = SpreoDataProvider.getFloorTitle(poi.getCampusID(), poi.getFacilityID(), (int) poi.getZ());
        PointSearchModel poisSearchModel= new PointSearchModel();
        poisSearchModel.setFloor(floorTitle);
        poisSearchModel.setFacilityID(getFacilityName(poi.getFacilityID()));
        poisSearchModel.setPoi(poi);
        pointSearchModels.add(poisSearchModel);
    }

    private String getFacilityName(String id) {
        String result = id;
        try {
            Campus campus = ProjectConf.getInstance().getSelectedCampus();
            if (campus != null) {
                result = campus.getFacilityConf(id).getName();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    // function for get detail of poi by poi id
    @ReactMethod
    public void getPoiDetail( String poiID,Callback callback) {
        IPoi poi = PoisUtils.getPoiById(poiID);
        String poiFromObj = null;
        if (poi != null){
            String floorTitle = SpreoDataProvider.getFloorTitle(poi.getCampusID(), poi.getFacilityID(), (int) poi.getZ());
            PointSearchModel poisSearchModel = new PointSearchModel();
            poisSearchModel.setFloor(floorTitle);
            poisSearchModel.setFacilityID(getFacilityName(poi.getFacilityID()));
            poisSearchModel.setPoi(poi);

            Gson gson = new Gson();
            poiFromObj = gson.toJson(poisSearchModel, Object.class);
        }else{
            Gson gson = new Gson();
            poiFromObj = gson.toJson(poi, Object.class);
        }
        callback.invoke(poiFromObj);
    }

    // function for show  Chosen Catigories
    @ReactMethod
    public void setPoiCategoriesData(ReadableArray data) {
        final List<PoiCategory> visibleategories = new ArrayList<PoiCategory>();
        for (int i = 0; i < data.size(); i++) {

            if (categoriesTabs.containsKey(data.getString(i))) {
                PoiCategory category = categoriesTabs.get(data.getString(i));
                if (category != null) {
                    visibleategories.add(category);
                }
            }

        }
        if (visibleategories != null) {
            PoisUtils.setPoiCategoriesVisible(visibleategories);
        }
        reDrawPois();
    }

    @ReactMethod

    private  void getBuildinglist (Callback callback){
        Campus campus = ProjectConf.getInstance().getSelectedCampus();
        List<BuildingNameModule> facilites = new ArrayList<>();
        if (campus != null) {
            Map<String, FacilityConf> facilitesmap = campus.getFacilitiesConfMap();

            for (FacilityConf o : facilitesmap.values()) {
                BuildingNameModule buildingNameModule = new BuildingNameModule();
                String name = o.getName(); // the name should be presented on the dialog
                String id = o.getId(); // the ID should be used to present the facility
                buildingNameModule.setId(id);
                buildingNameModule.setName(name);
                facilites.add(buildingNameModule);
            }
        }
        Gson gson = new Gson();
        String json= gson.toJson(facilites,new TypeToken<List<PointSearchModel>>() {}.getType());
        callback.invoke(json);
    }

    @ReactMethod
    private void showBuilding(final String id) {
        //  Present a specific facility on the map:
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                Campus campus = ProjectConf.getInstance().getSelectedCampus();
                if (campus != null) {
                    mapView.presentFacility(campus.getId(), id);
                }
            };
        });
    }

    @ReactMethod
    private void setSimpliFied(boolean isSimplified) {
        SettingsProvider.getInstance().setSimplifiedInstruction(isSimplified);
    }

    @ReactMethod
    private void setStaffOnly(boolean setStaffRouting) {
        SettingsProvider.getInstance().setStaffRouting(setStaffRouting);

    }

    @ReactMethod
    private void checkSetting(boolean isBleAlert,Callback callback) {
        String action = null;
        boolean gpsenabled = isGpsEnabled(reactContext);
        boolean btenabled = isBluetoothEnabled(reactContext);
        //get the campus
        Campus campus = ProjectConf.getInstance().getSelectedCampus();
        //get campus radius
        int radius = campus.getRadius();
        //check if the location is in campus
        boolean incampus = LocationFinder.getInstance().isInCampus(radius);

        WritableMap params = Arguments.createMap();
        params.putBoolean("isIncampus", incampus);
        params.putBoolean("isLocation", gpsenabled);
        params.putBoolean("isBluetooth", btenabled);
        callback.invoke(params);

//        if (!gpsenabled ) {
//            action = Settings.ACTION_SETTINGS;
//        }

//        if (action != null) {
//            callback.invoke(true);
//        }else{
//            callback.invoke(false);
//        }
    }

    @ReactMethod
    private  void checkLocation (){
        delayhandler = new Handler();
        run = new Runnable() {
            @Override
            public void run() {
                loop();
            }
        };
        delayhandler.postDelayed(run, 10000);
    }

    void loop(){
        //get the campus
//        Campus campus = ProjectConf.getInstance().getSelectedCampus();
        //get campus radius
//        int radius = campus.getRadius();
        //check if the location is in campus
//        boolean incampus = LocationFinder.getInstance().isInCampus(radius);
//        if(!incampus){
            if(!navigationState){
                startLocationtimer();
            }
//        }

        delayhandler.postDelayed(run, 10000);
    }

    @ReactMethod
    private void startLocationtimer () {
        //get the campus
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                Campus campus = ProjectConf.getInstance().getSelectedCampus();
                if (campus != null) {
                    //get campus radius
                    int radius = campus.getRadius();
                    //check if the location is in campus
                    boolean incampus = LocationFinder.getInstance().isInCampus(radius);
                    if (isBluetoothEnabled(reactContext) && isGpsEnabled(reactContext) && incampus  && userLastStatus == "outCampus" ) {
                        //present the message we can't find you in campus. you should show this message only on first time.
                        //enable follow me mode with 15000 interval
                        SettingsProvider.getInstance().setUserAutoFollowTimeInterval(15000);
                        //show the user location
                        userLastStatus = "inCampus";
                            mapView.showMyLocation();
                            mapView.setUserLocationVisibilty(true);
                    } else if(userLastStatus == "inCampus" && (!isBluetoothEnabled(reactContext) || !isGpsEnabled(reactContext) ||!incampus) ) {
                        //get the default location
                        ILocation defaultlocation = campus.getDefaultCampusLocation();
                        //disable follow me mode
                        SettingsProvider.getInstance().setUserAutoFollowTimeInterval(-1);
                        //present the default location
                        userLastStatus = "outCampus";
                        try {
                                mapView.presentLocation(defaultlocation);
                                mapView.setUserLocationVisibilty(false);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }

                    }
                }
            }
        });
    }

    @ReactMethod
    private void startLocationCheck () {
        //get the campus
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                boolean gpsenabled = isGpsEnabled(reactContext);
                Campus campus = ProjectConf.getInstance().getSelectedCampus();
                if (campus != null) {
                    //get campus radius
                    int radius = campus.getRadius();
                    //check if the location is in campus
                    boolean incampus = LocationFinder.getInstance().isInCampus(radius);
                    if (isBluetoothEnabled(reactContext) && isGpsEnabled(reactContext) && incampus ) {
                        //present the message we can't find you in campus. you should show this message only on first time.
                        //enable follow me mode with 15000 interval
                        SettingsProvider.getInstance().setUserAutoFollowTimeInterval(15000);
                        //show the user location
                        userLastStatus = "inCampus";
                        mapView.showMyLocation();
                        if(!navigationState){
                            mapView.setUserLocationVisibilty(true);
                        }
                    } else {
                        //get the default location
                        ILocation defaultlocation = campus.getDefaultCampusLocation();
                        //disable follow me mode
                        SettingsProvider.getInstance().setUserAutoFollowTimeInterval(-1);
                        //present the default location
                        userLastStatus = "outCampus";
                        try {
                            mapView.presentLocation(defaultlocation);
                            mapView.setUserLocationVisibilty(false);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }

                    }
                }
            }
        });
    }

    private boolean isBluetoothEnabled(Context reactContext) {
        boolean result = false;
        BluetoothAdapter bluetoothAdapter = null;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) reactContext.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter(); if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    private boolean isGpsEnabled(Context reactContext) {
        boolean result = false;
        LocationManager locationManager = (LocationManager) reactContext.getSystemService(Context.LOCATION_SERVICE);
        result = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return result;
    }

    @ReactMethod
    private void openSetting(String value) {
        Activity currentActivity = getCurrentActivity();
        if(value.equals("bluetooth")) {
            String action = Settings.ACTION_BLUETOOTH_SETTINGS;
            final String finalAction = action;
            currentActivity.startActivityForResult(new Intent(finalAction), 0);
        }else if(value.equals("location")){
            String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
            final String finalAction = action;
            currentActivity.startActivityForResult(new Intent(finalAction), 0);
        }

    }

    @ReactMethod
    public void downloadData(String key,final Callback callback) {

        SpreoUsersInfoHolder.getInstance().setSelectedKey(key);

        if(SpreoUsersInfoHolder.getInstance().getSelectedLanguage()==null || SpreoUsersInfoHolder.getInstance().getSelectedLanguage().equals("")){
            SpreoUsersInfoHolder.getInstance().setSelectedLanguage("english");
        }else {
            SpreoUsersInfoHolder.getInstance().setSelectedLanguage(SpreoUsersInfoHolder.getInstance().getSelectedLanguage());
        }
        openSplashScreen(callback);

        //    }
    }

    private void openSplashScreen(Callback callback) {
        UpdateActivity updateActivity= new UpdateActivity();
        updateActivity.onInit(reactContext,callback);
    }
    //    private boolean isGpsEnabled(Context reactContext) {
//        boolean result = false;
//        LocationManager locationManager = (LocationManager) reactContext.getSystemService(Context.LOCATION_SERVICE);
//        result = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        return result;
//    }
//
//    private boolean isBluetoothEnabled(Context reactContext) {
//        boolean result = false;
//        BluetoothAdapter bluetoothAdapter = null;
//// Initializes Bluetooth adapter.
//        final BluetoothManager bluetoothManager =
//                (BluetoothManager) reactContext.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
//        bluetoothAdapter = bluetoothManager.getAdapter();
//
//        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
//            result = false;
//        } else {
//            result = true;
//        }
//        return result;
//    }
    @TargetApi(Build.VERSION_CODES.M)
    private Boolean hasPermission(String requiredpermission) {
        boolean hasPermission = (reactContext.checkSelfPermission(requiredpermission) == PackageManager.PERMISSION_GRANTED);
        return hasPermission;
    }

//    @ReactMethod
//    public void downloadData(String stringArgument,  Callback callback) {
//        callbackreturn = callback;
//        PropertyHolder.getInstance().setServerName(defaultServerName);
//        String servername = PropertyHolder.getInstance().getServerName();
//        update("fdaf38bf053c49f3adba7c0c5d11621315577798257061405987126", "english", servername);
//
////        SpreoResourceConfigsUtils.subscribeToResourceUpdateService(this);
//        SpreoResourceConfigsUtils.update(reactContext);
//    }


    public void update(String key, String language, String servername) {
        if (key != null) {
            SpreoResourceConfigsUtils.setSpreoApiKey(key)
            ;
            SpreoResourceConfigsUtils.subscribeToResourceUpdateService(this);
            PropertyHolder.getInstance().setServerName(servername);
            PropertyHolder.getInstance().setAppLanguage(language);
            SettingsProvider.getInstance().setUseZipWithoutMaps(true);
            SpreoResourceConfigsUtils.update(reactContext);
        }
    }

    @Override
    public void onPreConfigsDownload() {

    }

    @Override
    public void onPostConfigsDownload(ResUpdateStatus resUpdateStatus) {
        if (resUpdateStatus.equals(ResUpdateStatus.OK)) {
            SpreoResourceConfigsUtils.unSubscribeFromResourceUpdateService(this);

            SpreoLocationProvider.getInstance().startLocationService(reactContext);


            notifyUpdateFinished();

        } else {

        }

    }

    private void notifyUpdateFinished() {

        try {
            if (updateListener != null) {
                updateListener.OnUpdateFinished();

            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void onPreConfigsInit() {

    }

    @Override
    public void onPostConfigsInit(ResUpdateStatus resUpdateStatus) {

    }
    public void setUpdateListener(SpreoUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    @Override
    public void OnUpdateFinished() {
        ApplicationSettings.getInstance().init(reactContext.getApplicationContext());

        String randomID = SpreoAnalyticsUtility.generateRandomID();

//            openMap();
        Log.d("shriom",SpreoDataProvider.getCampusId());


        if (!SpreoAnalyticsUtility.newSessionSent) {
            SpreoAnalyticsUtility.sendReport(SpreoAnalyticsUtility.NEW_SESSION_REPORT, randomID, null);
            SpreoAnalyticsUtility.newSessionSent = true;
        }
    }
//
//    @Override
//    public void OnUserDataDownloaded(String status) {
////        if (pl != null) {
////            pl.setVisibility(View.GONE); // TODO Rohit look up later
////        }
//        Log.d("shriom", "update data download "+status );
//        String username = "sheba_admin";
//        List<spreo.spreomobile.SpreoApiKeyConfigsObj> list = SpreoUsersInfoHolder.getInstance()
//                .getUserConfigs();
//        if (list != null) {
//
//            userName = username;
//
//            if (list.size() == 1) {
//                spreo.spreomobile.SpreoApiKeyConfigsObj conf = list.get(0);
//
//                if (conf != null) {
////                    ConfigsUpdater.getInstance().setReqApikey(conf.getKey());
//                    SpreoUsersInfoHolder.getInstance().setSelectedKey(conf.getKey());
//
////                    openSplashScreen(); // TODO Rohit look up later
//                }
//            } else {
//                Log.d("shriom", String.valueOf(list));
////                displayChooseProjectUI(list);// TODO Rohit look up later
//            }
//
//        }
//    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkOptionalPermissions() {

        List<String> optionalPermissions = new ArrayList<String>();

        optionalPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        optionalPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        optionalPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        optionalPermissions.add(Manifest.permission.CAMERA);

        List<String> forRequest = new ArrayList<String>();
        for (String o : optionalPermissions) {
            boolean hasPermission = (reactContext.checkSelfPermission(o) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                forRequest.add(o);
            }
        }

        if (forRequest.isEmpty()) {
//            startInit(); // TODO Rohit look up later
        } else {
            String arr[] = new String[forRequest.size()];
            String[] permissions = (String[]) forRequest.toArray(arr);
//            requestPermissions(permissions, 2); // TODO Rohit look up later
        }
    }

    public void setSimulatedLocation(ILocation location) {
        simulatedLocation = location;
    }


    private View.OnClickListener markParkingListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            markMyParking();
        }
    };

    private void markMyParking() {
        mapView.setCurrentLocationAsParking();
        //openParkingMenu();
    }

    private View.OnClickListener cancelParkingListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            closeParkingMenu();
        }
    };

    private View.OnClickListener TakeMeParkingListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            closeParkingMenu();
            startNavigationToParking();
            liveNavView.setParkingNav();
        }
    };

    private View.OnClickListener deleteParkingListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mapView.removeParkingLocation();
            // openParkingMenu();
        }
    };

    private View.OnClickListener closeParkingListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            closeParkingMenu();
        }
    };

    private View.OnClickListener closeDetailsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            title = "Maps and Directions";
            notifyTitleChagne();
            detailsView.setVisibility(GONE);
            detailsView.clear();
            seacrhView.refreshFavorites();
        }
    };

    private void notifyTitleChagne() {
        if (combinedViewListener != null) {
            try {
                combinedViewListener.onTitleChanged(title);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @ReactMethod
    public void showMyLocation() {
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
//                    Campus campus = ProjectConf.getInstance().getSelectedCampus();
//                     //get campus radius
//                    int radius = campus.getRadius();
//                    //check if the location is in campus
//                    boolean incampus = LocationFinder.getInstance().isInCampus(radius);
//                    if(incampus && isGpsEnabled(reactContext) && isBluetoothEnabled(reactContext)){
//                        mapView.showMyLocation();
//                        mapView.setUserLocationVisibilty(true);
//                    }else{
                startLocationCheck();
//                    }
            }
        });
    }

    @ReactMethod
    public void reDrawPois() {
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                mapView.reDrawPois();
            }
        });
    }

    @ReactMethod
    public void updateMapwithFloorId(final int floorId) {
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                mapView.showFloorWithId(floorId);
            }
        });
    }


    @ReactMethod
    private void openNavView(Callback callback) {

        if (fromPoi != null) {
//            realMute = PropertyHolder.getInstance().isNavigationInstructionsSoundMute();
//            PropertyHolder.getInstance().setNavigationInstructionsSoundMute(realMute);
            staticMsg = true;
            if (toPoi != null) {


                updateInstructions(callback);

/* String text = "Navigating to " + toPoi.getpoiDescription();
if (text != null) {
staticNavView.setText(text);
}*/
            }
        } else {
            setPoi(toPoi,callback);
        }
    }

    @ReactMethod
    private void markMyparking(final Callback callback) {
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                mapView.setCurrentLocationAsParking();
                if (mapView.hasParkingLocation()) {
                    //  addParkingMenu.setVisibility(View.GONE);
                    //   removeParkingMenu.setVisibility(View.VISIBLE);
                    ILocation parkloc = mapView.getParkingLocation();
                    if (parkloc != null) {
                        mapView.presentLocation(parkloc);
                    }
                } else {
                    //  removeParkingMenu.setVisibility(View.GONE);
                    //  addParkingMenu.setVisibility(View.VISIBLE);
//                    mapView.showMyLocation();
                    startLocationCheck();
                }
                callback.invoke("done");
            }
        });


    }

    @ReactMethod
    private void menuCategories(Callback callback) {
        List<PoiCategory> tmplist = PoisUtils.getPoiCategoies();
        ArrayList<PoiCategory> tmpcategories = new ArrayList<>();
        for (PoiCategory o : tmplist) {
            String name = o.getPoitype();
            if (!name.trim().isEmpty() && o.showInCatgories) {
                tmpcategories.add(o);
            }
        }
        List<PoiCategory> menuCategories = getCategoriesSortedAlphabetical(tmpcategories);

        String tmpCat= new Gson().toJson(menuCategories,new TypeToken<List<PoiCategory>>() {
        }.getType());
        callback.invoke(tmpCat);
    }
    private static List<PoiCategory> getCategoriesSortedAlphabetical(List<PoiCategory> categorylist) {
        List<PoiCategory> result = new ArrayList();
        result.addAll(categorylist);
        Collections.sort(result, new Comparator<PoiCategory>() {
            public int compare(PoiCategory p1, PoiCategory p2) {
                String s1 = p1.getPoitype();
                String s2 = p2.getPoitype();
                return s1.compareToIgnoreCase(s2);
            }
        });
        return result;
    }

    @ReactMethod
    private void poiSubCategories(String categoryname,Callback callback) {

        List<IPoi> poiList = PoisUtils.getAllCampusPoisList(SpreoDataProvider.getCampusId());
        List<IPoi> tmp = new ArrayList<>();
        List<PointSearchModel> pointSearchModels = new ArrayList<>();
        for (IPoi o : poiList) {
            for (String type : o.getPoitype()) {
                if (type.equals(categoryname)) {
                    tmp.add(o);

/* String floorTitle = SpreoDataProvider.getFloorTitle(o.getCampusID(), o.getFacilityID(), (int) o.getZ());
PointSearchModel poisSearchModel= new PointSearchModel();
poisSearchModel.setFloor(floorTitle);
poisSearchModel.setFacilityID(getFacilityName(o.getFacilityID()));
poisSearchModel.setPoi(o);
pointSearchModels.add(poisSearchModel);*/
                    break;
                }
            }
        }
        List<IPoi> sortedlistbyLoc =new ArrayList<>();


        if (SpreoSearchDataHolder.getInstance().isSortBYLocation() && SpreoLocationProvider.getInstance().getUserLocation() != null) {
            sortedlistbyLoc.addAll(sortByLocation(tmp));

        } else {
            sortedlistbyLoc.addAll(sortAlphabetical(tmp));
        }
        for (int i=0; i<sortedlistbyLoc.size();i++){
            setFloorAndFacilityIdInViewIfPossible(sortedlistbyLoc.get(i),pointSearchModels);
        }
        sourceObjects = sortedlistbyLoc;

        String tmpSubCat= new Gson().toJson(pointSearchModels,new TypeToken<List<PointSearchModel>>() {
        }.getType());
        callback.invoke(tmpSubCat);

    }

    @ReactMethod
    private  void setSimulation(Boolean value){
        simulation = value;
        PropertyHolder.getInstance().setSimulationMode(value);
    }


    @ReactMethod
    private void startNavigationToParking() {
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                presentOrigin();
                if (PropertyHolder.getInstance().isSimulationMode()) {
                    ILocation origin = LocationFinder.getInstance().getCurrentLocation();
                    if (origin != null) {
                        mapView.simulateNavigationToParking(origin);
//                        mapView.showMyLocation();
                        startLocationCheck();
                    }
                    simulationState = true;
                } else {
                    mapView.navigateToParking();
                }

                navigationState = true;
            }
        });


    }

    @ReactMethod
    private void removeParkingLocation(final Callback callback) {

        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                mapView.removeParkingLocation();  // delete parking
                callback.invoke("done");
            }
        });

    }

    @ReactMethod
    private void hasParkingLocation(final Callback callback) {

        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                if(mapView.hasParkingLocation()){
                    callback.invoke("true");
                }else {
                    callback.invoke("false");
                }

            }
        });

    }

    // Update navigation Instruction
    public void updateInstructions(Callback callback) {
        Gson gson= new Gson();
        //List<Instruction> instructions = RouteCalculationHelper.getInstance().getCombinedInstructions();
        List<INavInstruction> instructions = RouteCalculationHelper.getInstance().getCombinedSimplifiedInstructions();
        List<INavInstruction> simplified = getInstructions(instructions);
        String updateInstructions= gson.toJson(simplified, Object.class);
        WritableMap params = Arguments.createMap();
        params.putString("updateInstructions", updateInstructions);
        params.putBoolean("staticMsg", staticMsg);
        String txt = calculateDistance();
        if (txt != null) {
            params.putString("distance", txt);
        }
        callback.invoke(params);
    }

    @ReactMethod
    public void setPoiContent(final String pois, final Callback callback) {


        final Gson gson = new Gson();
        /* boolean isfavorite = SpreoSearchDataHolder.getInstance().isFavorite(poi);
        if (isfavorite) {
        addToFavoritesBtn.setText(removeFromFavorites);
        } else {
        addToFavoritesBtn.setText(addToFavorites);
        }*/
        Object obj = gson.fromJson(pois, Object.class);
        String fromPoiId = ((LinkedTreeMap) obj).get("poiID").toString();
        List<IPoi> alllist = PoisUtils.getAllCampusPoisList(SpreoDataProvider.getCampusId());
        IPoi poi = null;
        for (int i = 0; alllist.size() > i; i++) {
            if (alllist.get(i).getPoiID().equals(fromPoiId)) {
                poi = alllist.get(i);
                break;
            }
        }
        // PoisUtils.updateIPoiHeadImage(poi, this);
        //  PoisUtils.updateIPoiGallery(poi, this);
        writableMap = Arguments.createMap();
        if (poi.getpoiDescription() != null) {
            writableMap.putString("poiNameTv", poi.getpoiDescription());
        }
        String poiStr = gson.toJson(poi, Object.class);
        writableMap.putString("poi", poiStr);


        closestParking = PoisUtils.getClosestParking(poi);
        if (closestParking != null) {
            writableMap.putString("closestParkingName", closestParking.getpoiDescription());
            String closestParkingPoiStr = gson.toJson(closestParking, Object.class);
            writableMap.putString("closestParkingPoi", closestParkingPoiStr);
//
//        closestParkingName.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//        setPoi(closestParking);
//        }
//        });

        }


        // expandableTextView.setAnimationDuration(1000L);
        // expandableTextView.setInterpolator(new OvershootInterpolator());
        // showMore.setOnClickListener(showMoreListener);
        String poiInfo = "";
        String facilityID = "";
        String category = "";
        String floor = getFloorText(poi);
        Campus campus = ProjectConf.getInstance().getSelectedCampus();
        if (campus != null) {
            if (floor != null && !floor.isEmpty()) {
                String language =  PropertyHolder.getInstance().getAppLanguage();
                floor = floor.replace("L", "");
                if(language.equals("english")){
                    poiInfo = campus.getFacilityConf(poi.getFacilityID()).getName() + ", " + " Floor" + " " + floor;
                }else{
                    poiInfo = campus.getFacilityConf(poi.getFacilityID()).getName() + ", " + " קומה" + " " + floor;
                }

                category = poi.getPoitype().get(0);
                facilityID = campus.getFacilityConf(poi.getFacilityID()).getName();
            } else {
                poiInfo = campus.getFacilityConf(poi.getFacilityID()).getName();
                category = poi.getPoitype().get(0);
                facilityID = campus.getFacilityConf(poi.getFacilityID()).getName();
            }
        }
        // poiInfo = poiInfo.replace("ach hospital", getString(R.string.american_medical_center));
        // poiInfo = poiInfo.replace("[", "");
        // poiInfo = poiInfo.replace("]", "");
        writableMap.putString("facilityID", facilityID);
        writableMap.putString("poiInfo", poiInfo);
        writableMap.putString("floor", floor);
        writableMap.putString("category", category);


        if (poi.getActivehours().size() > 0) {
            writableMap.putString("hoursTv", poi.getActivehours().get(0).trim());
        }

        if (!poi.getDetails().isEmpty()) {
            // descriptionLL.setVisibility(View.VISIBLE);
            // expandableTextView.setText(poi.getDetails().trim());
            // showMoreLL.setVisibility(expandableTextView.getLineCount() > 3 ? View.VISIBLE : View.GONE);
            // showMore.post(new Runnable() {
            // @Override
            // public void run() {
            // Layout layout = expandableTextView.getLayout();
            // if (layout != null) {
            // int lines = layout.getLineCount();
            // if (lines > 0) {
            // final int ellipsisCount = layout.getEllipsisCount(lines - 1);
            // getActivity().runOnUiThread(new Runnable() {
            // @Override
            // public void run() {
            // showMoreLL.setVisibility(ellipsisCount > 0 ? View.VISIBLE : View.GONE);
            // final View view = (View) findViewById(R.id.description_sep);
            // view.setVisibility(ellipsisCount > 0 ? View.GONE : View.VISIBLE);
            // if (ellipsisCount > 0) {
            // expandableTextView.setOnClickListener(showMoreListener);
            // }
            // }
            // });
            // }
            // }
            // }
            // });
        }

        if (poi.getUrl() != null && !poi.getUrl().isEmpty() && !poi.getUrl().equals("null")) {

            writableMap.putString("poiUrl", poi.getUrl());

            /* webUrl.setText("More information");
            webInfo.setVisibility(View.VISIBLE);
            webInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(poi.getUrl()));
            ctx.startActivity(i);
            }
            });*/
        }

        if (!poi.getEmailaddress().isEmpty()) {

            writableMap.putString("emails", poi.getEmailaddress());

            /* emailTv.setText(poi.getEmailaddress());
            contactsMailLL.setVisibility(View.VISIBLE);
            contactsMailLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{poi.getEmailaddress()});
            ctx.startActivity(Intent.createChooser(intent, ""));
            }
            });*/
        }

        if (poi.getPhone1() != null && poi.getPhone1().size() > 0) {
            writableMap.putString("phone1", poi.getPhone1().get(0));
            //phoneTv1.setText(poi.getPhone1().get(0));
            /* contactsPhoneLL1.setVisibility(View.VISIBLE);
            contactsPhoneLL1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            String uri = "tel:" + poi.getPhone1().get(0).replaceAll("[^0-9|\\+]", "");
            intent.setData(Uri.parse(uri));
            ctx.startActivity(intent);
            }
            });*/
        }

        if (poi.getPhone2() != null && poi.getPhone2().size() > 0) {
            writableMap.putString("phone2", poi.getPhone2().get(0));

            // poi.getPhone2().get(0);
            /* phoneTv2.setText(poi.getPhone2().get(0));
            contactsPhoneLL2.setVisibility(View.VISIBLE);
            contactsPhoneLL2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            String uri = "tel:" + poi.getPhone2().get(0).replaceAll("[^0-9|\\+]", "");
            intent.setData(Uri.parse(uri));
            ctx.startActivity(intent);
            }
            });*/
        }

        if (poi.getPoiKeywords() != null && !poi.getPoiKeywords().isEmpty()) {
            String keywordtext = "";
            for (String o : poi.getPoiKeywords()) {
                if (poi.getPoiKeywords().indexOf(o) == 0) {
                    keywordtext += o;
                } else {
                    keywordtext += ", " + o;
                }
            }
            writableMap.putString("keywordtext", keywordtext);
            //keywordsText.setText(keywordtext);
            //keywordsLL.setVisibility(View.VISIBLE);
        }


        final IPoi finalPoi = poi;
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                GalleryObject headImage = finalPoi.getHeadImage();
                ArrayList<String> mData = new ArrayList<>();
                String base = PropertyHolder.getInstance().getServerName();
                String projectId = PropertyHolder.getInstance().getProjectId();
                String CampusID = finalPoi.getCampusID();
                String facilityID = finalPoi.getFacilityID();
                String url;

                if (headImage != null) {
                    url = base +"res"+ "/" + projectId + "/" + CampusID + "/" + facilityID + "/" + headImage.getUri();
                    mData.add(url);
                }


                if (finalPoi.getGallery().size() > 0) {
                    for (GalleryObject galleryObject : finalPoi.getGallery()) {
                        mData.add(base +"res"+ "/" + projectId + "/" + CampusID + "/" + facilityID + "/" + galleryObject.getUri());
                    }
                    String mediaurl = finalPoi.getMediaurl();
                    if (mediaurl != null && (!mediaurl.isEmpty() && mediaurl.length() > 4) && finalPoi.isPoiPlayMultyMedia()) {
                        String subStrMedia = mediaurl.substring(mediaurl.length() - 4);
                        if (subStrMedia.equals(".mp4")) {
                            mData.add(mediaurl);
                        }
                    }


                } else if (headImage != null) {
                    mData.add(base + "res/" + projectId + "/" + CampusID + "/" + facilityID + "/" + headImage.getUri());
                    if (finalPoi.getMediaurl() != null && !finalPoi.getMediaurl().isEmpty()) {
                        String mediaurl = finalPoi.getMediaurl();
                        mData.add(mediaurl);
                    }
                }

                if (finalPoi.getGallery() != null) {
                    if (finalPoi.getGallery().size() == 0) {
                        if (finalPoi.getMediaurl() != null && !finalPoi.getMediaurl().isEmpty() && finalPoi.isPoiPlayMultyMedia()) {
                            writableMap.putString("loadMainLogo", "false");
                        } else {
                            writableMap.putString("loadMainLogo", "true");
                        }
                    }
                } else {
                    writableMap.putString("loadMainLogo", "true");
                }
                Set<String> set = new HashSet<String>(mData);
                List<String> list = new ArrayList<String>();
                for (String temp : set){
                    if(temp != null && !temp.equals("null")){
                        list.add(temp);
                    }

                }
                String multiMedia = gson.toJson(list, new TypeToken<List<String>>() {
                }.getType());
                synchronized (reactContext) {
                    writableMap.putString("multiMedia", multiMedia);
                    callback.invoke(writableMap);
                }
            }

        });




    }

    @ReactMethod
    public void updateLanguage(String language,Callback callback) {
        PropertyHolder.getInstance().setAppLanguage(language);
        SpreoResourceConfigsUtils.setSpreoApiKey(SpreoUsersInfoHolder.getInstance().getSelectedKey());
        //  SpreoResourceConfigsUtils.subscribeToResourceUpdateService(this);
        SpreoUsersInfoHolder.getInstance().setSelectedLanguage(language);
        SettingsProvider.getInstance().setUseZipWithoutMaps(true);
        SpreoResourceConfigsUtils.update(reactContext);
        callback.invoke("done");
    }

    // Update navigation changed
    private List<INavInstruction> getInstructions(List<INavInstruction> ins) {
        List<INavInstruction> result = new ArrayList<>();
        List<INavInstruction> instructions = new ArrayList<>();
        instructions.addAll(ins);
        for (INavInstruction o: instructions) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            o.getSignBitmap().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            int fd = (int) o.getDistance();
            int md = (int) MathUtils.metersToFeet(fd);

            double result2 ;

            if(distanceType == 1){
                result2 =   fd ;
            }else if(distanceType == 2){
                result2 =   md ;
            }else{
                result2 =  fd ;
            }

            SpreoInstructionObj obj = new SpreoInstructionObj(o.getId(), o.getText(),o.getSignBitmap(), encoded,  result2);
            result.add(obj);
        }
        return result;
    }

    @ReactMethod
    public void setPoi(IPoi poi , Callback callback) {
        WritableMap params = Arguments.createMap();
        if (poi != null) {
            String dname = poi.getpoiDescription();
            if (dname != null) {
                params.putString("dname", dname);
            }
            String dinfo = getDestinationInfo(poi);
            if (dinfo != null) {
                params.putString("dinfo", dinfo);
            }
        }

        String distance = calculateDistance();
        if (distance != null && !distance.isEmpty()) {
            params.putString("distance", distance);
        }
        callback.invoke(params);
    }

    @ReactMethod
    public void showPoi(final String poi){
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                Gson gson= new Gson();
                Object showPOI= gson.fromJson(poi,Object.class);
                IPoi pois = null;
                List<IPoi> alllist = PoisUtils.getAllCampusPoisList(SpreoDataProvider.getCampusId());
                if(poi != null){
                    String poiId=((LinkedTreeMap) showPOI).get("poiID").toString();
                    for(int i =0; alllist.size()>i;i++){
                        if(alllist.get(i).getPoiID().equals(poiId)) {
                            pois = alllist.get(i);
                            break;
                        }
                    }
                }
                mapView.showPoi(pois);
            }
        });
    }


    private String getDestinationInfo(IPoi poi) {
        String result = "";
        if (poi != null) {
            String floor = getFloorText(poi);
            Campus campus = ProjectConf.getInstance().getSelectedCampus();
            if (campus != null) {
                if (floor != null && !floor.isEmpty()) {
                    floor = floor.replace("L", "");
                    String Floor = reactContext.getString(R.string.floor);
                    result = campus.getFacilityConf(poi.getFacilityID()).getName() + ", " + Floor + " " + floor;
                } else {
                    result = campus.getFacilityConf(poi.getFacilityID()).getName();
                }
            }
        }
        return result;
    }

    private String getFloorText(IPoi citem) {
        String result = "";
        if (!citem.getPoiNavigationType().equals("external")) {
            Campus ccampus = ProjectConf.getInstance().getSelectedCampus();
            FacilityConf cfacility = null;
            if (ccampus != null) {
                Map<String, FacilityConf> fmap = ccampus.getFacilitiesConfMap();
                int z = (int) citem.getZ();
                String fid = citem.getFacilityID();
                if (fid != null && !fid.equals("unknown")) {
                    cfacility = fmap.get(fid);
                    if (cfacility != null) {
                        String floortitle = cfacility.getFloorTitle(z);
                        if (floortitle != null) {
                            result = floortitle;
                        }
                    }
                }
            }
        } else {
            result = "L1";
        }
        return result;
    }

    @ReactMethod
    public void setMarkerOntoLocation(final String to) {
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                Gson gson= new Gson();
                Object obj= gson.fromJson(to,Object.class);
                String fromPoiId=((LinkedTreeMap) obj).get("poiID").toString();
                List<IPoi> alllist = PoisUtils.getAllCampusPoisList(SpreoDataProvider.getCampusId());
                IPoi to = null;
                for(int i =0; alllist.size()>i;i++){
                    if(alllist.get(i).getPoiID().equals(fromPoiId)){
                        to= alllist.get(i);
                        break;
                    }
                }
                presentDestination(to);
            }
        });
    }

    @ReactMethod
    public void startNavigation(final String poiFrom, final String poiTo, final Callback callback) {
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {
                Gson gson= new Gson();

                Object poiFromObj= gson.fromJson(poiFrom,Object.class);
                Object poiToObj= gson.fromJson(poiTo,Object.class);
                IPoi from = null,to=null;
                String fromPoiId= null;
                String toPoiId=((LinkedTreeMap) poiToObj).get("poiID").toString();
                List<IPoi> alllist = PoisUtils.getAllCampusPoisList(SpreoDataProvider.getCampusId());
                if(poiFrom != null){
                    fromPoiId=((LinkedTreeMap) poiFromObj).get("poiID").toString();
                    for(int i =0; alllist.size()>i;i++){
                        if(alllist.get(i).getPoiID().equals(fromPoiId)){
                            from= alllist.get(i);
                            break;
                        }
                    }
                }

                for(int i =0; alllist.size()>i;i++){
                    if(alllist.get(i).getPoiID().equals(toPoiId)){
                        to= alllist.get(i);
                        break;
                    }
                }
                navigate(from, to, callback);
            }
        });
    }



    private View.OnClickListener mapFilterButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openMapFilter();
        }
    };

    private void openMapFilter() {
        spreoFilterDialog = new SpreoFilterDialog();
        spreoFilterDialog.showMapFilter(reactContext, this);
    }

    private View.OnClickListener selectCampusButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (combinedViewListener != null) {
                try {
                    combinedViewListener.onCampusSelectionClick();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    };




    private void initFromTo() {
        fromToView.showToEdit();
        //openFromTo();
    }


    private int getFloorConsiderNoLocationMode(IPoi poiStartNav) {

        int result = getEntranceFloor();
        boolean isNoLocationMode = false; //Lookup.getInstance().lookup(SimulationHelper.class).isNoLocationMode();
        ILocation myloc = SpreoLocationProvider.getInstance().getUserLocation();

        if (poiStartNav != null) {
            result = (int)poiStartNav.getLocation().getZ();
        } else if (!isNoLocationMode && myloc != null && myloc.getLocationType() == LocationMode.INDOOR_MODE) {
            result = (int)myloc.getZ();
        }

        return result;
    }

    public int getEntranceFloor() {
        int result = 0;
        Campus campus = ProjectConf.getInstance().getSelectedCampus();
        if (campus != null) {
            String facilityId = SpreoDataProvider.getFloorPickerFacilityId();
            FacilityConf fac = campus.getFacilityConf(facilityId);
            if (fac != null) {
                result = fac.getEntranceFloor();
            }
        }
        return result;
    }

    public static List<SpreoNewFloorObject> getAllFacilityFloors() {
        List<SpreoNewFloorObject> floorObjects = new ArrayList<>();
        String campusId = SpreoDataProvider.getCampusId();
        // get the facility of the floor picker (the facility with highest number of floors)
        String facilityId = SpreoDataProvider.getFloorPickerFacilityId();
        // get the floors index list
        HashMap<String, Object> facilityinfo = SpreoDataProvider
                .getFacilityInfo(campusId, facilityId);
        List<Integer> floors = (List<Integer>) facilityinfo.get("floors");
        for (Integer floor : floors) {
            String floortitle = SpreoDataProvider.getFloorTitle(campusId, facilityId, floor);
            floorObjects.add(new SpreoNewFloorObject(floor, floortitle));
        }
        return floorObjects;


    }

    public void onCreate(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        resetNavigation();
    }

    public void resetNavigation() {
        mapView.stopNavigation();
        List<ICustomMarker> mlist = new ArrayList<>();
        mapView.setCustomMarkers(mlist);
    }


    public void onResume() {
        // required for the google map
        mapView.onResume();

    }

    public void onDestroy() {
        if (simulationState) {
            mapView.stopSimulation();
        }
        // required for the google map
        mapView.onDestroy();
    }

    public void onPause() {
        // required for the google map
        mapView.onPause();
    }

    public void onLowMemory() {
        // required for the google map
        mapView.onLowMemory();
    }

    public void onSaveInstanceState(Bundle outState) {
        // required for the google map
        mapView.onSaveInstanceState(outState);
    }




    @Override
    public void onGoClickListener(IPoi poi) {
        if (navigationState) {
            // navigationCanceled();
        }
        seacrhView.hideMenu();
        seacrhView.setseacrhType(null);
        detailsView.setVisibility(GONE);
        if (poi != null) {
            SpreoSearchDataHolder.getInstance().addHistory(poi);
            fromToView.setTo(poi);
            //  presentDestination(poi, view);
            //openFromTo();
        }
    }

    @Override
    public void onItemClickListener(IPoi poi) {
        if (poi != null) {
            showPoiDetails(poi, false);

            SpreoAnalyticsUtility.sendReport(SpreoAnalyticsUtility.SEARCH_REPORT, poi.getpoiDescription(), poi.getFacilityID());
        }
    }

    private void showPoiDetails(IPoi poi, boolean restSearch) {
        detailsView.setVisibility(VISIBLE);
        if (restSearch) {
            seacrhView.hideMenu();
            seacrhView.setseacrhType(null);
        } else {
            seacrhView.hideKeyboard();
        }
        detailsView.setPoi(poi);
        String text = poi.getpoiDescription();
        if (text != null) {
            title = text;
            notifyTitleChagne();
        }
    }

    @Override
    public void onShowClickListener(IPoi poi) {
        title = "Maps and Directions";
        notifyTitleChagne();
        if (poi != null) {
            detailsView.setVisibility(GONE);
            seacrhView.hideMenu();
            seacrhView.setseacrhType(null);
            mapView.showPoi(poi);
        }
    }

    @Override
    public void onParkingClickListener() {
        //  openParkingMenu();
    }

    @Override
    public void menuOpened() {
        closeParkingMenu();
        resetMapFilter();
    }

    private void resetMapFilter() {
        List<PoiCategory> allceategories = SpreoSearchDataHolder.getInstance().getFilterCategories();
        if (allceategories != null) {
            PoisUtils.setPoiCategoriesVisible(allceategories);
            SpreoSearchDataHolder.getInstance().setVisibleCategories(allceategories);
            mapView.reDrawPois();
        }
    }

    @Override
    public void typeStarted() {
        closeParkingMenu();
    }

    @Override
    public void onCampusSelectionClick() {
        if (combinedViewListener != null) {
            try {
                combinedViewListener.onCampusSelectionClick();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void setCombinedViewListener(SpreoCombinedViewListener combinedViewListener) {
        this.combinedViewListener = combinedViewListener;
        notifyTitleChagne();
    }


    public void navigate(IPoi from, IPoi to, Callback callback) {
        if (from == null) {
            SettingsProvider.getInstance().setSimplifiedInstruction(false);
//            initNavigation(to);
            poiForInitNavigation = to;
            ILocation loc = SpreoLocationProvider.getInstance().getUserLocation();
            if (loc != null && loc.getLocationType() == LocationMode.INDOOR_MODE) {
                startNavigation(from, to);
            } else {
                if (SpreoLocationProvider.getInstance().isInCampus(getCampusRadius())) {
                    startNavigation(null, to);
                } else {
                    startThirdPartyNavigation(to, callback);
                }
            }
        } else {
            startNavigation(from, to);
        }


        if (to != null) {
            SpreoAnalyticsUtility.sendReport(SpreoAnalyticsUtility.NAVIGATE_REPORT, to.getpoiDescription(), to.getFacilityID());
        }
    }



    @ReactMethod
    private void updateFloorPicker(Callback callback) {

        int fromFloor;

        if (toPoi != null) {
            fromFloor = getFloorConsiderNoLocationMode(fromPoi);
        } else {
            fromFloor = Integer.MIN_VALUE;
        }

        int toFloor = Integer.MIN_VALUE;
        if (toPoi != null) {
            ILocation loc = toPoi.getLocation();
            if (loc != null) {
                if (loc.getLocationType() == LocationMode.INDOOR_MODE) {
                    toFloor = (int)loc.getZ();
                } else {
                    toFloor = getEntranceFloor();
                }
            }
        }

        List<Integer> extraNavigationFloors = new ArrayList<>();
        HashMap<String, NavigationPath> navmap = RouteCalculationHelper.getInstance().getIndoorNavPaths();
        for (NavigationPath facilitypath : navmap.values()) {
            for (FloorNavigationPath floornav : facilitypath.getFullPath()) {
                if (!extraNavigationFloors.contains((int)floornav.getZ())) {
                    extraNavigationFloors.add((int)floornav.getZ());
                }
            }
        }
        List<SpreoNewFloorObject> dispayItemList = new ArrayList<>();
        List<SpreoNewFloorObject> allFloors = getAllFacilityFloors();

        for (SpreoNewFloorObject item : allFloors) {
            if (item.floorId == fromFloor || item.floorId == toFloor
                    || item.floorId == SpreoMapViewInstance.getInstance().getPresentFloor() || extraNavigationFloors.contains(item.floorId)) {
                dispayItemList.add(item);
            }
        }
        Gson gson = new Gson();
        String displayJsonStr= gson.toJson(dispayItemList,new TypeToken<List<SpreoNewFloorObject>>() {}.getType());
        WritableMap params = Arguments.createMap();
        params.putString("updateFloorId",displayJsonStr);
        /*reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("updateFloor", params);*/
        callback.invoke(params);
        //   mapView.showFloorWithId(((SpreoNewFloorAdapter) adapter).getItem(position).floorId);

    }

    @ReactMethod
    public void setHandicappedRouting(boolean value){
        SettingsProvider.getInstance().setHandicappedRouting(value);
    }

    @ReactMethod
    public void navigationCanceled() {
        gpsBool = true;
        reactContext.runOnUiQueueThread(new Runnable() {
            @Override
            public void run() {

                if (fromPoi != null) {
                    PropertyHolder.getInstance().setRotatingMapType(MapRotationType.NAVIGATION);
//            SpreoLocationProvider.getInstance().stopLocationService();
//            SpreoLocationProvider.getInstance().startLocationService(reactContext);
                    SpreoLocationProvider.getInstance().setSimulatedLocation(simulatedLocation);
                    setUserLocationVisibilty(false);
//                    mapView.showMyLocation();
                    startLocationCheck();
//                    checkLocation();
//                    realMute = PropertyHolder.getInstance().isNavigationInstructionsSoundMute();
//                    PropertyHolder.getInstance().setNavigationInstructionsSoundMute(realMute);
                }
                //updateFloorPicker(callback);
                List<ICustomMarker> mlist = new ArrayList<>();
                mapView.setCustomMarkers(mlist);
                toPoi = null;
                fromPoi = null;
//                elseLocation = false;
//                ifLocation = false;
                if (navigationState) {
                    if (simulationState) {
                        mapView.stopSimulation();
                        simulationState = false;
                    } else {
                        mapView.stopNavigation();
                    }
                }

                navigationState = false;
            }
        });


    }


    public void presentDestination(IPoi destination) {
        if (destination != null) {
            toPoi=destination;
            String id = destination.getPoiID();
            if (id != null) {
                IPoi dest = PoisUtils.getPoiById(id);
                if (dest != null) {
                    Bitmap bm = BitmapFactory.decodeResource(reactContext.getResources(),
                            R.drawable.map_destination_center_anchor);
                    if (bm != null) {
                        List<ICustomMarker> mlist = new ArrayList<>();
                        ICustomMarker cmarker = new SpreoCustomMarkerObj(dest, destinationMarkerId, bm);
                        mlist.add(cmarker);
                        mapView.setCustomMarkers(mlist);
                        ILocation loc = dest.getLocation();
                        mapView.presentLocation(loc);

//                    mapView.setIconForPoi(dest, bm);
//                    ILocation loc = dest.getLocation();
//                    mapView.presentLocation(loc);
                    }
                }
            }
        }
    }



    private void presentOrigin
            () {
        if (fromPoi != null) {
            ILocation loc = fromPoi.getLocation();
            if (loc != null) {
//                SpreoLocationProvider.getInstance().stopLocationService();
//                SpreoLocationProvider.getInstance().startLocationService(reactContext, loc);
                SpreoLocationProvider.getInstance().setSimulatedLocation(loc, simulation);
                presentOriginMarker(loc);
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        SpreoLocationProvider.getInstance().stopLocationService();
//                    }
//
//                }, 500);
            }
        }  else {
//            mapView.showMyLocation();
            startLocationCheck();
        }

    }

    Runnable mPresentOriginMarker = new Runnable() {
        @Override
        public void run() {
            presentOriginMarker();
        }
    };

    private void presentOriginMarker(ILocation loc) {
        setUserLocationVisibilty(false);
        Bitmap bm = BitmapFactory.decodeResource(reactContext.getResources(), R.drawable.map_origin_center_anchor);
        if (bm != null) {
            List<ICustomMarker> mlist = CustomMarkersManager.getInstance().getCustomMarkers();
            if (loc != null) {
                ICustomMarker cmarker = new SpreoCustomMarkerObj(loc, originMarkerId, bm);
                mlist.add(cmarker);
                mapView.setCustomMarkers(mlist);
            }
        }
        mapView.presentLocation(loc);
    }


    private void presentOriginMarker() {
        setUserLocationVisibilty(false);
        Bitmap bm = BitmapFactory.decodeResource(reactContext.getResources(), R.drawable.map_origin_center_anchor);
        if (bm != null) {
            List<ICustomMarker> mlist = CustomMarkersManager.getInstance().getCustomMarkers();
            ILocation loc = SpreoLocationProvider.getInstance().getUserLocation();
            if (loc != null) {
                ICustomMarker cmarker = new SpreoCustomMarkerObj(loc, originMarkerId, bm);
                mlist.add(cmarker);
                mapView.setCustomMarkers(mlist);
            }
        }
    }

    private void removeOriginMarker() {
        List<ICustomMarker> mlist = CustomMarkersManager.getInstance().getCustomMarkers();
        ICustomMarker toremove = null;
        if (mlist != null && !mlist.isEmpty()) {
            for (ICustomMarker o : mlist) {
                if (o.getId().equals(originMarkerId)) {
                    toremove = o;
                    break;
                }
            }
        }
        if (toremove != null) {
            mlist.remove(toremove);
            mapView.setCustomMarkers(mlist);
        }
    }


    public void presentDestination(String destination, SpreoDualMapView view) {
        if (destination != null) {
            String id = destination;
            if (id != null) {
                IPoi dest = PoisUtils.getPoiById(id);
                if (dest != null) {
                    Bitmap bm = BitmapFactory.decodeResource(reactContext.getResources(), R.drawable.map_destination_center_anchor);
                    if (bm != null) {
                        List<ICustomMarker> mlist = new ArrayList<>();
                        ICustomMarker cmarker = new SpreoCustomMarkerObj(dest, destinationMarkerId, bm);
                        mlist.add(cmarker);
                        view.setCustomMarkers(mlist);
                        ILocation loc = dest.getLocation();
                        view.presentLocation(loc);

//                    mapView.setIconForPoi(dest, bm);
//                    ILocation loc = dest.getLocation();
//                    mapView.presentLocation(loc);
                    }
                }
            }
        }
    }
    @ReactMethod
    public void setDistanceType(int distanceType){
        this.distanceType = distanceType;
    }

    @Override
    public String calculateDistance() {
        int fd = (int)mapView.getRouteDistance();
        int md = (int) ((int)fd * 0.3048);
        double t = Math.ceil(((md * 0.75) / 60 ));
        /* String time = String.format("%.1f", t); */
        String result = "";

        if(distanceType == 1){
            result =   fd + " ft, " + (int)t + " min";
        }else if(distanceType == 2){
            result =   md + " m, " + (int)t + " min";
        }else{
            result =  fd + " ft (" + md + "m), " + (int)t + " min";
        }

        return result;
    }


    @Override
    public void mapFilterApplied() {
        mapView.reDrawPois();
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }



    public void notifyUserAboutZone(boolean enter, String id) {
//        List<GeofenceContent> content = GeofenceContentManager.getInstance().getContentByTriggerId(id);
//        if(content != null && enter) {
//            ZoneContentDialog.show(content);
//        } else {
//            Toast.makeText(reactContext, (enter ? "Entering zone: " : "Leaving zone: ") + id + (enter ? " (no content)" : ""), Toast.LENGTH_LONG).show();
//        }
        if (debugMode) {
            Toast.makeText(reactContext, (enter ? "Entering zone: " : "Leaving zone: ") + id, Toast.LENGTH_LONG).show();
        }
    }

    public void onCreate() {
        mapView.onCreate();
        resetNavigation();
    }

    @Override
    public void onPostDownload(GalleryUpdateStatus status) {

    }

    private class DownloadFacilityImageTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... req) {
            String projid = PropertyHolder.getInstance().getProjectId();
            String campusid = SpreoDataProvider.getCampusId();
            String imgaekey = getImagekey(projid, campusid);
            try {
                if (imgaekey != null) {
                    URL url;
                    url = new URL(PropertyHolder.getInstance()
                            .getServerName()
                            + "res/"
                            + projid
                            + "/"
                            + campusid
                            + "/"
                            + "homeicons/" + imgaekey);
                    URLConnection conn = url.openConnection();

                    HttpURLConnection httpConn = (HttpURLConnection) conn;
                    httpConn.setRequestMethod("GET");
                    httpConn.connect();

                    InputStream is = new BufferedInputStream(httpConn.getInputStream());
                    if (is != null) {
                        Bitmap strlogo = BitmapFactory.decodeStream(is);
                        if (detailsView != null) {
                            detailsView.setFacilityLogo(strlogo);
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

        private String getImagekey(String projid, String campusid) {
            String result = null;
            try {
                URL url = new URL(PropertyHolder.getInstance()
                        .getServerName()
                        + "res/"
                        + projid
                        + "/"
                        + campusid
                        + "/homeactions.json");

                URLConnection conn = url.openConnection();

                HttpURLConnection httpConn = (HttpURLConnection) conn;
                httpConn.setRequestMethod("GET");
                httpConn.connect();

                InputStream is = httpConn.getInputStream();
                String strjson = convertStreamToString(is);
                JSONObject obj = new JSONObject(strjson);
                JSONObject homeinfos = obj.getJSONObject("homescreeninfos");
                result = homeinfos.getString("facimage");

            } catch (Throwable t) {
                t.printStackTrace();
            }
            return result;
        }

        private String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                //e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
            return sb.toString();
        }
    }


    private void closeParkingMenu() {
        removeParkingMenu.setVisibility(GONE);
        addParkingMenu.setVisibility(GONE);
        title = "Maps and Directions";
        notifyTitleChagne();

    }

    private void startThirdPartyNavigation(final IPoi to, Callback callback) {
        final LatLng latlng = getLatLngForNavApps(to);
        toPoi= to;
        if (latlng != null) {
            final String uri = "geo:0,0?q=" + latlng.latitude + "," + latlng.longitude;
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            List<ResolveInfo> resInfos = reactContext.getPackageManager().queryIntentActivities(mapsIntent, 0);
            if (resInfos != null && !resInfos.isEmpty()) {
                ArrayList<SpreoAppInfoObject> spreoAppInfoObjects = new ArrayList<>();
                SpreoAppInfoObject spreoAppInfoObject;
                appsmap = new HashMap<>();
                for (ResolveInfo o : resInfos) {

                    CharSequence appname = getAppName(o);
                    CharSequence appIcon = getAppIcon(o);
                    if (appname != null) {
                        String aname =  appname.toString();
                        String aicon =  appIcon.toString();
                        if(!aname.isEmpty()) {
                            appsmap.put(aname, o);
                            spreoAppInfoObject=new SpreoAppInfoObject(aname,aicon);
                            spreoAppInfoObjects.add(spreoAppInfoObject);
                        }
                    }

                }

                Type listType = new TypeToken<List<SpreoAppInfoObject>>(){}.getType();
                Gson gson = new Gson();
                String appInfo = gson.toJson(spreoAppInfoObjects, listType);
                WritableMap params = Arguments.createMap();
                params.putString("appInfo", appInfo);
                callback.invoke(appInfo);

            }
        }
    }

    private String getAppIcon(ResolveInfo resolveInfo){
        Drawable appIcon;
        appIcon = resolveInfo.loadIcon(reactContext.getPackageManager());
        Bitmap bitmap = getBitmapFromDrawable(appIcon);
        String encodedImage = encodeToBase64(bitmap, Bitmap.CompressFormat.PNG, 100);
        return encodedImage;
    }

    @NonNull
    static private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    @ReactMethod
    private void  navigateThirdParty(String name){
        final LatLng latlng = getLatLngForNavApps(toPoi);
        final String uri = "geo:0,0?q=" + latlng.latitude + "," + latlng.longitude;
        if (name != null) {
            ResolveInfo resInfo = appsmap.get(name);
            String packageName = resInfo.activityInfo.packageName;
            if (packageName.contains("google")) {
                String googleuri = "google.navigation:q=" + latlng.latitude + "," + latlng.longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleuri));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                reactContext.startActivity(intent);
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                reactContext.startActivity(intent);
            }

            thirdPartyNavigation = true;
        }

    }


    private void initNavigation(final IPoi to) {
        ILocation loc = SpreoLocationProvider.getInstance().getUserLocation();
        if (loc != null && loc.getLocationType() == LocationMode.OUTDOOR_MODE) {
            if (SpreoLocationProvider.getInstance().isInCampus(getCampusRadius())) {
                final Dialog dialog = new Dialog(reactContext);
                dialog.setContentView(R.layout.spreo_dialog_navigation);
                dialog.setCancelable(false);
                TextView dialogContinue = (TextView) dialog.findViewById(R.id.dialogContinue);
                dialogContinue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (to != null) {
                            dialog.dismiss();
                            if (to != null) {
                                startNavigation(null, to);
                            }
                        }
                    }
                });

                TextView dialogStartingPoint = (TextView) dialog.findViewById(R.id.dialogStartingPoint);
                dialogStartingPoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (to != null) {
                            fromToView.setTo(to);
                            //  presentDestination(to, view);
                            fromToView.showFromEdit();
                            //  openFromTo();
                        }
                    }
                });

                TextView dialogCancel = (TextView) dialog.findViewById(R.id.dialogCancel);
                dialogCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (to != null) {
                            fromToView.setTo(to);
                            // presentDestination(to, view);
                            // openFromTo();
                        }
                    }
                });

                dialog.show();
            } else {
                final LatLng latlng = getLatLngForNavApps(to);
                if (latlng != null) {
                    final String uri = "geo:0,0?q=" + latlng.latitude + "," + latlng.longitude;
                    Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    List<ResolveInfo> resInfos = reactContext.getPackageManager().queryIntentActivities(mapsIntent, 0);
                    if (resInfos != null && !resInfos.isEmpty()) {
                        ArrayList<String> appsnamelist = new ArrayList<>();
                        final HashMap<String, ResolveInfo> appsmap = new HashMap<>();
                        for (ResolveInfo o : resInfos) {
                            CharSequence appname = getAppName(o);
                            if (appname != null) {
                                String aname =  appname.toString();
                                if(!aname.isEmpty()) {
                                    appsmap.put(aname, o);
                                    appsnamelist.add(aname);
                                }
                            }

                        }

                        final Dialog dialog = new Dialog(reactContext);
                        dialog.setContentView(R.layout.spreo_dialog_outofcampus_navigation);
                        dialog.setCancelable(false);

                        ListView appslist = (ListView) dialog.findViewById(R.id.appsList);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(reactContext, R.layout.spreo_appslist_item, appsnamelist);
                        appslist.setAdapter(adapter);
                        appslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String name = (String)parent.getItemAtPosition(position);
                                dialog.dismiss();
                                if (to != null) {
                                    fromToView.setTo(to);
                                    // presentDestination(to, view);
                                    //openFromTo();
                                }
                                if (name != null) {
                                    ResolveInfo resInfo = appsmap.get(name);
                                    String packageName = resInfo.activityInfo.packageName;
                                    if (packageName.contains("google")) {
                                        String googleuri = "google.navigation:q=" + latlng.latitude + "," + latlng.longitude;
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleuri));
                                        intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                                        reactContext.startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                        intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                                        reactContext.startActivity(intent);
                                    }

                                    thirdPartyNavigation = true;
                                }
                            }
                        });


                        TextView dialogCancel = (TextView) dialog.findViewById(R.id.dialogCancel);
                        dialogCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                if (to != null) {
                                    fromToView.setTo(to);
                                    //   presentDestination(to, view);
                                    // openFromTo();
                                }
                            }
                        });

                        dialog.show();
                    }
                }

            }

        } else {
            if (to != null) {
                startNavigation(null, to);
            }
        }
    }

    private CharSequence getAppName(ResolveInfo resolveInfo){
        CharSequence appName = "";
        appName = resolveInfo.loadLabel(reactContext.getPackageManager());
        return appName;
    }




    private void closeNavView() {
        if (fromPoi != null) {
            staticNavView.setVisibility(GONE);
            staticNavView.setText("");
        } else {
            liveNavView.setVisibility(GONE);
        }
    }

    private void startNavigation(IPoi from, final IPoi to) {
        gpsBool = true;
        if (from == null) {
            toPoi = to;
            //  updateFloorPicker(SpreoMapViewInstance.getInstance().getPresentFloor());
            if (PropertyHolder.getInstance().isSimulationMode()) {
                mapView.simulateNavigationTo(to);
                simulationState = true;
//                mapView.showMyLocation();
                startLocationCheck();
            } else {
                presentOrigin();
                mapView.navigateTo(to);
            }
            navigationState = true;
        } else {
            toPoi = to;
            fromPoi = from;
            navigationState = true;
            PropertyHolder.getInstance().setRotatingMapType(MapRotationType.STATIC);
            //   updateFloorPicker(SpreoMapViewInstance.getInstance().getPresentFloor());
            presentOrigin();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mapView.navigateTo(to);
                }
            }, 1000);


        }


    }


    public void setPoiForNavigation(String poiID) {
        IPoi poi = PoisUtils.getPoiById(poiID);
        if (poi != null) {
            onGoClickListener(poi);
        }
    }

//    public void setPoiForDetails(String poiID) {
//        IPoi poi = PoisUtils.getPoiById(poiID);
//        if (poi != null) {
//            showPoiDetails(poi, false);
//        }
//    }



    private void setParkingButtonsAvailability(boolean available) {
        markParkingBtn.setEnabled(available);
        TakeMeParkingBtn.setEnabled(available);
        if (available) {
            parkingDisableMsg1.setVisibility(GONE);
            parkingDisableMsg2.setVisibility(GONE);
        } else {
            parkingDisableMsg1.setVisibility(VISIBLE);
            parkingDisableMsg2.setVisibility(VISIBLE);
        }
    }

    private LatLng getLatLngForNavApps(IPoi to) {
        LatLng result = ProjectConf.getInstance().getSelectedCampus().getDefaultLatlng();
        if (to != null) {
            IPoi prk = ProjectConf.getInstance().getClosestParking(to);
            if (prk != null) {
                result = new LatLng(prk.getPoiLatitude(), prk.getPoiLongitude());
            }
        }
        return result;
    }

    private int getCampusRadius() {
        int result = 1000;
        Campus campus = ProjectConf.getInstance().getSelectedCampus();
        if (campus != null) {
            result = campus.getRadius();
        }
        return result;
    }

    private void closeMenu() {
        seacrhView.hideMenu();
    }

    private void clean() {
        SpreoSearchDataHolder.releaseInstance();
    }

    public void setMapType(int type) {
        mapView.setMapType(type);
    }



}