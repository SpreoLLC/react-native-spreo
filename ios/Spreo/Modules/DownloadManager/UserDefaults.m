//
//  UserDefaults.m
//  Spreo
//
//  Created by Hasan Sa on 7/7/14.
//  Copyright (c) 2014 Spreo LLC. All rights reserved.
//

#import "UserDefaults.h"

// USER KEYS
static NSString * const kUserDefaultsAnalyticsModeKey                   = @"kUserDefaultsAnalyticsModeKey";
static NSString * const kUserDefaultsUserLocationsModeKey               = @"kUserDefaultsUserLocationsModeKey";
static NSString * const kUserDefaultsProximityTriggerModeKey            = @"kUserDefaultsTriggerModeKey";
static NSString * const kUserDefaultsGeofenceDebugModeKey            = @"kUserDefaultsGeofenceDebugModeKey";
static NSString * const kUserDefaultsLocationSharingModeKey             = @"kUserDefaultsLocationSharingModeKey";
static NSString * const kUserDefaultssetSimplifiedInstructionStatus     = @"kUserDefaultsFilteredInstructionsModeKey";
static NSString * const kUserDefaultsApiKeysModeKey                     = @"kUserDefaultsApiKeysModeKey";
static NSString * const kUserDefaultsAppApiKey                          = @"kUserDefaultsAppApiKey";
static NSString * const kUserDefaultsUserIDKey                          = @"kUserDefaultsUserIDKey";
static NSString * const kUserDefaultsProjectIDKey                       = @"kUserDefaultsProjectIDKey";
static NSString * const kUserDefaultsDeveloperUserKey                   = @"kUserDefaultsDeveloperUserKey";
static NSString * const kUserDefaultsOutDoorMapTypeKey                  = @"kUserDefaultsOutDoorMapTypeKey";
static NSString * const kUserDefaultsInDoorMapRotationKey               = @"kUserDefaultsInDoorMapRotationKey";
static NSString * const kUserDefaultsMapZoomLevel               = @"kUserDefaultsMapZoomLevel";

// INDOOR MAP KEYS
static NSString * const kUserDefaultsUserLayerKey                       = @"kUserDefaultsShowUserLayerKey";
static NSString * const kUserDefaultsCampusLayerKey                     = @"kUserDefaultsCampusLayerKey";
static NSString * const kUserDefaultsFacilityLayerKey                   = @"kUserDefaultsFacilityLayerKey";
static NSString * const kUserDefaultsPoisLayerKey                       = @"kUserDefaultsPoisLayerKey";
static NSString * const kUserDefaultsPathsLayerKey                      = @"kUserDefaultsGisLayerKey";
static NSString * const kUserDefaultsRouteLayerKey                      = @"kUserDefaultsRouteLayerKey";
static NSString * const kUserDefaultsLabelsLayerKey                     = @"kUserDefaultsLabelsLayerKey";
//
static NSString * const kUserDefaultsTripNavigationPoiKey               = @"kUserDefaultsTripNavigationPoiKey";
//
static NSString * const kUserDefaultsVisibleCategoriesKey               = @"kUserDefaultsVisibleCategoriesKey";
//
static NSString * const kUserDefaultsSimulationModeKey                  = @"kkUserDefaultsSimulationModeKey";
//
static NSString * const kUserDefaultsSimulatedModeKey                   = @"kUserDefaultsSimulatedModeKey";
static NSString * const kUserDefaultsSimulatedLocKey                    = @"kUserDefaultsSimulatedLocKey";
//
static NSString * const kUserDefaultsMuteSoundModeKey                   = @"kUserDefaultsSoundModeKey";



// GLOABL KEYS
static NSString * const kUserDefaultsPoiFavorites                       = @"kUserDefaultsPoiFavorites";
static NSString * const kUserDefaultsRecentSearch                       = @"kUserDefaultsRecentSearch";
static NSString * const kSaveLanguageDefaultKey                         = @"kSaveLanguageDefaultKey";

