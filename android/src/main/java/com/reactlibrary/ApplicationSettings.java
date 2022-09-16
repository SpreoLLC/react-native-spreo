package com.reactlibrary;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.mlins.nav.location.sharing.ReportLimitation;
import com.mlins.project.ProjectConf;
import com.mlins.utils.PropertyHolder;
import com.mlins.utils.gis.Location;
import com.spreo.nav.enums.MapRotationType;
import com.spreo.nav.interfaces.ILocation;
import com.spreo.sdk.location.SpreoLocationProvider;
import com.spreo.sdk.setting.SettingsProvider;


public class ApplicationSettings {

    private static ApplicationSettings instance = null;

    private static final String KEY_DEBUG_MODE = "KEY_DEBUG_MODE";

    private final String APPLICATION_SETTINGS_KEY = "applicationSettings";
    private final String PROJECT_SETTINGS_KEY = "projectSettingsFor:";
    private final String KEY_USER_LOGIN_FROM_LOGIN_SCREEN = "savedUserLogin";
    private final String KEY_USER_PASSWORD_FROM_LOGIN_SCREEN = "savedUserPassword";
    private final String KEY_SAVED_PROJECT = "savedProject";
    private final String KEY_SAVED_SIMULATION_MODE = "savedSimulationModeInSetting";
    private final String KEY_DISPLAY_TOP_FLOOR = "shouldDisplayTopFloorContent";
    private final String KEY_DISPLAY_LABELS = "displayLabelsForPOIs";

    private final String KEY_SIMULATION_MODE = "simulationMode";
    private final String KEY_SIMPLIFIED_INSTRUCTIONS = "simplifiedInstructions";
    private final String KEY_MUTE_INSTRUCTIONS = "muteInstructions";
    private final String KEY_LOCATION_ANALYTICS = "locationAnalyticsEnabled";
    private final String KEY_MAP_ORIENTATION = "mapOrientation";
    private MapRotationType defaultMapOrientation = MapRotationType.NAVIGATION;
    private final String KEY_MAP_TYPE = "mapType";
    private int defaultMapType = GoogleMap.MAP_TYPE_NORMAL;
    private final String KEY_DATA_ANALYTICS = "dataAnalyticsEnabled";
    private final String KEY_REPORT_LIMITATION = "reportLimitation";
    private ReportLimitation defaultReportLimitation = ReportLimitation.IN_CAMPUS;
    private final String KEY_DEFAULT_LOCATION = "defaultLocation";
    private final String KEY_simulate_LOCATION = "simulateLocation";
    private final String KEY_APP_LANGUAGE = "appLanguage";
    private String defaultAppLanguage = "english";


    private SharedPreferences applicationPreferences;
    private SharedPreferences.Editor editor;
    private ProjectSettings projectSettings;

//    private final Context context = null;

    public static ApplicationSettings getInstance() {

        if (instance == null) {
            instance = new ApplicationSettings();
        }
        return instance;

    }

    public ApplicationSettings() {

    }

    public void init(@NonNull Context context) {

//        context = context;

        this.applicationPreferences = context.getSharedPreferences(APPLICATION_SETTINGS_KEY, Context.MODE_PRIVATE);
        this.editor = getEditor();

        loadSavedSettings(context);
    }

    public void saveUserLoginFromLoginScreen(String loginFromLoginScreen) {

        editor.putString(KEY_USER_LOGIN_FROM_LOGIN_SCREEN, loginFromLoginScreen);
        editor.apply();
    }

    private void loadSavedSettings(Context context) {
        PropertyHolder.getInstance().setSimulationMode(isSimulationMode());
        PropertyHolder.getInstance().setSimplifiedInstruction(isSimplifiedInstruction());
        PropertyHolder.getInstance().setNavigationInstructionsSoundMute(isMuteInstruction());
        PropertyHolder.getInstance().setRotatingMapType(getMapOrientation());
        PropertyHolder.getInstance().setMapType(getMapType());
        SettingsProvider.getInstance().setLocationReportLimitation(getReportLimitation());
        SettingsProvider.getInstance().setRouteColor("#1b4297");
        if (isLocationAnalyticsEnabled()) {
            startLocationAnalytics();
        } else {
            stopLocationAnalytics();
        }
        SpreoAnalyticsUtility.enabled = isDataAnalyticsEnabled();
        if (!isDefaultLocation() && isSimulateLocation()) {
            ILocation loc = getLocationToSimulate(context);
            if (loc != null) {
                setLocationToSimulate(loc, context);
            }
        } else if (isDefaultLocation()) {
            SpreoLocationProvider.getInstance().setSimulatedLocation(getDefaultLocation());
        }
    }

