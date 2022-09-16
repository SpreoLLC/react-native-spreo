//
//  MapBoxManager.h
//  react-native-sipl-spreo
//
//  Created by Shriom on 22/11/19.
//

#import "RCTView.h"
#import <React/RCTBridgeModule.h>
#import <IndoorKit/IndoorKit.h>
#import "Global.h"
#import "NavigationManager.h"
#import "RCTViewManager.h"
#import <CoreBluetooth/CoreBluetooth.h>

NS_ASSUME_NONNULL_BEGIN

@class MapNavigation;
 
@interface Spreo : RCTViewManager<RCTBridgeModule,IDDualMapViewControllerDelegate, IDNavigationDelegate, IDInstructionsControllerDelegate,IDLocationListener,CBCentralManagerDelegate>{
    
    IDMapViewId mapViewId;
    IDPoi *indoorPoi;
    
    NSMutableArray *phonesArray;
    NSString* phoneURL;
    NSString* emailURL;
    NSArray *currentKeywords;
    CGFloat descriptionNumberOfRows;
    NSInteger countShowAlertOffCampus;
    NSInteger fromFloorIdSimulation;
    double total_distance;
    double total_timeestimation;

    NSInteger selectedFloor;
    
    BOOL isMapToSelected;
    BOOL locationsIsExpand;
    BOOL isFromParking;
    BOOL descriptionIsExpand;
    BOOL isSelectedSubcategory;
    BOOL gpsBool;
    BOOL isBluetooth;
    IDLocation *tempFromLocation;
    BOOL isLocationOn;
    BOOL isFirstTime;
    BOOL isNavigationStopped;
    
    NSInteger distanceIn;
    BOOL isSimulationOn;
    NSNumber *lastInstrId;
    BOOL isDisableSound;
    NSTimer *timer;
    int totalSeconds;
    BOOL timerLocationCheck;
    BOOL isLocationEnable;
    
    BOOL isOnCampus;
    BOOL isOffCampus;
    
    
}

@property (nonatomic, assign) BOOL myCurrentPosition;
@property (nonatomic, assign) BOOL isNeedShowAlertChooseMap;
@property (nonatomic, assign) BOOL isIndoorLocation;


@property (nonatomic, strong) IDDualMapViewController *mapVC;
@property (nonatomic, strong) IDInstructionsViewController *instructionController;
@property (nonatomic, strong) IDCombinedRoute *instructionList;
@property (nonatomic, weak)   RCTBridge *bridge;
@property (nonatomic, strong) IDPoi *from_poi;
@property (nonatomic, strong) IDPoi *to_poi;
@property (nonatomic, strong) IDPoi *parkingPoi;
@property (nonatomic, weak)   IDPoi *currentPoi;

@property (nonatomic, strong) NSDictionary* cuttentInstruction;
@property (nonatomic, strong) NSDictionary* playCuttentInstruction;

@property (nonatomic, strong) NSArray * pickerViewDataArray;
@property (nonatomic, strong) NSArray * poisCategoriesArray;
@property (nonatomic, strong) NSMutableArray <IDPoi*> *poisSubCategoriesArray;


@property (nonatomic, strong) NSArray * floorsIndexes;
@property (nonatomic, strong) NSMutableArray * navigateFloorsList;
@property (nonatomic, strong) NSMutableArray <IDPoi*>* searchResults;
@property (nonatomic, strong) NSString * searchString;
@property (nonatomic, strong) NSArray * poiSearchListContent;

@property (nonatomic, strong) NSArray * matchedString;


@property (nonatomic, strong) NSString * floorIdentifier;
@property (nonatomic, strong) NSString * myFacilityId;
@property (nonatomic, strong) NSString * selectedFloorId;
@property (nonatomic, strong) NSString * destinationPOI;

@property (nonatomic, strong) NSArray <IDPoi*> *poisListContent;
@property (nonatomic, strong) UIView *vw_main;
@property (nonatomic, strong) NSMutableArray <IDPoi*> *allFilterPoisArray;
@property (nonatomic, strong) NSArray *allCategories;
@property (nonatomic, strong) NSMutableArray *allPoiSearch;
@property (nonatomic, strong) NSDictionary *navAppsNamesWithUrl;
@property (nonatomic, strong) NSArray * nextLocations;
@property (nonatomic, strong) NSMutableArray *allCampusFacility;

@property (nonatomic, strong) NSMutableArray *allCampusFacilityIds;

@property (nonatomic, strong) CBCentralManager *bluetoothManager;

@property (nonatomic, strong) NSArray <IDPoi*> *subCategorypoisListContent;

@end

NS_ASSUME_NONNULL_END