static NSString *const kVisibleSearchHelpScreenKey                      = @"kVisibleSearchHelpScreenKey";

static NSString *const kUsedThirdPartyNavigation                        = @"kUsedThirdPartyNavigation";


static NSString *const kUseSimulationDefaultLocation           = @"kUseSimulationDefaultLocation";

static NSString *const kRememberMe           = @"kRememberMe";
static NSString *const kProjectSelect        = @"kProjectSelect";

static NSString *const kDisplayTopFloorContentKey = @"kDisplayTopFloorContent";

#ifdef HEALTHCARE
static NSString * const kStaffModeKey                                   = @"kStaffModeKey";
static NSString * const kOneTimeStaffModeKey                            = @"kOneTimeStaffModeKey";
#endif

#ifdef NIKE
static NSString * const kStaffModeKey                            = @"kStaffModeKey";
static NSString * const kOneTimeStaffModeKey                     = @"kOneTimeStaffModeKey";
#endif

#pragma mark - Location Sharing Keys
static NSString * const kProfileLoginKey                                = @"kProfileLoginKey";
static NSString * const kProfilePasswordKey                             = @"kProfilePasswordKey";
static NSString * const kProfileIdKey                                   = @"kProfileIdKey";
static NSString * const kProfileIconUrlKey                              = @"kProfileIconUrlKey";
static NSString * const kProfileIsSharingLocationKey                    = @"kProfileIsSharingLocationKey";

static NSString * const kAppSettingsKey                                 = @"kAppSettingsKey";


@interface UserDefaults()

@property (nonatomic, strong) NSArray* myTripPoisArray;
@property (nonatomic, strong) NSArray* myTripArrivedPoisArray;

@end

@implementation UserDefaults

static UserDefaults * _sharedUserDefaults = nil;

+ (UserDefaults *)sharedDefaults
{
    @synchronized(self) {
        if (nil == _sharedUserDefaults) {
            
            _sharedUserDefaults = [[self alloc] init];
        }
    }
    return _sharedUserDefaults;
}

+ (void)initialize
{
    if ([self class] == [UserDefaults class]) {

        NSDictionary * defaultsDict = @{kUserDefaultsUserLayerKey                       : @YES,
                                        kUserDefaultsApiKeysModeKey                     : @YES,
                                        kUserDefaultsCampusLayerKey                     : @YES,
                                        kUserDefaultsFacilityLayerKey                   : @YES,
                                        kUserDefaultsPoisLayerKey                       : @YES,
                                        kUserDefaultsPathsLayerKey                      : @NO,
                                        kUserDefaultsRouteLayerKey                      : @YES,
                                        kUserDefaultsLabelsLayerKey                     : @YES,
                                        kUserDefaultsSimulationModeKey                  : @NO,
                                        kUserDefaultsInDoorMapRotationKey               : @(1),
                                        kUserDefaultsAnalyticsModeKey                   : @YES,
                                        kUserDefaultsUserLocationsModeKey               : @NO,
                                        kUserDefaultsLocationSharingModeKey             : @NO,
                                        kUserDefaultsMuteSoundModeKey                   : @NO,
                                        kUserDefaultssetSimplifiedInstructionStatus     : @YES,
                                        kUserDefaultsProximityTriggerModeKey            : @NO,
                                        kUserDefaultsGeofenceDebugModeKey               : @NO,
                                        kProfileIsSharingLocationKey                    : @NO,
                                        kDisplayTopFloorContentKey                      : @YES,
#ifdef HEALTHCARE

                                        kStaffModeKey : @NO,
                                        kOneTimeStaffModeKey : @NO};
#elif NIKE
                                        kStaffModeKey : @NO,
                                        kOneTimeStaffModeKey : @NO};
                                       
#else
    };
#endif

        [[NSUserDefaults standardUserDefaults] registerDefaults:defaultsDict];
    }
}

- (id)init {
	if (self = [super init]) {
    }
    return self;
}

#pragma mark -
#pragma mark - Getters
- (NSString*)kAppApiKey
{
    return [[NSUserDefaults standardUserDefaults] objectForKey:kUserDefaultsAppApiKey];
}

