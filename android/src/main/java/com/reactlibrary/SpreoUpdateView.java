package com.reactlibrary;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.react.bridge.Callback;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ThemedReactContext;
import com.mlins.utils.PropertyHolder;
import com.spreo.enums.ResUpdateStatus;
import com.spreo.interfaces.ConfigsUpdaterListener;
import com.spreo.sdk.data.SpreoResourceConfigsUtils;
import com.spreo.sdk.location.SpreoLocationProvider;
import com.spreo.sdk.setting.SettingsProvider;



public class SpreoUpdateView extends LinearLayout implements ConfigsUpdaterListener {

    Callback callback;
    private final Context ctx;

    ThemedReactContext reactContext;
    private RelativeLayout pl;
    private SpreoUpdateListener updateListener = null;

    public SpreoUpdateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        ctx = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.spreo_update_view, this, true);

        pl = (RelativeLayout) findViewById(R.id.loading_indicator);

    }

    public void update(String key) {
        if (key != null) {
            SpreoResourceConfigsUtils.setSpreoApiKey(key);
            SpreoResourceConfigsUtils.subscribeToResourceUpdateService(this);
            SpreoResourceConfigsUtils.update(ctx);
        }
    }

    public void update(String key, String language, String servername) {
        if (key != null) {
            SpreoResourceConfigsUtils.setSpreoApiKey(key);
            SpreoResourceConfigsUtils.subscribeToResourceUpdateService(this);
            PropertyHolder.getInstance().setServerName(servername);
            PropertyHolder.getInstance().setAppLanguage(language);
            SettingsProvider.getInstance().setUseZipWithoutMaps(true);
            SpreoResourceConfigsUtils.update(ctx);
        }
    }

    @Override
    public void onPreConfigsDownload() {
        if (pl != null) {
            pl.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPostConfigsDownload(ResUpdateStatus resUpdateStatus) {
        if (resUpdateStatus.equals(ResUpdateStatus.OK)) {
            SpreoResourceConfigsUtils.unSubscribeFromResourceUpdateService(this);

            SpreoLocationProvider.getInstance().startLocationService(ctx);

            if (pl != null) {
                pl.setVisibility(View.GONE);
            }
            notifyUpdateFinished();
        } else {
            if (pl != null) {
                notifyUpdateFinished();
//                reactContext
//                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                        .emit("downloadFail",false );
//                pl.setVisibility(View.GONE);
            }
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
}
