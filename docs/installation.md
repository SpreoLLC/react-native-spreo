# Installation

Install the library from npm:

```sh
npm install react-native-spreo --save-exact
```

or 

```sh
yarn add react-native-spreo -E
```

The library ships with platform native code that needs to be compiled
together with React Native. This requires you to configure your build
tools.

Since React Native 0.60 and higher, [autolinking](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md) makes the installation process simpler.

---

## Build configuration on iOS

### Using React Native Link (React Native 0.59 and lower)

Run `react-native link react-native-spreo` after which you should be able
to use this library on iOS. Note that by default this will use Apple
Maps and you will miss some of the features provided by Google (see the
instruction on manually enabling Google Maps below).

### Using CocoaPods (React Native 0.59 and lower)

> If the CocoaPods package manager is new to you, please first review
> its [installation guide](https://guides.cocoapods.org/using/getting-started.html)

Setup your `Podfile` (found at `ios/Podfile` as below, replacing all
references to `_YOUR_PROJECT_TARGET_` with your project target (it's the
same as project name by default).

```ruby
# Uncomment the next line to define a global platform for your project
# platform :ios, '9.0'

target '_YOUR_PROJECT_TARGET_' do
  rn_path = '../node_modules/react-native'
  rn_maps_path = '../node_modules/react-native-spreo'

  # See http://facebook.github.io/react-native/docs/integration-with-existing-apps.html#configuring-cocoapods-dependencies
  pod 'yoga', path: "#{rn_path}/ReactCommon/yoga/yoga.podspec"
  pod 'React', path: rn_path, subspecs: [
    'Core',
    'CxxBridge',
    'DevSupport',
    'RCTActionSheet',
    'RCTAnimation',
    'RCTGeolocation',
    'RCTImage',
    'RCTLinkingIOS',
    'RCTNetwork',
    'RCTSettings',
    'RCTText',
    'RCTVibration',
    'RCTWebSocket',
  ]

  # React Native third party dependencies podspecs
  pod 'DoubleConversion', :podspec => "#{rn_path}/third-party-podspecs/DoubleConversion.podspec"
  pod 'glog', :podspec => "#{rn_path}/third-party-podspecs/glog.podspec"
  # If you are using React Native <0.54, you will get the following error:
  # "The name of the given podspec `GLog` doesn't match the expected one `glog`"
  # Use the following line instead:
  #pod 'GLog', :podspec => "#{rn_path}/third-party-podspecs/GLog.podspec"
  pod 'Folly', :podspec => "#{rn_path}/third-party-podspecs/Folly.podspec"

  # react-native-spreo dependencies
  pod 'react-native-spreo', path: rn_maps_path
  pod 'SpreoPod'
end

```
>**note:-** React Native all versions (**recommended**)  add SpreoPod by add this line  **pod 'SpreoPod'** for uses Spreo SDK methods


Then run in the `ios` folder

```sh
pod install 
```

and open the produced workspace file (`.xcworkspace`) in XCode to build your project.

### Using CocoaPods (React Native 0.60 and higher)

```sh
cd ios
pod install
```


#### React Native all versions (**recommended**) after installing Framework using Pods Please add framework in spero module(react-native-spreo) from xcode side menu editor. Please check below image for reference:

![](https://i.ibb.co/WFj456x/IOS-Integration.png)


React Native all versions (**recommended**)  add 3 permission in info.plist
 ```groovy
    <key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
    <string>The app requires the device location in order tonavigate</string>

    <key>Privacy - Bluetooth Always Usage Description</key>
    <string>The app requires the device bluetooth in order get the region</string>
    
    <key>NSLocationWhenInUseUsageDescription</key>
    <string></string>
```

That's it, you made it! 👍
    
---


## Build configuration on Android

Ensure your build files match the following requirements:

1. (React Native 0.59 and lower) Define the `react-native-spreo` project in `android/settings.gradle`:

```groovy
...
include ':react-native-spreo'
project(':react-native-spreo').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-spreo/lib/android')
```

2. (React Native 0.59 and lower) Add the `react-native-spreo` as an dependency of your app in `android/app/build.gradle`:

```groovy
...
dependencies {
  ...
  implementation project(':react-native-spreo')
}
```

3.1 React Native all versions (**recommended**) in your root `build.gradle`.

    3.1.1 Chnage the minSdkVersion to 18 due to SDK minSdkVersion compatibility :

    ```groovy

    buildscript {
        ext {
            buildToolsVersion = "xxx"
            minSdkVersion = 18
            compileSdkVersion = xxx
            targetSdkVersion = xxx
        }
    }
    ...
    ```
    3.1.1 Add flatDir to allprojects/repositories :
    
    ```groovy

    allprojects {
        repositories {
            ...
            ...
            ...
            ...
            ...
            flatDir {
                dirs 'libs'
                dirs project(':react-native-spreo').file('libs')
            }
        }
    }
    ...
    ```

3.2 (React Native all versions) If you do **not** enabled multidex then add the `com.android.support:multidex:1.0.3` as an dependency of your app in `android/app/build.gradle` and set **multiDexEnabled true** in defaultConfig:

```groovy
...
dependencies {
   ...
  implementation 'com.android.support:multidex:1.0.3'
}
```

4. (React Native all versions) add following :

   Add your API key to your manifest file (`android/app/src/main/AndroidManifest.xml`):

    ```xml
    <application>
    <!-- You will only need to add this meta-data tag, but make sure it's a child of application -->
    <meta-data
            android:name="com.google.android.maps.v2.API_KEY"
            android:value="Your Google maps API Key Here" />
    </application>
    ```
    > Note: As shown above, `com.google.android.maps.v2.API_KEY`. This legacy name allows
    > authentication to the Android Maps API v2 only. An application can
    > specify the API key metadata names. 

    Source: https://developers.google.com/maps/documentation/android-api/signup

    Add xmlns:tools to your manifest file 

    ```xml
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        package="com.exampleapp">
    </manifest>
    ```
    Add  **uses-library, tools:replace, tools:ignore** to your manifest file 
    ```xml
    <application
        ...
        ...
        ...
        tools:replace="android:allowBackup"
        tools:ignore="GoogleAppIndexingWarning">
        <uses-library android:name="org.apache.http.legacy" android:required="false"/>
        <meta-data
            ...
            ...
        >
        </meta-data>
    </application>
    ```

5. (React Native 0.59 and lower) Add `import com.reactlibrary.SpreoPackage;` and `new SpreoPackage()` in your `MainApplication.java` :

```java
import com.reactlibrary.SpreoPackage;
...
    @Override
        protected List<ReactPackage> getPackages() {
          @SuppressWarnings("UnnecessaryLocalVariable")
          List<ReactPackage> packages = new PackageList(this).getPackages();
          // Packages that cannot be autolinked yet can be added manually here, for example:
           packages.add(new SpreoPackage());
          return packages;
        }
```

That's it, you made it! :+1:

---

## Troubleshooting

## No map whatsoever

Ensure the map component and its container have viewport dimensions. An
example is below:

```jsx
import MapView from 'react-native-spreo'; 
import {Dimensions} from 'react-native';
// get the height and width from Dimensions
const {height,width}= Dimensions.get('window')
...
const styles = StyleSheet.create({
    container: {
        flex:1
    },
    map: {
        height:height,
        width:width,
        flex:1
    },
});

export default () => (
   <View style={styles.container}>
        <MapView
        style={styles.map}
        >
        </MapView>
   </View>
);
```

### Clearing caches

Run these commands to clean caches

```sh
# NPM
watchman watch-del-all
npm cache clean

# Android, if you encounter `com.android.dex.DexException: Multiple dex files define Landroid/support/v7/appcompat/R$anim`, then clear build folder.
cd android
./gradlew clean
cd ..
```

### Trouble with React Native Spreo

- please comment if you face any problem