- (BOOL)multiApikeysMode
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsApiKeysModeKey];
}

- (BOOL)locationSharingMode
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsLocationSharingModeKey];
}

- (BOOL)analyticsMode
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsAnalyticsModeKey];
}

- (BOOL)userlocationsMode
{
   return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsUserLocationsModeKey];
}

- (BOOL)getSimplifiedInstructionStatus
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultssetSimplifiedInstructionStatus];
}

- (NSString*)userID
{
    return [[NSUserDefaults standardUserDefaults] stringForKey:kUserDefaultsUserIDKey];
}

- (NSString*)projectID
{
    return [[NSUserDefaults standardUserDefaults] stringForKey:kUserDefaultsProjectIDKey];
}

- (BOOL)developerUser
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsDeveloperUserKey];
}

- (IDMapType)mapType
{
    return (IDMapType)[[[NSUserDefaults standardUserDefaults] objectForKey:kUserDefaultsOutDoorMapTypeKey] integerValue];
}

- (NSInteger)mapRotation
{
    return [[NSUserDefaults standardUserDefaults] integerForKey:kUserDefaultsInDoorMapRotationKey];
}

- (double)mapZoomLevel {
    return [[NSUserDefaults standardUserDefaults] doubleForKey:kUserDefaultsMapZoomLevel];
}

- (BOOL)userLayer
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsUserLayerKey];
}

- (BOOL)campusLayer
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsCampusLayerKey];
}

- (BOOL)facilityLayer
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsFacilityLayerKey];
}

- (BOOL)poisLayer
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsPoisLayerKey];
}

- (BOOL)pathsLayer
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsPathsLayerKey];
}

- (BOOL)routeLayer
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsRouteLayerKey];
}

- (BOOL)labelsLayer
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsLabelsLayerKey];
}

- (NSArray *)visibleCategories
{
    return [[NSUserDefaults standardUserDefaults] objectForKey:kUserDefaultsVisibleCategoriesKey];
}

- (BOOL)simulationMode
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsSimulationModeKey];
}

- (BOOL)simulatedLocationMode
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsSimulatedModeKey];
}

- (BOOL)muteMode
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsMuteSoundModeKey];
}

- (NSString *)language
{
    return [[NSUserDefaults standardUserDefaults] objectForKey:kSaveLanguageDefaultKey];
}

- (NSDictionary *)userPersonalPois
{
    return [[NSUserDefaults standardUserDefaults] objectForKey:kUserDefaultsPoiFavorites];
}

- (NSDictionary *)userPersonalRecentSearch
{
    return [[NSUserDefaults standardUserDefaults] objectForKey:kUserDefaultsRecentSearch];
}

- (NSArray*)myTripPois
{
    return _myTripPoisArray;
}

- (NSArray*)myTripArrivedPois
{
    return _myTripArrivedPoisArray;
}

- (IDPoi*)myTripNavigationPio
{
    NSString* poiID = [[NSUserDefaults standardUserDefaults] objectForKey:kUserDefaultsTripNavigationPoiKey];
    if (poiID != nil) {
         NSArray* filteredArr = [_myTripPoisArray filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"SELF.identifier = %@", poiID]];
        return [filteredArr firstObject];
    }
    
    return nil;
}

- (BOOL)proximityTrigger
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsProximityTriggerModeKey];
}

- (BOOL)geofenceDebugMode
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUserDefaultsGeofenceDebugModeKey];
}

-(BOOL)visibleSearchHelpScreen{
    return [[NSUserDefaults standardUserDefaults] integerForKey:kVisibleSearchHelpScreenKey];
}

-(BOOL)usedThirdPartyNavigation {
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUsedThirdPartyNavigation];
}

-(IDUserLocation *)simulatedLocation {
    NSData *encodedObject = [[NSUserDefaults standardUserDefaults] objectForKey:API_KEY];
    IDUserLocation *simulatedLocation = [NSKeyedUnarchiver unarchiveObjectWithData:encodedObject];
    return simulatedLocation;
}

