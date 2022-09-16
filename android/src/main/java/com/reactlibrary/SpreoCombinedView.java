package com.reactlibrary;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.mlins.custom.markers.CustomMarkersManager;
import com.mlins.locationutils.LocationFinder;
import com.mlins.project.Campus;
import com.mlins.project.ProjectConf;
import com.mlins.utils.FacilityConf;
import com.mlins.utils.MathUtils;
import com.mlins.utils.PropertyHolder;
import com.spreo.enums.NavigationResultStatus;
import com.spreo.geofence.GeoFenceObject;
import com.spreo.geofence.ZoneDetection;
import com.spreo.interfaces.ICustomMarker;
import com.spreo.interfaces.SpreoDualMapViewListener;
import com.spreo.interfaces.SpreoNavigationListener;
import com.spreo.nav.enums.LocationMode;
import com.spreo.nav.enums.NavigationState;
import com.spreo.nav.interfaces.ILabel;
import com.spreo.nav.interfaces.ILocation;
import com.spreo.nav.interfaces.INavInstruction;
import com.spreo.nav.interfaces.IPoi;
import com.spreo.sdk.data.SpreoDataProvider;
import com.spreo.sdk.geofencing.GeoFencingUtils;
import com.spreo.sdk.location.SpreoLocationProvider;
import com.spreo.sdk.poi.PoiCategory;
import com.spreo.sdk.poi.PoisUtils;
import com.spreo.sdk.setting.SettingsProvider;
import com.spreo.sdk.view.SpreoDualMapView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SpreoCombinedView extends  FrameLayout implements SpreoDualMapViewListener, SpreoNavigationListener, SpreoSearchClickListener, SpreoFromToListener, SpreoNavViewListener, MapFilterListener, ZoneDetection {

    private SpreoDualMapView mapView;
    private Context ctx = null;
    private RecyclerView recyclerView;
    private RelativeLayout floorPickerLL;
    private int presentFloor = 0;
    private ImageView showmylocation, startNav, selectCampusButton, mapFilterButton;
    private SpreoSearchView seacrhView;
    private SpreoDetailsView detailsView;
    private SpreoCombinedViewListener combinedViewListener = null;
    private String title = "Maps and Directions";
    private Button closeDetailsBtn, markParkingBtn, cancelParkingBtn, TakeMeParkingBtn, deleteParkingBtn, closeParkingBtn ;
    private LinearLayout addParkingMenu, removeParkingMenu;
    private SpreoFromToView fromToView;
    private SpreoStaticNavView staticNavView;
    private IPoi toPoi = null;
    private IPoi fromPoi = null;
    private boolean navigationState = false;
    private TextView parkingDisableMsg1, parkingDisableMsg2, staticMsg;
    private IPoi preDefinedDestination = null;
    private boolean locationVisible = false;
    private boolean thirdPartyNavigation = false;
    private IPoi poiForInitNavigation = null;
    private SpreoFilterDialog spreoFilterDialog = null;
    private String originMarkerId = ICustomMarker.navPrefix + "origin";
    private String destinationMarkerId = ICustomMarker.navPrefix + "dest";
    private SpreoLiveNavView liveNavView;
    private boolean simulationState = false;
    private boolean debugMode = false;
    private static final String ZONE_TYPE = "zone";
    private ILocation simulatedLocation = null;

    public SpreoCombinedView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        clean();

        ctx = context;

        SettingsProvider.getInstance().setFloorPickerVisibility(false);
        SettingsProvider.getInstance().setDisplayNavigationInstructionsOnMap(false);
        SettingsProvider.getInstance().setDisplayLabelsForPois(true);
//        SettingsProvider.getInstance().setMapRotation(MapRotationType.NAVIGATION);
        SettingsProvider.getInstance().setDrawInvisibleFloorsRoute(true);
        SettingsProvider.getInstance().setClickableDynamicBubbles(true);
        SettingsProvider.getInstance().setDrawInvisibleNavMarkers(true);
        SettingsProvider.getInstance().setDrawRouteTails(true);
        SettingsProvider.getInstance().setVirtualRouteAlpha(0.4f);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.spreo_combined_view, this, true);


        mapView = (SpreoDualMapView) findViewById(R.id.map1);

        mapView.registerListener(this);

        mapView.registerNavigationListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_floor);

        floorPickerLL = (RelativeLayout) findViewById(R.id.floor_picker_LL);

        showmylocation = (ImageView) findViewById(R.id.showMyLocation);
        showmylocation.setOnClickListener(showMyLocationListener);
        startNav = (ImageView) findViewById(R.id.startNav);
        startNav.setOnClickListener(startNavListener);
        selectCampusButton = (ImageView) findViewById(R.id.selectCampusButton);
        selectCampusButton.setOnClickListener(selectCampusButtonListener);

        mapFilterButton = (ImageView) findViewById(R.id.mapFilterButton);
        mapFilterButton.setOnClickListener(mapFilterButtonListener);

        fromToView = (SpreoFromToView) findViewById(R.id.fromtoview);
        fromToView.setFromToListener(this);
        seacrhView = (SpreoSearchView) findViewById(R.id.searchview);
        seacrhView.setSearchClickListener(this);
        detailsView = (SpreoDetailsView) findViewById(R.id.details);
        detailsView.setSearchClickListener(this);
        closeDetailsBtn = (Button) findViewById(R.id.closeDetailsBtn);
        closeDetailsBtn.setOnClickListener(closeDetailsListener);
        addParkingMenu = (LinearLayout) findViewById(R.id.addParkingMenu);
        removeParkingMenu = (LinearLayout) findViewById(R.id.removeParkingMenu);

        markParkingBtn = (Button) findViewById(R.id.markParkingBtn);
        markParkingBtn.setOnClickListener(markParkingListener);
        cancelParkingBtn = (Button) findViewById(R.id.cancelParkingBtn);
        cancelParkingBtn.setOnClickListener(cancelParkingListener);
        TakeMeParkingBtn = (Button) findViewById(R.id.TakeMeParkingBtn);
        TakeMeParkingBtn.setOnClickListener(TakeMeParkingListener);
        deleteParkingBtn = (Button) findViewById(R.id.deleteParkingBtn);
        deleteParkingBtn.setOnClickListener(deleteParkingListener);
        closeParkingBtn = (Button) findViewById(R.id.closeParkingBtn);
        closeParkingBtn.setOnClickListener(closeParkingListener);

        staticNavView = (SpreoStaticNavView) findViewById(R.id.staticNavView);
        staticNavView.setFromToListener(this);
        staticNavView.setNavViewListener(this);

        liveNavView = (SpreoLiveNavView) findViewById(R.id.liveNavView);
        liveNavView.setFromToListener(this);
        liveNavView.setNavViewListener(this);

        parkingDisableMsg1 = (TextView) findViewById(R.id.parkingDisableMsg1);
        parkingDisableMsg2 = (TextView) findViewById(R.id.parkingDisableMsg2);

        staticMsg = (TextView) findViewById(R.id.staticMsg);

        DownloadFacilityImageTask dft = new DownloadFacilityImageTask();
        dft.execute();

        List<String> glist = new ArrayList<>();
        glist.add(ZONE_TYPE);
        GeoFencingUtils.subscribeToService(this, glist);
    }

    public void setSimulatedLocation(ILocation location) {
        simulatedLocation = location;
    }


    private OnClickListener markParkingListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            markMyParking();
        }
    };

    private void markMyParking() {
        mapView.setCurrentLocationAsParking();
        openParkingMenu();
    }

    private OnClickListener cancelParkingListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            closeParkingMenu();
        }
    };

    private OnClickListener TakeMeParkingListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            closeParkingMenu();
            startNavigationToParking();
            liveNavView.setParkingNav();
        }
    };

    private OnClickListener deleteParkingListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mapView.removeParkingLocation();
            openParkingMenu();
        }
    };

    private OnClickListener closeParkingListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            closeParkingMenu();
        }
    };

    private OnClickListener closeDetailsListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            title = "Maps and Directions";
            notifyTitleChagne();
            detailsView.setVisibility(View.GONE);
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

    @Override
    public void mapDidLoad() {
        mapView.showSwitchFloorMarkers(true);
        Bitmap parkingbm = BitmapFactory.decodeResource(getResources(), R.drawable.my_parking);
        mapView.setMyParkingMarkerIcon(parkingbm);
        Bitmap locbm = BitmapFactory.decodeResource(getResources(), R.drawable.blue_dot_2);
        mapView.setUserIcon(locbm);
    }

    private OnClickListener mapFilterButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            openMapFilter();
        }
    };

    private void openMapFilter() {
        spreoFilterDialog = new SpreoFilterDialog();
        spreoFilterDialog.showMapFilter(ctx, this);
    }

    private OnClickListener selectCampusButtonListener = new OnClickListener() {

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

    private OnClickListener showMyLocationListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            mapView.showMyLocation();
        }
    };

    private OnClickListener startNavListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (navigationState) {
                navigationCanceled();
            } else if (fromToView.getVisibility() == VISIBLE) {
                fromToView.close();
                navigationCanceled();
            } else {
                seacrhView.hideFavoritesMsg();
                closeParkingMenu();
                initFromTo();
                seacrhView.hideMenu();
            }
        }
    };

    private void initFromTo() {
        fromToView.showToEdit();
        openFromTo();
    }

    private void updateFloorPicker(int selectedFloorId) {
        boolean expandAble = true;
        List<SpreoNewFloorObject> allFloors = getAllFacilityFloors();

        int fromFloor;

        if (toPoi != null) {
            fromFloor = getFloorConsiderNoLocationMode(fromPoi);
            expandAble = false;
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


        SpreoNewFloorAdapter newFloorAdapter = new SpreoNewFloorAdapter(allFloors, selectedFloorId, fromFloor, toFloor, false, expandAble, new SpreoNewFloorAdapter.ItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.Adapter adapter, int position) {
                mapView.showFloorWithId(((SpreoNewFloorAdapter) adapter).getItem(position).floorId);
            }
        });

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(ctx, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(newFloorAdapter);
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
        mapView.unregisterListener(this);
        mapView.unregisterNavigationListener(this);
        GeoFencingUtils.unSubscribeFromService(this);
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
    public void onPoiClick(IPoi poi) {
        closeMenu();
        if (poi != null) {
            mapView.openPoiBubble(poi);
        }
    }

    @Override
    public void onBubbleClick(IPoi poi) {
        closeMenu();
        if (poi != null) {
            showPoiDetails(poi, true);
            mapView.closeBubble(poi);
        }
    }

    @Override
    public View aboutToOpenBubble(IPoi iPoi) {
        return null;
    }

    @Override
    public View aboutToOpenParkingBubble() {
        return null;
    }

    @Override
    public void onMyParkingMarkerClick() {
        closeMenu();
    }

    @Override
    public void onMyParkingBubbleClick() {

    }

    @Override
    public void onUserlocationClick() {
        closeMenu();
    }

    @Override
    public void onUserLocationBubbleClick() {

    }

    @Override
    public void onLabelClick(ILabel iLabel) {
        closeMenu();
    }

    @Override
    public void onMapClick(LatLng latLng, String s, int i) {
        closeMenu();
    }

    @Override
    public void onMapLongClick(LatLng latLng, String s, int i) {

    }

    @Override
    public void mapDidLoadFloor(String campusId, String facilityId, int floorId) {

    }


    @Override
    public void OnFloorChange(int floorId) {
        presentFloor = floorId;
        updateFloorPicker(presentFloor);
        floorPickerLL.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.slide_in_left_fl));
    }

    @Override
    public void onMultipointClick(IPoi iPoi) {

    }

    @Override
    public void onZoomChange(float v) {

    }

    @Override
    public void onMapLongClick(ILocation iLocation) {

    }

    @Override
    public View aboutToOpenUserBubble() {
        return null;
    }

    @Override
    public void onNavigationStateChanged(NavigationState navigationState) {
        if (navigationState == NavigationState.STARTED) {
                openNavView();
            updateFloorPicker(presentFloor);
        } else if (navigationState == NavigationState.DESTINATION_REACHED) {
            navigationCanceled();
        } else if (navigationState == NavigationState.STOPED) {
            updateFloorPicker(presentFloor);
        }
    }

    @Override
    public void onNavigationArriveToPoi(IPoi iPoi, List<IPoi> list) {

    }

    @Override
    public void OnNavigationInstructionChanged(INavInstruction iNavInstruction) {
        if (fromPoi == null) {
            liveNavView.setInstruction(iNavInstruction);
        }
    }

    @Override
    public void onInstructionRangeEntered(INavInstruction iNavInstruction) {

    }

    @Override
    public void OnNavigationFailed(NavigationResultStatus navigationResultStatus) {

    }


    @Override
    public void onGoClickListener(IPoi poi) {
        if (navigationState) {
            navigationCanceled();
        }
        seacrhView.hideMenu();
        seacrhView.setseacrhType(null);
        detailsView.setVisibility(View.GONE);
        if (poi != null) {
            SpreoSearchDataHolder.getInstance().addHistory(poi);
            fromToView.setTo(poi);
            presentDestination(poi);
            openFromTo();
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
        detailsView.setVisibility(View.VISIBLE);
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
            detailsView.setVisibility(View.GONE);
            seacrhView.hideMenu();
            seacrhView.setseacrhType(null);
            mapView.showPoi(poi);
        }
    }

    @Override
    public void onParkingClickListener() {
        openParkingMenu();
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

    @Override
    public void navigate(IPoi from, IPoi to) {
        SpreoSearchDataHolder.getInstance().addHistory(to);
        if (from == null) {
//            initNavigation(to);
            poiForInitNavigation = to;
            ILocation loc = SpreoLocationProvider.getInstance().getUserLocation();
            if (loc != null && loc.getLocationType() == LocationMode.INDOOR_MODE) {
                startNavigation(from, to);
            } else {
                if (SpreoLocationProvider.getInstance().isInCampus(getCampusRadius())) {
                    startNavigation(from, to);
                } else {
                    startThirdPartyNavigation(to);
                }
            }
        } else {
            startNavigation(from, to);
        }


        if (to != null) {
            SpreoAnalyticsUtility.sendReport(SpreoAnalyticsUtility.NAVIGATE_REPORT, to.getpoiDescription(), to.getFacilityID());
        }
    }

    @Override
    public void navigationCanceled() {
        if (fromPoi != null) {
//            SpreoLocationProvider.getInstance().stopLocationService();
//            SpreoLocationProvider.getInstance().startLocationService(ctx);
            SpreoLocationProvider.getInstance().setSimulatedLocation(simulatedLocation);
            setUserLocationVisibilty(true);
            mapView.showMyLocation();
        }
        seacrhView.setVisibility(View.VISIBLE);
        title = "Maps and Directions";
        notifyTitleChagne();
        setNavButtonImage(false);
        List<ICustomMarker> mlist = new ArrayList<>();
        mapView.setCustomMarkers(mlist);
        closeNavView();
        toPoi = null;
        fromPoi = null;
        updateFloorPicker(presentFloor);
        staticNavView.hideInstructionsList();
        staticNavView.setVisibility(View.GONE);
        staticMsg.setVisibility(View.GONE);
        liveNavView.setVisibility(View.GONE);
        liveNavView.clear();

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

    private void presentOrigin () {
        if (fromPoi != null) {
            ILocation loc = fromPoi.getLocation();
            if (loc != null) {
//                SpreoLocationProvider.getInstance().stopLocationService();
//                SpreoLocationProvider.getInstance().startLocationService(ctx, loc);
                SpreoLocationProvider.getInstance().setSimulatedLocation(loc, true);
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
            mapView.showMyLocation();
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
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.map_origin_center_anchor);
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
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.map_origin_center_anchor);
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

    @Override
    public void presentDestination(IPoi destination) {
        if (destination != null) {
            String id = destination.getPoiID();
            if (id != null) {
                IPoi dest = PoisUtils.getPoiById(id);
                if (dest != null) {
                    Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.map_destination_center_anchor);
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

    @Override
    public String calculateDistance() {
        int md = (int)mapView.getRouteDistance();
        int fd = (int) MathUtils.metersToFeet(md);
        double t = Math.ceil(((md * 0.75) / 60 ));
//        String time = String.format("%.1f", t);
        String result = fd + " ft (" + md + "m), " + (int)t + " min";
        return result;
    }


    @Override
    public void mapFilterApplied() {
        mapView.reDrawPois();
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public void onZoneEnter(GeoFenceObject geoFenceObject) {
        notifyUserAboutZone(true, geoFenceObject.getId());
    }

    @Override
    public void onZoneExit(GeoFenceObject geoFenceObject) {
        notifyUserAboutZone(false, geoFenceObject.getId());
    }

    @Override
    public List<String> getListeningTo() {
        return null;
    }

    @Override
    public void setListeningTo(List<String> list) {

    }

    public void notifyUserAboutZone(boolean enter, String id) {
//        List<GeofenceContent> content = GeofenceContentManager.getInstance().getContentByTriggerId(id);
//        if(content != null && enter) {
//            ZoneContentDialog.show(content);
//        } else {
//            Toast.makeText(ctx, (enter ? "Entering zone: " : "Leaving zone: ") + id + (enter ? " (no content)" : ""), Toast.LENGTH_LONG).show();
//        }
        if (debugMode) {
            Toast.makeText(ctx, (enter ? "Entering zone: " : "Leaving zone: ") + id, Toast.LENGTH_LONG).show();
        }
    }

    public void onCreate() {
        mapView.onCreate();
        resetNavigation();
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

    private void openParkingMenu() {
        if (mapView.hasParkingLocation()) {
            addParkingMenu.setVisibility(View.GONE);
            removeParkingMenu.setVisibility(View.VISIBLE);
            ILocation parkloc = mapView.getParkingLocation();
            if (parkloc != null) {
                mapView.presentLocation(parkloc);
            }
        } else {
            removeParkingMenu.setVisibility(View.GONE);
            addParkingMenu.setVisibility(View.VISIBLE);
        }

        title = "My Parking";
        notifyTitleChagne();
    }

    private void closeParkingMenu() {
        removeParkingMenu.setVisibility(View.GONE);
        addParkingMenu.setVisibility(View.GONE);
        title = "Maps and Directions";
        notifyTitleChagne();

    }

    private void openFromTo() {
        seacrhView.setVisibility(View.GONE);
        fromToView.setVisibility(View.VISIBLE);
        title = "Navigation";
        notifyTitleChagne();
        setNavButtonImage(true);
    }

    private void setNavButtonImage(boolean on) {
        Bitmap bm = null;
        if (on) {
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_on);
        } else {
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_off);
        }
        if (bm != null) {
            startNav.setImageBitmap(bm);
        }
    }

    private void startThirdPartyNavigation(final IPoi to) {
        final LatLng latlng = getLatLngForNavApps(to);
        if (latlng != null) {
            final String uri = "geo:0,0?q=" + latlng.latitude + "," + latlng.longitude;
            Intent mapsIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
            List<ResolveInfo> resInfos = ctx.getPackageManager().queryIntentActivities(mapsIntent, 0);
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

                final Dialog dialog = new Dialog(ctx);
                dialog.setContentView(R.layout.spreo_dialog_outofcampus_navigation);
                dialog.setCancelable(false);

                ListView appslist = (ListView) dialog.findViewById(R.id.appsList);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, R.layout.spreo_appslist_item, appsnamelist);
                appslist.setAdapter(adapter);
                appslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String name = (String)parent.getItemAtPosition(position);
                        dialog.dismiss();
                        if (to != null) {
                            fromToView.setTo(to);
                            presentDestination(to);
                            openFromTo();
                        }
                        if (name != null) {
                            ResolveInfo resInfo = appsmap.get(name);
                            String packageName = resInfo.activityInfo.packageName;
                            if (packageName.contains("google")) {
                                String googleuri = "google.navigation:q=" + latlng.latitude + "," + latlng.longitude;
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(googleuri));
                                intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                                ctx.startActivity(intent);
                            } else {
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                                intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                                ctx.startActivity(intent);
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
                            presentDestination(to);
                            openFromTo();
                        }
                    }
                });

                dialog.show();
            }
        }
    }


    private void initNavigation(final IPoi to) {
        ILocation loc = SpreoLocationProvider.getInstance().getUserLocation();
        if (loc != null && loc.getLocationType() == LocationMode.OUTDOOR_MODE) {
            if (SpreoLocationProvider.getInstance().isInCampus(getCampusRadius())) {
                final Dialog dialog = new Dialog(ctx);
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
                            presentDestination(to);
                            fromToView.showFromEdit();
                            openFromTo();
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
                            presentDestination(to);
                            openFromTo();
                        }
                    }
                });

                dialog.show();
            } else {
                final LatLng latlng = getLatLngForNavApps(to);
                if (latlng != null) {
                    final String uri = "geo:0,0?q=" + latlng.latitude + "," + latlng.longitude;
                    Intent mapsIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                    List<ResolveInfo> resInfos = ctx.getPackageManager().queryIntentActivities(mapsIntent, 0);
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

                        final Dialog dialog = new Dialog(ctx);
                        dialog.setContentView(R.layout.spreo_dialog_outofcampus_navigation);
                        dialog.setCancelable(false);

                        ListView appslist = (ListView) dialog.findViewById(R.id.appsList);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, R.layout.spreo_appslist_item, appsnamelist);
                        appslist.setAdapter(adapter);
                        appslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String name = (String)parent.getItemAtPosition(position);
                                dialog.dismiss();
                                if (to != null) {
                                    fromToView.setTo(to);
                                    presentDestination(to);
                                    openFromTo();
                                }
                                if (name != null) {
                                    ResolveInfo resInfo = appsmap.get(name);
                                    String packageName = resInfo.activityInfo.packageName;
                                    if (packageName.contains("google")) {
                                        String googleuri = "google.navigation:q=" + latlng.latitude + "," + latlng.longitude;
                                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(googleuri));
                                        intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                                        ctx.startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                                        intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                                        ctx.startActivity(intent);
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
                                    presentDestination(to);
                                    openFromTo();
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
        appName = resolveInfo.loadLabel(ctx.getPackageManager());
        return appName;
    }


    private void openNavView() {
        if (fromPoi != null) {
            staticNavView.setVisibility(View.VISIBLE);
            staticMsg.setVisibility(View.VISIBLE);
            if (toPoi != null) {
                staticNavView.updateInstructions();
                String text = "Navigating to " + toPoi.getpoiDescription();
                if (text != null) {
                    staticNavView.setText(text);
                }
            }
        } else {
            liveNavView.setVisibility(View.VISIBLE);
            liveNavView.setPoi(toPoi);
        }
    }

    private void closeNavView() {
        if (fromPoi != null) {
            staticNavView.setVisibility(View.GONE);
            staticNavView.setText("");
            staticMsg.setVisibility(View.GONE);
        } else {
            liveNavView.setVisibility(View.GONE);
        }
    }

    private void startNavigationToParking() {
        presentOrigin();

        if (PropertyHolder.getInstance().isSimulationMode()) {
            ILocation origin = LocationFinder.getInstance().getCurrentLocation();
            if (origin != null) {
                mapView.simulateNavigationToParking(origin);
                mapView.showMyLocation();
            }
            simulationState = true;
        } else {
            mapView.navigateToParking();
        }

        navigationState = true;

    }

    private void startNavigation(IPoi from, final IPoi to) {

        if (from == null) {
            toPoi = to;
//            updateFloorPicker(presentFloor);
            if (PropertyHolder.getInstance().isSimulationMode()) {
                mapView.simulateNavigationTo(to);
                simulationState = true;
                mapView.showMyLocation();
            } else {
                presentOrigin();
                mapView.navigateTo(to);
            }
            navigationState = true;
        } else {
            toPoi = to;
            fromPoi = from;
            navigationState = true;
//            updateFloorPicker(presentFloor);
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

    private void setUserLocationVisibilty(boolean visible) {
        mapView.setUserLocationVisibilty(visible);
        locationVisible = visible;
    }


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
