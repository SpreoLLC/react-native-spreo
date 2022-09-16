package com.reactlibrary;

public class SpreoAppInfoObject {
    final String appName;
    final String appIcon;

    public SpreoAppInfoObject(String appName, String appIcon) {
        this.appName = appName;
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppIcon() {
        return appIcon;
    }
}