-(BOOL)useSimulationDefaultLocation {
    return [[NSUserDefaults standardUserDefaults] boolForKey:kUseSimulationDefaultLocation];
}

-(BOOL)rememberMeMode {
    return [[NSUserDefaults standardUserDefaults] boolForKey:kRememberMe];
}

-(BOOL)projectSelect {
    return [[NSUserDefaults standardUserDefaults] boolForKey:kProjectSelect];
}

- (BOOL)displayTopFloorContent {
    return [[NSUserDefaults standardUserDefaults] boolForKey:kDisplayTopFloorContentKey];
}

#ifdef HEALTHCARE
- (BOOL)staffMode {
    return [[NSUserDefaults standardUserDefaults] boolForKey:kStaffModeKey];
}

- (BOOL)oneTimeStaffMode {
    return [[NSUserDefaults standardUserDefaults] boolForKey:kOneTimeStaffModeKey];
}
#endif

#ifdef NIKE
- (BOOL)staffMode {
    return [[NSUserDefaults standardUserDefaults] boolForKey:kStaffModeKey];
}

- (BOOL)oneTimeStaffMode {
    return [[NSUserDefaults standardUserDefaults] boolForKey:kOneTimeStaffModeKey];
}
#endif

#pragma mark -
#pragma mark - Setters

- (void)setAppApiKey:(NSString*)anApiKey
{
    [[NSUserDefaults standardUserDefaults] setObject:anApiKey forKey:kUserDefaultsAppApiKey];
}

- (void)setMultiApikeysMode:(BOOL)aMode
{
    [[NSUserDefaults standardUserDefaults] setBool:aMode forKey:kUserDefaultsApiKeysModeKey];
}

- (void)updateMyTripPoisWithdArray:(NSArray*)sortedArr
{
    NSMutableArray* newArr = [NSMutableArray arrayWithCapacity:sortedArr.count];
    
    for (IDPoi* poi in sortedArr) {
            if (poi.identifier != nil) {
                
                [newArr addObject:poi];
            }
    }
    
    _myTripPoisArray = [NSArray arrayWithArray:newArr];
}

- (void)updateMyTripPoiWithPoi:(IDPoi*)aPoi add:(BOOL)add
{
    NSMutableArray* newArr = [NSMutableArray arrayWithArray:_myTripPoisArray];

    if (aPoi.identifier != nil) {
        
        if (add) {
            if (![self myTripListContainsPoi:aPoi withArray:_myTripPoisArray]) {
                [newArr addObject:aPoi];
            }
        }
        else {
            if ([self myTripListContainsPoi:aPoi withArray:_myTripPoisArray]) {
                [newArr removeObject:aPoi];
            }
        }
    }
    _myTripPoisArray = [NSArray arrayWithArray:newArr];
}

- (void)updateMyTripArrivedPoisWithPoi:(IDPoi*)aPoi add:(BOOL)add
{
    NSMutableArray* newArr = [NSMutableArray arrayWithArray:_myTripArrivedPoisArray];
    
    if (aPoi.identifier != nil) {
        
        if (add) {
            if (![self myTripListContainsPoi:aPoi withArray:_myTripArrivedPoisArray]) {
                [newArr addObject:aPoi];
            }
        }
        else {
            if ([self myTripListContainsPoi:aPoi withArray:_myTripArrivedPoisArray]) {
                [newArr removeObject:aPoi];
            }
        }
    }
    _myTripArrivedPoisArray = [NSArray arrayWithArray:newArr];
}

- (BOOL)myTripListContainsPoi:(IDPoi*)aPoi
{
    return [self myTripListContainsPoi:aPoi withArray:_myTripArrivedPoisArray];
}

- (BOOL)myTripListContainsPoi:(IDPoi*)aPoi withArray:(NSArray*)arr
{
    NSArray* filteredArr = [arr filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"SELF.identifier = %@", aPoi.identifier]];
    
    return [filteredArr firstObject] != nil;
}

