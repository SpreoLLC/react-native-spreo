package com.reactlibrary;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.mlins.utils.PropertyHolder;
import com.spreo.interfaces.SpreoDualMapViewListener;
import com.spreo.sdk.data.SpreoDataProvider;
import com.spreo.sdk.location.SpreoLocationProvider;

import java.util.jar.Attributes;


public class UpdateActivity implements SpreoUpdateListener {

    private SpreoUpdateView updateView;
    Callback callback;
    ReactApplicationContext reactApplicationContext;
    public void onInit(ReactApplicationContext reactApplicationContext, Callback callback) {
        this.reactApplicationContext=reactApplicationContext;
        this.callback=callback;
        String language =  SpreoUsersInfoHolder.getInstance().getSelectedLanguage();
        AttributeSet attributes=null;
        updateView = new SpreoUpdateView(reactApplicationContext,attributes);
        updateView.setUpdateListener(this);
        String key = SpreoUsersInfoHolder.getInstance().getSelectedKey();

        if (key != null) {
            String servername = PropertyHolder.getInstance().getServerName();
            updateView.update(key, language, servername);
        }
    }
    public  ReactApplicationContext getCont (){
     return reactApplicationContext   ;
}
    @Override
    public void OnUpdateFinished() {
        Log.d("shriom", "OnUpdateFinished() called");
        ApplicationSettings.getInstance().init(reactApplicationContext);

        String randomID = SpreoAnalyticsUtility.generateRandomID();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(reactApplicationContext);
        boolean firstrun = preferences.getBoolean("SpreoFirstRun", true);
        if (firstrun) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("SpreoFirstRun",false);
            editor.apply();

           /* final Dialog dialog = new Dialog(reactApplicationContext);
            dialog.setContentView(R.layout.spreo_dialog_instructions);
            dialog.setCancelable(false);*/

//            TextView dialogOk = (TextView) dialog.findViewById(R.id.ok_btn);

//            dialogOk.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
                    openMap();
//                    dialog.dismiss();
//                }
//            });
//
//            dialog.show();

            // TODO Rohit
            SpreoAnalyticsUtility.sendReport(SpreoAnalyticsUtility.FIRST_RUN_REPORT, randomID, null);
        } else {
            openMap();
        }

        if (!SpreoAnalyticsUtility.newSessionSent) {
            SpreoAnalyticsUtility.sendReport(SpreoAnalyticsUtility.NEW_SESSION_REPORT, randomID, null);
            SpreoAnalyticsUtility.newSessionSent = true;
        }
    }

    private void openMap() {
        try {
            SpreoLocationProvider.getInstance().startLocationService(reactApplicationContext);
            if(SpreoDataProvider.getFloorPickerFacilityId() == null){
                callback.invoke("fail");
                Log.d("shriom", "openMap() called  fail");
            }else{
                callback.invoke("done");
                Log.d("shriom", "openMap() called  done");
            }

        }catch (Throwable t){
            callback.invoke("fail");
            Log.d("shriom", "openMap() called  fail");

        }



    }
}
