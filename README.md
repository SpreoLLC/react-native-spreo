# react-native-spreo

React Native spreo components for iOS + Android

## Installation

See [Installation Instructions](docs/installation.md).

## Compatibility

Compatibile with all react native versions

### Note about React requires

Since react-native 0.25.0, `React` should be required from `node_modules`.
React Native versions from 0.18 should be working out of the box, for lower
versions you should add `react` as a dependency in your `package.json`.

## Component API

[`<MapView />` Component API](docs/mapview.md)

[`Spreo` Component API](docs/spreo.md)

## General Usage

```js
import MapView from 'react-native-spreo';
```
or

```js
var MapView = require('react-native-spreo');
```

This MapView component is built so that the map can shown in app.

### Rendering a Map 

## MapView
```jsx
<MapView
	style={styles.map}
>
</MapView>
```

```js
import {Spreo,DownloadManager} from 'react-native-spreo';
```
>note:- before load map we need to call download function of Spreo SDK
>for download function in IOS we need to add **(DownloadManager)** else in android we can use **(Spreo)** 

This Spreo component is built so that the Spreo Sdk method can be used in app.

### Calling Download function in Android 

#### download.js file in your app before map load in IOS
```jsx
// set the SPREO API KEY 
let  SPREO_API_KEY = "xxxxxxxxx"   // like sheba medical center
Spreo.downloadData(Response=>{
	DownloadManager.setupAPIKey(SPREO_API_KEY,(value,error)=>{
        if(value == "Download Completed"){
         // navigate to map 
        }
    });
});
```


#### download.js file in your app before map load in Android
```jsx
Spreo.downloadData(Response=>{
	if(Response == "done"){
		// navigate to map 
	}
});
```


For Android: add the following line in your AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.INTERNET" />
```
For IOS: configure [App Transport Security](https://developer.apple.com/library/content/documentation/General/Reference/InfoPlistKeyReference/Articles/CocoaKeys.html#//apple_ref/doc/uid/TP40009251-SW33) in your app



#### Inputs don't focus

* When inputs don't focus or elements don't respond to tap, look at the order of the view hierarchy, sometimes the issue could be due to ordering of rendered components, prefer putting MapView as the first component.

Bad:

```jsx
<View>
  <TextInput/>
  <MapView/>
</View>
```

Good:

```jsx
<View>
  <MapView/>
  <TextInput/>
</View>
```


License
--------

     Copyright (c) 2019 Spreo

     Licensed under the The MIT License (MIT) (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
       
       ## need to add License

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