- (void)setMyTripNavigationPoi:(IDPoi*)aPoi
{
    [[NSUserDefaults standardUserDefaults] setObject:aPoi.identifier forKey:kUserDefaultsTripNavigationPoiKey];
}

-(void)setLocationSharingMode:(BOOL)mode
{
   [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsLocationSharingModeKey];
}

-(void)setAnalyticsMode:(BOOL)mode
{
   [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsAnalyticsModeKey];
}

- (void)setUserLocationsMode:(BOOL)mode
{
   [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsUserLocationsModeKey];
}

- (void)setProximityTriggerMode:(BOOL)mode
{
   [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsProximityTriggerModeKey];
}

- (void)setGeofenceDebugMode:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsGeofenceDebugModeKey];
}

- (void)setsetSimplifiedInstructionStatus:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultssetSimplifiedInstructionStatus];
}

- (void)setUserID:(NSString*)userID
{
    [[NSUserDefaults standardUserDefaults] setObject:userID forKey:kUserDefaultsUserIDKey];
}

- (void)setProjectID:(NSString*)projectID
{
    [[NSUserDefaults standardUserDefaults] setObject:projectID forKey:kUserDefaultsProjectIDKey];
}

- (void)setDeveloperUser:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsDeveloperUserKey];
}

- (void)setMapType:(NSInteger)type
{
    [[NSUserDefaults standardUserDefaults] setInteger:type forKey:kUserDefaultsOutDoorMapTypeKey];
}

- (void)setMapZoomLevel:(double)level {
    [[NSUserDefaults standardUserDefaults] setDouble:level forKey:kUserDefaultsMapZoomLevel];
}

- (void)setMapRotation:(NSInteger)type
{
    [[NSUserDefaults standardUserDefaults] setInteger:type forKey:kUserDefaultsInDoorMapRotationKey];
}

- (void)setUserLayer:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsUserLayerKey];
}

- (void)setCampusLayer:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsCampusLayerKey];
}

- (void)setFacilityLayer:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsFacilityLayerKey];
}

- (void)setPoisLayer:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsPoisLayerKey];
}

- (void)setPathsLayer:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsPathsLayerKey];
}

- (void)setRouteLayer:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsRouteLayerKey];
}

- (void)setLabelsLayer:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsLabelsLayerKey];
}

- (void)setVisibleCategories:(NSArray *)categories
{
    [[NSUserDefaults standardUserDefaults] setObject:categories forKey:kUserDefaultsVisibleCategoriesKey];
}

- (void)setSimulationMode:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsSimulationModeKey];
}

- (void)setSimulatedLocationMode:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsSimulatedModeKey];
}

- (void)setMutedMode:(BOOL)mode
{
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUserDefaultsMuteSoundModeKey];
}

- (void)setUserPersonalLanguage:(NSString *)language
{
    [[NSUserDefaults standardUserDefaults] setObject:language forKey:kSaveLanguageDefaultKey];
    [self storeDefaults];
}

- (void)setUserPersonalPoi:(NSDictionary *)poiData
{
    [[NSUserDefaults standardUserDefaults] setObject:poiData forKey:kUserDefaultsPoiFavorites];
    [self storeDefaults];
}

- (void)setUserPersonalRecentSearch:(NSDictionary *)recentSearchData
{
    [[NSUserDefaults standardUserDefaults] setObject:recentSearchData forKey:kUserDefaultsRecentSearch];
    [self storeDefaults];
}

- (void)storeDefaults
{
    [[NSUserDefaults standardUserDefaults] synchronize];
}

- (void)setVisibleSearchHelpScreen:(BOOL)visible{
    [[NSUserDefaults standardUserDefaults] setBool:visible forKey:kVisibleSearchHelpScreenKey];
    [self storeDefaults];
}

- (void)setUsedThirdPartyNavigation:(BOOL)mode {
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUsedThirdPartyNavigation];
    [self storeDefaults];
}

