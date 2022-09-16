## API

Note that Some APIs are platform-specific. If there is no implementation for a platform, then in return  you will receive **Error**. 


| Method                                                            | Parameter to pass in func. |  iOS | Android | 
| ----------------------------------------------------------------- | -------------------------  | :--: | :-----: | 
| [setupAPIKey()](#setupAPIKey)                                     | `Key<string>`, `callback`  |  ✅  |   ❌    |   
| [downloadData()](#downloadData)                                   | `Key<string>`, `callback`  |  ❌  |   ✅    |   
| [getPoiCategories()](#getPoiCategories)                           | `callback`                 |  ✅  |   ✅    |   
| [showAllPois()](#showAllPois)                                     |                            |  ✅  |   ✅    |   
| [getAllFacilityFloors()](#getAllFacilityFloors)                   | `callback`                 |  ✅  |   ✅    |   
| [showMyLocation()](#showMyLocation)                               |                            |  ✅  |   ✅    |   
| [getAllPoiCategories()](#getAllPoiCategories)                     | `callback`                 |  ✅  |   ✅    | 
| [setPoiCategoriesData()](#setPoiCategoriesData)                   | `checkcategories<array>`   |  ✅  |   ✅    |   
| [updateFloorPicker()](#updateFloorPicker)                         | `callback`                 |  ❌  |   ✅    |   
| [setPoiContent()](#setPoiContent)                                 | `poi`, `callback`          |  ✅  |   ✅    |   
| [showPoi()](#getbundleid)                                         | `poi`                      |  ✅  |   ✅    |   
| [updateMapwithFloorId()](#updateMapwithFloorId)                   | `floorId`                  |  ✅  |   ✅    | 
| [setMarkerOnFromLocation()](#setMarkerOnFromLocation)             | `poi`                      |  ✅  |   ❌    |   
| [setMarkerOntoLocation()](#setMarkerOntoLocation)                 | `poi`                      |  ✅  |   ✅    |   
| [startNavigation()](#startNavigation)                             |`frompoi`,`topoi`,`callback`|  ✅  |   ✅    |   
| [navigateThirdParty()](#navigateThirdParty)                       | `appname`                  |  ✅  |   ✅    |   
| [navigationCanceled()](#navigationCanceled)                       |                            |  ✅  |   ✅    |   
| [hasParkingLocation()](#hasParkingLocation)                       | `callback`                 |  ✅  |   ✅    |   
| [markMyparking()](#markMyparking)                                 | `callback`                 |  ✅  |   ✅    |   
| [removeParkingLocation()](#removeParkingLocation)                 | `callback`                 |  ✅  |   ✅    |   
| [startNavigationToParking()](#startNavigationToParking)           |                            |  ✅  |   ✅    |   
| [poiSubCategories()](#poiSubCategories)                           | `categorie`, `callback`    |  ✅  |   ✅    |   
| [resetFromLocation()](#resetFromLocation)                         |                            |  ❌  |   ✅    |   
| [openNavView()](#openNavView)                                     | `callback`                 |  ✅  |   ✅    |   
| [updateInstructionView()](#updateInstructionView)                 | `callback`                 |  ✅  |   ❌    |

---

### Responce description of Function (for Android and  iOS)
> In Android array return in JSON String so you have to parse by **JSON.parse()** function
---
> In iOS array return in array form no need to do parsing.
----

### setupAPIKey()

set the Campus Key and download corresponding campus data in IOS .

#### Examples

```js
import {Spreo,DownloadManager} from 'react-native-spreo'; // import for using setupAPIKey function 

DownloadManager.setupAPIKey("xxxxx",(result)=>{
    if(result == "Download Completed"){
        //navigate to map view 
    }
});
```

---

### downloadData()

set the Campus Key and download corresponding campus data in Android .

#### Examples

```js
Spreo.downloadData("xxxxx",(result)=>{
    if(result == "done"){
        //navigate to map view 
    }
});
```

---

### getPoiCategories()

Gets Poi Categories in array form.

#### Examples

>note:- in result you get two bolean variable(showInCatgories, showInMapFilter) by which you have to show thease categories in MapFilter  and categories list.

```js
Spreo.getPoiCategories(result =>{
    let ary_data;
    if(Platform.OS == 'android'){
        ary_data = JSON.parse(result);
    }else{
        ary_data = result;
    }
    console.log(ary_data)
});

```

---

### showAllPois()

This function used to show all poi in map

#### Examples

```js
Spreo.showAllPois()
```

---

### getAllFacilityFloors()

This function used for getting floor list

#### Examples

```js
Spreo.getAllFacilityFloors(result =>{
    let ary_data;
    if(Platform.OS == 'android'){
        ary_data = JSON.parse(result);
    }else{
        ary_data = result;
    }
    console.log(ary_data)
});
```

---

### showMyLocation()

This function used for show user Location

#### Examples

```js
Spreo.showMyLocation();
```

---

### getAllPoiCategories()

This function used get all poi categories

#### Examples

```js
Spreo.getAllPoiCategories(result =>{
    let ary_data;
    if(Platform.OS == 'android'){
        ary_data = JSON.parse(result);
    }else{
        ary_data = result;
    }
    console.log(ary_data)
});
```

---

### setPoiCategoriesData()

This function used set Poi categories data which we select in filter shown by getPoiCategories()

#### Examples

```js
Spreo.setPoiCategoriesData(["categorie1","categorie1"]);
```

---

### updateFloorPicker()

This function used get floor list when the navigation path is drawn(in Android only, for iOS we used emitter)

#### Examples

```js
Spreo.updateFloorPicker((result) => {
    console.log("updateFloor",result)
});
```

---

### setPoiContent()

This function used get poi Detail when we tap on poi then emitter is called and we get poi and then call this function with the emiiter result poi

#### Examples

```js
Spreo.setPoiContent(poi,(result) => {
        console.log("poiDetail",result)
    });
});
```

---

### showPoi()

This function used show the Poi in center of map by passing the poi (used in poi detail pop up)

#### Examples

```js
Spreo.showPoi(poi);
```

---

### updateMapwithFloorId()

This function used update the map by clicking in floor list to show the selected floor map

#### Examples

```js
Spreo.updateMapwithFloorId(floorId);
```

---

### setMarkerOnFromLocation()

This function used set from location poi in sdk(iOS only,for android we manged with startnavigation )

#### Examples

```js
Spreo.setMarkerOnFromLocation(poi);
```

---

### setMarkerOntoLocation()

This function used set to location poi in map with red icon

#### Examples

```js
Spreo.setMarkerOntoLocation(poi);
```

---

### startNavigation()

This function used to start navigation and it's draw a path

#### Examples

```js
Spreo.startNavigation(from_poi,to_poi,(result)=>{
    if(result == "gpsNavigation"){
        //you have to show modal popup  with three option given in sample app
    }else if(result.length > 0 ){
        //you have to show modal popup  with the app list (result <array>) and select and send name to navigateThirdParty(name) 
    }else{
        //navigation start manage your state accordingly 
    }
});
```

---

### navigateThirdParty()

This function used open third party app by selecting the app name send in start naigation

#### Examples

```js
Spreo.navigateThirdParty(app_name);
```

---

### navigationCanceled()

This function used cancel the navigation and remove the draw path by start navigation

#### Examples

```js
Spreo.navigationCanceled();
```

---

### hasParkingLocation()

This function used to check if you already parked or not

#### Examples

```js
Spreo.hasParkingLocation(result =>{
    //result is "true" if you already set yor parking else result is "false"
});
```

---

### markMyparking()

This function used to mark your current location as user parking location

#### Examples

```js
Spreo.markMyparking(result =>{
    if(result == "done"){
        // manage your view
    }
});
```

---

### removeParkingLocation()

This function used to remove user parking location

#### Examples

```js
Spreo.removeParkingLocation(result =>{
    if(result == "done"){
        // manage your view
    }
});
```

---

### startNavigationToParking()

This function used to navigate user from his current location to user parking location

#### Examples

```js
Spreo.startNavigationToParking();
```

---

### poiSubCategories()

This function used to navigate user from his current location to user parking location

#### Examples

```js
Spreo.poiSubCategories(categorie_poi,(result) =>{
    let ary_data;
    if(Platform.OS == 'android'){
        ary_data = JSON.parse(result);
    }else{
        ary_data = result;
    }
    console.log(ary_data)
})
```

---

### resetFromLocation()

This function used to reset navigation when we use ENTER MY STARTING POINT used in sample app(iOS only)

#### Examples

```js
Spreo.resetFromLocation();
```

---

### openNavView()

This function used to get instruction and open instruction view accordingly to instruction condition

#### Examples

```js
Spreo.openNavView(result =>{
    // in result we get instruction in a form of array(ios) and object(android)
});
```
>note:- please refer sample for calling this function in emmiter in (STARTED state)

---

### updateInstructionView()

This function used to get instruction and open instruction view accordingly to instruction condition

#### Examples

```js
Spreo.updateInstructionView(result =>{
    // in result we get instruction in a form of array(ios) and object(android)
});
```
>note:- please refer sample for calling this function in emmiter in (NAVIGATE state) and used in iOS only

---
