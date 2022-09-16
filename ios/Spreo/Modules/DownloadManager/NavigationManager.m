//
//  NavigationManager.m
//  Spreo
//
//  Created by Yury Tulup on 11.05.17.
//  Copyright © 2017 Spreo LLC. All rights reserved.
//

#import "NavigationManager.h"

@implementation NavigationManager

+ (instancetype)sharedManager {
    static NavigationManager *navManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        navManager = [[self alloc]init];
    });
    return navManager;
}

- (void)setIsGoNavigation:(BOOL)isGoNavigation {
    _isGoNavigation = isGoNavigation;
    _isGoToParkinkingNavigation = NO;
}

- (void)setIsGoToParkinkingNavigation:(BOOL)isGoToParkinkingNavigation {
    _isGoNavigation = isGoToParkinkingNavigation;
    _isGoToParkinkingNavigation = isGoToParkinkingNavigation;
}



- (void)setDefaultUserLocationForFromLocation:(IDLocation*)fromLocation isPutUserInCampus:(BOOL)putUserInCampus {
    IDUserLocation *userLoc = nil;
    if (fromLocation == nil) {
            //[IDKit setUserLocation:nil];
            [IDKit startUserLocationTrack];
     } else {
        if (!fromLocation.isIndoor) {
            userLoc = [[IDUserLocation alloc]
                                  initWithCampusId:fromLocation.campusId
                                  facilityId:fromLocation.facilityId
                                  outCoordinate:fromLocation.outCoordinate
                                  inCoordinate:fromLocation.inCoordinate
                                  andFloorId:fromLocation.floorId];
        } else {
            userLoc = [[IDUserLocation alloc]
                       initWithCampusId:fromLocation.campusId
                       facilityId:fromLocation.facilityId
                       outCoordinate:fromLocation.outCoordinate
                       inCoordinate:fromLocation.inCoordinate
                       andFloorId:fromLocation.floorId];
        }
       // [IDKit setForceUserLocation:nil];
        [IDKit setForceUserLocation:userLoc];
    }
}

- (IDUserLocation*)getDefaultUserLocation {
    NSDictionary* info  = [IDKit getInfoForCampusWithID:kCampusId];
    CLLocation *clocation = nil;
    if (info[@"default_location"] != nil) {
        clocation = info[@"default_location"];
    } else if (info[@"location"] != nil) {
        clocation = info[@"location"];
    }
    
    if (clocation != nil) {
        IDUserLocation* location = [[IDUserLocation alloc] initWithCampusId:kCampusId
                                                                 facilityId:nil
                                                              outCoordinate:clocation.coordinate
                                                               inCoordinate:CGPointZero
                                                                 andFloorId:0];
        return location;
    }
    return nil;
}

- (void)setDefaultLocationToSimulatedLocation {
    _simulatedLocation = [self getDefaultUserLocation];
}


- (void)setUserLocationForFromLocation:(IDLocation*)fromLocation isPutUserInCampus:(BOOL)putUserInCampus {
    IDUserLocation *userLoc = nil;
    if (fromLocation == nil) {
        
        if ([IDKit getProjectLocationType] == kNO_LOCATION && [[UserDefaults sharedDefaults] simulationMode]) {
           // [IDKit stopUserLocationTrack];
            userLoc = [IDKit getUserLocation];
            [IDKit setUserLocation:userLoc];
            return;
        }
        
        if ([[UserDefaults sharedDefaults] simulatedLocationMode]) {
            if ([NavigationManager sharedManager].simulatedLocation && ([IDKit getProjectLocationType] != kNO_LOCATION )) {
                userLoc = [NavigationManager sharedManager].simulatedLocation.copy;
            } else {
                userLoc = [IDKit getUserLocation];
            }
            [IDKit setUserLocation:nil];
            [IDKit setUserLocation:userLoc];
        } else {
            [IDKit setUserLocation:nil];
            if (putUserInCampus || self.putUserInCampusNoLocation) {
                IDUserLocation *location = [self getDefaultUserLocation];
                if (location) {
                    userLoc = [IDKit getUserLocation];
                    userLoc.floorId = 0;
                    [IDKit setUserLocation:userLoc];
                    userLoc = location;
                    [IDKit setUserLocation:nil];
                    [IDKit setUserLocation:userLoc];
                }
            } else {
                userLoc = [IDKit getUserLocation];
                userLoc.floorId = 0;
                [IDKit setUserLocation:userLoc];
                [IDKit startUserLocationTrack];
                [IDKit setCurrentUserLocation];
            }
        }
    } else {
        if (!fromLocation.isIndoor) {
            userLoc = [IDKit getUserLocation];
            userLoc.floorId = 0;
           // [IDKit setUserLocation:userLoc];
            
            userLoc = [[IDUserLocation alloc]
                       initWithCampusId:fromLocation.campusId
                       facilityId:nil
                       outCoordinate:fromLocation.outCoordinate
                       inCoordinate:CGPointZero
                       andFloorId:0];
        } else {
            userLoc = [[IDUserLocation alloc]
                       initWithCampusId:fromLocation.campusId
                       facilityId:fromLocation.facilityId
                       outCoordinate:fromLocation.outCoordinate
                       inCoordinate:fromLocation.inCoordinate
                       andFloorId:fromLocation.floorId];
        }
        [IDKit setUserLocation:nil];
        [IDKit setUserLocation:userLoc];
    }
}



@end
