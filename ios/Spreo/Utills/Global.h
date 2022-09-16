//
//  Global.h
//  IndoorKitDemo
//
//  Copyright (c) 2015 Spreo LLC. All rights reserved.
//

#import <IndoorKit/IndoorKit.h>

#define BASE_URL                    [IDKit getServerURL]//@"https://developer.spreo.co/middle/ios/"
#define SPREO_API_KEY @"fdaf38bf053c49f3adba7c0c5d11621315577798257061405987126"
#define API_KEY     [[UserDefaults sharedDefaults] kAppApiKey]
#define ICONS_IMAGES_DIRECTORY      @"Images/Instruction"
#define ICONS_MAP_DIRECTORY      @"Images/MapIcons"



#define SIMULATION_NAVIGATION
#define GOOGLE_API_KEY @"AIzaSyBWOQ_qhgpGelrvCSKdoFWU3QAh6xS_ZMg"
#define SMSReceived_Vibrate     1011
#define kCampusType             @"campus"
#define kFacilityType           @"facility"
#define kBannerGeoId            @"banner"
#define kElevatorGeoId          @"elevator"
#define kBookableId             @"bookable"

//////////////////////////////////////////////////////////////////////////////////////////
//Global Identifiers
//////////////////////////////////////////////////////////////////////////////////////////
#define kCampusId   [[IDKit getCampusIDs] firstObject]
#define kFacilityId [[IDKit getFacilityIDsForCampusID:kCampusId] firstObject]
#define MAX_DISTANCE_FROM_CAMPUS        50000