    public void startLocationAnalytics() {
        SettingsProvider.getInstance().setUserAnalyticsEnable(true);
        SettingsProvider.getInstance().setAnalyticsInterval(20000);
    }

    public void stopLocationAnalytics() {
        SettingsProvider.getInstance().setUserAnalyticsEnable(false);
    }

    @Nullable
    public String getUserLoginFromLoginScreen() {

        return applicationPreferences.getString(KEY_USER_LOGIN_FROM_LOGIN_SCREEN, null);
    }

    public void saveUserPasswordFromLoginScreen(String passwordFromLoginScreen) {

        editor.putString(KEY_USER_PASSWORD_FROM_LOGIN_SCREEN, passwordFromLoginScreen);
        editor.apply();
    }

    @Nullable
    public String getUserPasswordFromLoginScreen() {

        return applicationPreferences.getString(KEY_USER_PASSWORD_FROM_LOGIN_SCREEN, null);
    }

    public void saveSelectedProject(String selectedProject) {

        editor.putString(KEY_SAVED_PROJECT, selectedProject);
        editor.apply();
    }

    @Nullable
    public String getLastSavedProject() {

        return applicationPreferences.getString(KEY_SAVED_PROJECT, null);
    }

    public void saveSimulationModeClassName(String modeName) {

        editor.putString(KEY_SAVED_SIMULATION_MODE, modeName);
        editor.apply();
    }

    @Nullable
    public String getSavedSimulationModeClassName() {

        return applicationPreferences.getString(KEY_SAVED_SIMULATION_MODE, null);
    }

//    public ProjectSettings getProjectSettings() {
//        return getProjectSettings(context);
//    }

    public ProjectSettings getProjectSettings(Context context) {

        if (projectSettings == null) {
            String projectId = PropertyHolder.getInstance().getProjectId();

            if(projectId == null)
                throw new IllegalStateException("Can't provide app settings: project id is null.");

            projectSettings = new ProjectSettings(context.getSharedPreferences(PROJECT_SETTINGS_KEY +
                    projectId, Context.MODE_PRIVATE));
        }

        return projectSettings;
    }


    private SharedPreferences.Editor getEditor() {

        return applicationPreferences.edit();
    }

    public boolean hasSavedCredentials(){
        return getUserLoginFromLoginScreen() != null && getUserPasswordFromLoginScreen() != null;
    }

    public boolean hasSavedProject(){
        return getLastSavedProject() != null;
    }

    public boolean shouldDisplayTopFloorContent(){
        return applicationPreferences.getBoolean(KEY_DISPLAY_TOP_FLOOR, false);
    }

    public void setShouldDisplayTopFloorContent(boolean mode) {
        applicationPreferences.edit().putBoolean(KEY_DISPLAY_TOP_FLOOR, mode).commit();
    }


        public boolean displayLabelsForPOIs(){
        return applicationPreferences.getBoolean(KEY_DISPLAY_LABELS, true);
    }

    public void setDisplayLabelsForPOIs(boolean mode) {
        applicationPreferences.edit().putBoolean(KEY_DISPLAY_LABELS, mode).commit();
    }



    public boolean isSimulationMode(){
        return applicationPreferences.getBoolean(KEY_SIMULATION_MODE, false);
    }

    public void setSimulationMode(boolean mode) {
        PropertyHolder.getInstance().setSimulationMode(mode);
        applicationPreferences.edit().putBoolean(KEY_SIMULATION_MODE, mode).commit();
    }

    public boolean isSimplifiedInstruction(){
        return applicationPreferences.getBoolean(KEY_SIMPLIFIED_INSTRUCTIONS, false);
    }

    public void setSimplifiedInstruction(boolean mode) {
        PropertyHolder.getInstance().setSimplifiedInstruction(mode);
        applicationPreferences.edit().putBoolean(KEY_SIMPLIFIED_INSTRUCTIONS, mode).commit();
    }

    public boolean isMuteInstruction(){
        return applicationPreferences.getBoolean(KEY_MUTE_INSTRUCTIONS, false);
    }

    public void setmuteInstruction(boolean mode) {
        PropertyHolder.getInstance().setNavigationInstructionsSoundMute(mode);
        applicationPreferences.edit().putBoolean(KEY_MUTE_INSTRUCTIONS, mode).commit();
    }


    public boolean isDataAnalyticsEnabled(){
        return applicationPreferences.getBoolean(KEY_DATA_ANALYTICS, false);
    }

