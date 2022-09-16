//
//  UserDefaults.h
//  Spreo
//
//  Created by Hasan Sa on 7/7/14.
//  Copyright (c) 2014 Spreo LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <IndoorKit/IndoorKit.h>
#import "Global.h"
#import "UserDefaults.h"

@interface UserDefaults : NSObject

#pragma mark - Getters
+ (UserDefaults *)sharedDefaults;
- (BOOL)getSimplifiedInstructionStatus;
- (NSString*)userID;
- (NSString*)projectID;
- (BOOL)developerUser;
- (IDMapType)mapType;
- (NSInteger)mapRotation;
- (double)mapZoomLevel;
- (BOOL)userLayer;
- (BOOL)campusLayer;
- (BOOL)facilityLayer;
- (BOOL)poisLayer;
- (BOOL)pathsLayer;
- (BOOL)routeLayer;
- (BOOL)labelsLayer;
- (NSArray *)visibleCategories;
- (BOOL)muteMode;
- (BOOL)simulationMode;
- (BOOL)simulatedLocationMode;
- (NSString *)language;
- (NSDictionary *)userPersonalPois;
- (NSDictionary *)userPersonalRecentSearch;
- (NSArray*)myTripPois;
- (IDPoi*)myTripNavigationPio;
- (NSArray*)myTripArrivedPois;
- (BOOL)myTripListContainsPoi:(IDPoi*)aPoi;
- (BOOL)myTripListContainsPoi:(IDPoi*)aPoi withArray:(NSArray*)arr;
- (BOOL)multiApikeysMode;
- (BOOL)proximityTrigger;
- (BOOL)geofenceDebugMode;
- (BOOL)visibleSearchHelpScreen;
- (BOOL)usedThirdPartyNavigation;
//
- (BOOL)analyticsMode;
- (BOOL)locationSharingMode;
- (BOOL)userlocationsMode;

-(IDUserLocation *)simulatedLocation;

- (BOOL)useSimulationDefaultLocation;

- (BOOL)rememberMeMode;
- (BOOL)projectSelect;

- (BOOL)displayTopFloorContent;

#ifdef HEALTHCARE
- (BOOL)staffMode;
- (BOOL)oneTimeStaffMode;
#endif

#ifdef NIKE
- (BOOL)staffMode;
- (BOOL)oneTimeStaffMode;
#endif

//
- (NSString*)kAppApiKey;
//

#pragma mark - Setters
- (void)setAppApiKey:(NSString*)anApiKey;
- (void)setMultiApikeysMode:(BOOL)aMode;

- (void)setLocationSharingMode:(BOOL)aMode;
- (void)setAnalyticsMode:(BOOL)mode;
- (void)setUserLocationsMode:(BOOL)mode;

- (void)updateMyTripPoisWithdArray:(NSArray*)sortedArr;
- (void)updateMyTripPoiWithPoi:(IDPoi*)aPoi add:(BOOL)add;
- (void)updateMyTripArrivedPoisWithPoi:(IDPoi*)aPoi add:(BOOL)add;
- (void)setMyTripNavigationPoi:(IDPoi*)aPoi;
- (void)setsetSimplifiedInstructionStatus:(BOOL)mode;
- (void)setUserID:(NSString*)userID;
- (void)setProjectID:(NSString*)projectID;
- (void)setDeveloperUser:(BOOL)mode;
- (void)setMapType:(NSInteger)type;
- (void)setMapRotation:(NSInteger)type;
- (void)setUserLayer:(BOOL)mode;
- (void)setCampusLayer:(BOOL)mode;
- (void)setFacilityLayer:(BOOL)mode;
- (void)setPoisLayer:(BOOL)mode;
- (void)setPathsLayer:(BOOL)mode;
- (void)setRouteLayer:(BOOL)mode;
- (void)setLabelsLayer:(BOOL)mode;
- (void)setVisibleCategories:(NSArray *)categories;
- (void)setProximityTriggerMode:(BOOL)mode;
- (void)setGeofenceDebugMode:(BOOL)mode;
- (void)setSimulationMode:(BOOL)mode;
- (void)setSimulatedLocationMode:(BOOL)mode;
- (void)setMutedMode:(BOOL)mode;
- (void)setUserPersonalLanguage:(NSString *)language;
- (void)setUserPersonalPoi:(NSDictionary *)poiData;
- (void)setUserPersonalRecentSearch:(NSDictionary *)recentSearchData;
- (void)setVisibleSearchHelpScreen:(BOOL)visible;
- (void)setUsedThirdPartyNavigation:(BOOL)mode;
- (void)setSimulatedLocation:(IDUserLocation *)simulatedLocation;
- (void)setUseSimulationDefaultLocation:(BOOL)mode;
- (void)setRememberMe:(BOOL)mode;
- (void)setProjectSelect:(BOOL)mode;
- (void)setMapZoomLevel:(double)level;
- (void)setDisplayTopFloorContent:(BOOL)display;

#ifdef HEALTHCARE
- (void)setStaffMode:(BOOL)isStaffMode;
- (void)setOneTimeStaffMode:(BOOL)isOneTimeStaffMode;
#endif

#ifdef NIKE
- (void)setStaffMode:(BOOL)isStaffMode;
- (void)setOneTimeStaffMode:(BOOL)isOneTimeStaffMode;
#endif
#pragma mark - Location Sharing
#pragma mark - Getters
- (NSString*)getProfileLogin;
- (NSString*)getProfilePassword;
- (NSString*)getProfileId;
- (NSString*)getProfileIconUrl;
- (BOOL)getShareMyLocationMode;
- (NSArray*)getAppsSettings;

#pragma mark - Setters
- (void)setProfileLogin:(NSString*)login;
- (void)setProfilePassword:(NSString*)password;
- (void)setProfileId:(NSString*)profileId;
- (void)setProfileIconUrl:(NSString*)iconUrl;
- (void)setShareMyLocationMode:(BOOL)isSharing;
- (void)setAppsSettings:(NSArray*)appSettings;

@end
