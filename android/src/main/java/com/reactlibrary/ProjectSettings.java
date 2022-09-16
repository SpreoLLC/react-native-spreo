package com.reactlibrary;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.spreo.nav.enums.LocationMode;
import com.spreo.nav.interfaces.ILocation;

public class ProjectSettings {

    private static final String KEY_FIRST_RUN_REPORTED = "KEY_FIRST_RUN_REPORTED";
    private final String KEY_FOR_LOCATION_SHARING_SETTINGS = "locationSharingSettings:";

    //region keysForSaveSimulatedLocation
    private final String KEY_SAVED_CAMPUS = "savedCampusInSavedSettings";
    private final String KEY_SAVED_LOCATION_MODE = "savedLocationMode";
    private final String KEY_FIRST_VALUE = "firstSavedValueInSavedSettings";
    private final String KEY_SECOND_VALUE = "secondSavedValueInSavedSettings";
    private final String KEY_SAVED_FACILITY = "savedFacilityValueInSavedSettings";
    private final String KEY_SAVED_FLOOR = "savedFloorValueInSavedSettings";
    //endregion


    private String currentUserId;
    private SharedPreferences currentProjectPreferences;
    private SharedPreferences.Editor editor;

    public ProjectSettings(SharedPreferences currentProjectPreferences) {
        this.currentProjectPreferences = currentProjectPreferences;
        this.editor = getEditor();
    }


    public void saveLastCustomLocationForSimulation(@NonNull ILocation location) {

        int locationMode;

        if (location.getLocationType() == LocationMode.INDOOR_MODE)
            locationMode = 0;
        else
            locationMode = 1;

        editor.putInt(KEY_SAVED_LOCATION_MODE, locationMode);
        editor.putString(KEY_SAVED_CAMPUS, location.getCampusId());

        if (locationMode == 0) {
            editor.putString(KEY_SAVED_FACILITY, location.getFacilityId());
            editor.putInt(KEY_SAVED_FLOOR, (int) location.getZ());
            editor.putString(KEY_FIRST_VALUE, String.valueOf(location.getX()));
            editor.putString(KEY_SECOND_VALUE, String.valueOf(location.getY()));
        } else {
            editor.putString(KEY_FIRST_VALUE, String.valueOf(location.getLat()));
            editor.putString(KEY_SECOND_VALUE, String.valueOf(location.getLon()));
        }

        editor.apply();
    }

    @Nullable
    public ILocation getLastCustomLocationFromSimulation() {

        ILocation location = null;

        String savedCampus = currentProjectPreferences.getString(KEY_SAVED_CAMPUS, null);

        if (savedCampus != null) {

            location = new AdvancedLocation();

            int mode = currentProjectPreferences.getInt(KEY_SAVED_LOCATION_MODE, 1);

            location.setCampusId(savedCampus);

            if (mode == 0) {
                location.setType(LocationMode.INDOOR_MODE);
                location.setFacilityId(currentProjectPreferences.getString(KEY_SAVED_FACILITY, null));
                location.setZ(currentProjectPreferences.getInt(KEY_SAVED_FLOOR, 1));
                location.setX(getDoubleValueFromPreferences(currentProjectPreferences.getString(KEY_FIRST_VALUE, null)));
                location.setY(getDoubleValueFromPreferences(currentProjectPreferences.getString(KEY_SECOND_VALUE, null)));
            } else {
                location.setType(LocationMode.OUTDOOR_MODE);
                location.setLat(getDoubleValueFromPreferences(currentProjectPreferences.getString(KEY_FIRST_VALUE, null)));
                location.setLon(getDoubleValueFromPreferences(currentProjectPreferences.getString(KEY_SECOND_VALUE, null)));
            }
        }

        return location;
    }

    public void setCurrentUserId(String currentUserId) {

        this.currentUserId = currentUserId;
    }

    public String getCurrentUserId()  {
        return currentUserId;
    }


    private double getDoubleValueFromPreferences(String savedValue) {

        if (savedValue != null)
            return Double.valueOf(savedValue);
        else
            return 0.0d;
    }

    private SharedPreferences.Editor getEditor() {
        return currentProjectPreferences.edit();
    }


    public boolean wasFirstRunReported(){
        return currentProjectPreferences.getBoolean(KEY_FIRST_RUN_REPORTED, false);
    }

    public void markFirstRunReported(){
        getEditor().putBoolean(KEY_FIRST_RUN_REPORTED, true).commit();
    }
}