    public void setDataAnalyticsEnabled(boolean enabled) {
        SpreoAnalyticsUtility.enabled = enabled;
        applicationPreferences.edit().putBoolean(KEY_DATA_ANALYTICS, enabled).commit();
    }

    public boolean isLocationAnalyticsEnabled(){
        return applicationPreferences.getBoolean(KEY_LOCATION_ANALYTICS, false);
    }

    public void setLocationAnalyticsEnabled(boolean enabled) {
        if(enabled) {
            startLocationAnalytics();
        } else {
            stopLocationAnalytics();
        }

        applicationPreferences.edit().putBoolean(KEY_LOCATION_ANALYTICS, enabled).commit();
    }



    public boolean isDebugMode(){
        return applicationPreferences.getBoolean(KEY_DEBUG_MODE, false);
    }

    public void setDebugMode(boolean debugMode) {
        applicationPreferences.edit().putBoolean(KEY_DEBUG_MODE, debugMode).commit();
    }

    public void setMapOrientation(MapRotationType type){
        PropertyHolder.getInstance().setRotatingMapType(type);
        applicationPreferences.edit().putString(KEY_MAP_ORIENTATION, type.toString()).commit();
    }

    public MapRotationType getMapOrientation(){
        String maporientation = applicationPreferences.getString(KEY_MAP_ORIENTATION, defaultMapOrientation.toString());
        return MapRotationType.valueOf(maporientation);
    }

    public void setMapType(int type){
        PropertyHolder.getInstance().setMapType(type);
        applicationPreferences.edit().putInt(KEY_MAP_TYPE, type).commit();
    }

    public int getMapType(){
        return applicationPreferences.getInt(KEY_MAP_TYPE, defaultMapType);
    }

    public void setReportLimitation(ReportLimitation type){
        SettingsProvider.getInstance().setLocationReportLimitation(type);
        applicationPreferences.edit().putString(KEY_REPORT_LIMITATION, type.toString()).commit();
    }

    public ReportLimitation getReportLimitation(){
        String reportlimitation = applicationPreferences.getString(KEY_REPORT_LIMITATION, defaultReportLimitation.toString());
        return ReportLimitation.valueOf(reportlimitation);
    }

    public boolean isDefaultLocation(){
        return applicationPreferences.getBoolean(KEY_DEFAULT_LOCATION, false);
    }

    public void setDefaultLocation(boolean mode) {
        applicationPreferences.edit().putBoolean(KEY_DEFAULT_LOCATION, mode).commit();
        if (mode == true) {
            SpreoLocationProvider.getInstance().setSimulatedLocation(getDefaultLocation());
        } else {
            SpreoLocationProvider.getInstance().setSimulatedLocation(null);
        }
    }

    public boolean isSimulateLocation(){
        return applicationPreferences.getBoolean(KEY_simulate_LOCATION, false);
    }

    public void setSimulateLocation(boolean mode) {
        applicationPreferences.edit().putBoolean(KEY_simulate_LOCATION, mode).commit();
    }

    public void setLocationToSimulate(ILocation loc, Context context) {
        if (loc != null) {
            ProjectSettings projectSettings = getProjectSettings(context);
            projectSettings.saveLastCustomLocationForSimulation(loc);
            setSimulateLocation(true);
        } else {
            setSimulateLocation(false);
        }
        SpreoLocationProvider.getInstance().setSimulatedLocation(loc);
    }

    public ILocation getLocationToSimulate(Context context) {
        ProjectSettings projectSettings = getProjectSettings(context);
        return projectSettings.getLastCustomLocationFromSimulation();
    }

    public Location getDefaultLocation(){
        LatLng defaultLatLng = ProjectConf.getInstance().getSelectedCampus().getDefaultLatlng();
        Location result = new Location(defaultLatLng);
        result.setCampusId(ProjectConf.getInstance().getSelectedCampus().getId());
        return result;
    }


    public void saveAppLanguage(String language){
        applicationPreferences.edit().putString(KEY_APP_LANGUAGE, language).commit();
    }

    public String getDefaultAppLanguage(){
        return defaultAppLanguage;
    }

    public String getAppLanguageKey() {
        return KEY_APP_LANGUAGE;
    }

    public String getAppSettingsKey() {
        return APPLICATION_SETTINGS_KEY;
    }

//    public String getAppLanguage(){
//       return applicationPreferences.getString(KEY_APP_LANGUAGE, defaultAppLanguage);
//    }
}