-(void)setSimulatedLocation:(IDUserLocation *)simulatedLocation {
    NSData *encodedObject = [NSKeyedArchiver archivedDataWithRootObject:simulatedLocation];
    [[NSUserDefaults standardUserDefaults] setObject:encodedObject forKey:API_KEY];
    [self storeDefaults];
}

-(void)setUseSimulationDefaultLocation:(BOOL)mode {
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kUseSimulationDefaultLocation];
    [self storeDefaults];
}

-(void)setRememberMe:(BOOL)mode {
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kRememberMe];
    [self storeDefaults];
}

-(void)setProjectSelect:(BOOL)mode {
    [[NSUserDefaults standardUserDefaults] setBool:mode forKey:kProjectSelect];
    [self storeDefaults];
}

- (void)setDisplayTopFloorContent:(BOOL)display
{
    [[NSUserDefaults standardUserDefaults] setBool:display forKey:kDisplayTopFloorContentKey];
    NSLog(@"Storing: %d", display);
    [self storeDefaults];
}

#ifdef HEALTHCARE
- (void)setStaffMode:(BOOL)isStaffMode {
    [[NSUserDefaults standardUserDefaults] setBool:isStaffMode forKey:kStaffModeKey];
}

- (void)setOneTimeStaffMode:(BOOL)isOneTimeStaffMode {
    [[NSUserDefaults standardUserDefaults] setBool:isOneTimeStaffMode forKey:kOneTimeStaffModeKey];
}
#endif

#ifdef NIKE
- (void)setStaffMode:(BOOL)isStaffMode {
    [[NSUserDefaults standardUserDefaults] setBool:isStaffMode forKey:kStaffModeKey];
}

- (void)setOneTimeStaffMode:(BOOL)isOneTimeStaffMode {
    [[NSUserDefaults standardUserDefaults] setBool:isOneTimeStaffMode forKey:kOneTimeStaffModeKey];
}
#endif
#pragma mark - Location Sharing
#pragma mark - Getters

- (NSString*)getProfileLogin {
    return [[NSUserDefaults standardUserDefaults] stringForKey:kProfileLoginKey];
}

- (NSString*)getProfilePassword {
    return [[NSUserDefaults standardUserDefaults] stringForKey:kProfilePasswordKey];
}

- (NSString*)getProfileId {
    return [[NSUserDefaults standardUserDefaults] stringForKey:kProfileIdKey];
}

- (NSString*)getProfileIconUrl {
    return [[NSUserDefaults standardUserDefaults] stringForKey:kProfileIconUrlKey];
}

- (BOOL)getShareMyLocationMode {
    return [[NSUserDefaults standardUserDefaults] stringForKey:kProfileIsSharingLocationKey].boolValue;
}

- (NSArray*)getAppsSettings {
    return [[NSUserDefaults standardUserDefaults] objectForKey:kAppSettingsKey];
}

#pragma mark - Setters

- (void)setProfileLogin:(NSString*)login {
    [[NSUserDefaults standardUserDefaults] setObject:login forKey:kProfileLoginKey];
    [self storeDefaults];
}

- (void)setProfilePassword:(NSString*)password {
    [[NSUserDefaults standardUserDefaults] setObject:password forKey:kProfilePasswordKey];
    [self storeDefaults];
}

- (void)setProfileId:(NSString*)profileId {
    [[NSUserDefaults standardUserDefaults] setObject:profileId forKey:kProfileIdKey];
    [self storeDefaults];
}

- (void)setProfileIconUrl:(NSString*)iconUrl {
    [[NSUserDefaults standardUserDefaults] setObject:iconUrl forKey:kProfileIconUrlKey];
    [self storeDefaults];

}

- (void)setShareMyLocationMode:(BOOL)isSharing {
    [[NSUserDefaults standardUserDefaults] setObject:@(isSharing) forKey:kProfileIsSharingLocationKey];
    [self storeDefaults];
    
}

- (void)setAppsSettings:(NSArray*)appSettings {
    [[NSUserDefaults standardUserDefaults] setObject:appSettings forKey:kAppSettingsKey];
    [self storeDefaults];
}

@end
