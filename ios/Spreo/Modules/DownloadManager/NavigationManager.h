//
//  NavigationManager.h
//  Spreo
//
//  Created by Yury Tulup on 11.05.17.
//  Copyright © 2017 Spreo LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <IndoorKit/IndoorKit.h>
#import "Global.h"
#import "UserDefaults.h"

@interface NavigationManager : NSObject

+ (instancetype)sharedManager;

@property (nonatomic, strong) IDPoi *toPoi;
@property (nonatomic, assign) BOOL isGoNavigation;
@property (nonatomic, strong) IDLocation *fromLocation;

@property (nonatomic, strong) IDLocation *parkingLocation;
@property (nonatomic, assign) BOOL isGoToParkinkingNavigation;

@property (nonatomic, strong) IDUserLocation *simulatedLocation;

@property (nonatomic, assign) BOOL putUserInCampusNoLocation;

- (void)setDefaultUserLocationForFromLocation:(IDLocation*)fromLocation isPutUserInCampus:(BOOL)putUserInCampus;
- (IDUserLocation*)getDefaultUserLocation;
- (void)setDefaultLocationToSimulatedLocation;
- (void)setUserLocationForFromLocation:(IDLocation*)fromLocation isPutUserInCampus:(BOOL)putUserInCampus;

@end
