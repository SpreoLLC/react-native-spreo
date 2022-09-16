//
// .m
// react-native-sipl-spreo
//
// Created by Shriom on 22/11/19.
//

#import "Spreo.h"
#import <React/RCTLog.h>

@implementation Spreo

RCT_EXPORT_MODULE()

@synthesize bridge = _bridge;

RCTResponseSenderBlock callbackSetup;
RCTResponseSenderBlock callbackParkingSetup;
RCTResponseSenderBlock callbackBluetooth;
RCTResponseSenderBlock callbackNavigation;

#pragma mark -
#pragma mark Initiate methods

- (UIView *)view
{
    isFirstTime = true;
    isOnCampus = true;
    isOffCampus = true;
    [IDKit setDefaultUserLocation:[[NavigationManager sharedManager] getDefaultUserLocation].outCoordinate];
    [self initMapViewController];
    [self initDictionaryOfNavApps];
    [self initInstructionsController];
    [IDKit setClusterLabel:true];
    [IDKit setNavigationDelegate:self];
    [IDKit registerToLocationListenerWithDelegate:self];
    [IDKit setNavigationDistanceUnitToFeet:true];
    [IDKit setAutomaticReRoute:false];
    UIView* view1 = [[UIView alloc] initWithFrame:CGRectMake( 0.0f, 0.0f, [[UIScreen mainScreen]bounds].size.width, [[UIScreen mainScreen]bounds].size.height)];
    self.mapVC.view.frame = view1.frame;
    [view1 addSubview:self.mapVC.view];
  
    return view1;
}

// Initialize mapview
- (void)initMapViewController {
        gpsBool = true;
        distanceIn = 1;
        self.floorIdentifier = @"";
        [IDKit setDisplayUserLocationIcon:false];
        [self.mapVC setUserMarkerDisplay:true];
        self.mapVC = [IDKit getDualMapViewController];
        [self.mapVC provideGoogleMapsAPIKey:      GOOGLE_API_KEY];
        self.mapVC.settings.indoorPicker = NO;
        self.mapVC.settings.myLocationButton = NO;
        self.mapVC.delegate = self;
        self.mapVC.putUserInCampus = NO;
        [self.mapVC setMapRotationMode:KIDMapRotationNavigation];
        [self.mapVC exitFollowMeMode];
        [self.mapVC setMapAutoFollowUserMode:YES];
        [self.mapVC setMinZoom:3 maxZoom:30.0];
        [self.mapVC placeOriginMarker:false];
        [self.mapVC setUserMarkerDisplay:true];
        NSString * imagePath = [NSString stringWithFormat:@"IndoorKit.bundle/%@/",ICONS_MAP_DIRECTORY];
        NSURL*url = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:
                                                  [imagePath stringByAppendingString:@"userIcon"] ofType:@"png"]];
        UIImage *img = [UIImage imageWithContentsOfFile:url.path];
        [IDKit setCustomUserLocationIcon:img];
        [self.mapVC showAllFacilityIDPopup];
    
        dispatch_async(dispatch_get_main_queue(), ^{
            [self startLocationTimer];
        });
    
}


// Initialize Instructor controller..
- (void)initInstructionsController {
    // get instruction controller
    self.instructionController = [IDKit getInstructionsController];
    self.instructionController.delegate = self;
    [self.instructionController playInstructionSound];
}

  
//Method to check bluetooth
RCT_EXPORT_METHOD(checkSetting:(BOOL)isBleAlert  callback:(RCTResponseSenderBlock)callback)
{
    callbackBluetooth = callback;
    selectedFloor = -40;
    timerLocationCheck = false;
    self.bluetoothManager = [[CBCentralManager alloc] initWithDelegate:self queue:dispatch_get_main_queue() options:@{CBCentralManagerOptionShowPowerAlertKey :(isBleAlert)?@true:@false}];
}

////Method to open phone setting
RCT_EXPORT_METHOD(openSetting)
{
    if (@available(iOS 10.0, *)) {
        dispatch_async(dispatch_get_main_queue(), ^{
              [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString] options:[NSDictionary dictionary] completionHandler:nil];
        });
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
               [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
        });
    }
}


//Method to check bluetooth
RCT_EXPORT_METHOD(checkLocation)
{
   // [self checkBlueDotLocation];
}

-(void)startLocationTimer {
     totalSeconds = 5;//your time
     timer = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self
     selector:@selector(updateCountDownTime) userInfo:nil repeats:YES];
 }

-(void)updateCountDownTime {
    if( totalSeconds != 0) {
       totalSeconds -= 1;
    } else {
         [self checkLocationAvailable];
         [self checkBlueDotLocation];
    }
}

- (void)checkBlueDotLocation{
    if (isBluetooth || isLocationEnable || [self checkUserOutCampus]){
       
        if (self->isOffCampus){
            [self.mapVC setMapAutoFollowUserMode:NO];
            [IDKit setDisplayUserLocationIcon:false];
            [self.mapVC setUserMarkerDisplay:false];
            [self.mapVC placeOriginMarker:false];
           
            dispatch_async (dispatch_get_main_queue(), ^{
                [IDKit setDefaultUserLocation:[[NavigationManager sharedManager] getDefaultUserLocation].outCoordinate];
                [self.mapVC presentLocation:[[NavigationManager sharedManager] getDefaultUserLocation]];
            });
            
            self->isOffCampus = false;
            self->isOnCampus = true;
        }
    }else{
        if (![self checkUserOutCampus]){
            _myCurrentPosition = true;
        
            if (self->isOnCampus){
               
              [self.mapVC setMapAutoFollowUserMode:YES];
                
               if ([self.from_poi.title  isEqualToString:@"My Current Position"] && self.to_poi != nil){
                   [IDKit setDisplayUserLocationIcon:true];
                   [self.mapVC setUserMarkerDisplay:true];
                   [self.mapVC placeOriginMarker:false];
               }else if (self.from_poi != nil &&  self.to_poi != nil){
               }else{
                   [IDKit setDisplayUserLocationIcon:true];
                   [self.mapVC setUserMarkerDisplay:true];
                   [self.mapVC placeOriginMarker:false];
               }
              
              dispatch_async (dispatch_get_main_queue(), ^{
                [self.mapVC presentLocation:[IDKit getUserLocation]];
                [self.mapVC showMyPosition];
              });
                
              self->isOnCampus = false;
              self->isOffCampus = true;
           }
        }
    }
}


- (void)checkUserLocation{
    //timerLocationCheck = true;
    //self.bluetoothManager = [[CBCentralManager alloc] initWithDelegate:self queue:dispatch_get_main_queue() options:@{CBCentralManagerOptionShowPowerAlertKey :@false}];
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central
{
    NSMutableDictionary *dict_setting = [[NSMutableDictionary alloc] init];
    
    switch(self.bluetoothManager.state)
    {
        case CBCentralManagerStateUnsupported:
            isBluetooth = false;
        break;
        case CBCentralManagerStateUnauthorized:
            isBluetooth = false;
        break;
        case CBCentralManagerStatePoweredOff:
            isBluetooth = true;
        break;
        case CBCentralManagerStatePoweredOn:
            isBluetooth = false;
        break;
        default:
            isBluetooth = false;
        break;
    }

    [dict_setting setValue:[NSNumber numberWithBool:!isBluetooth]  forKey:@"isBluetooth"];

    if ([CLLocationManager locationServicesEnabled]){
        if ([CLLocationManager authorizationStatus]==kCLAuthorizationStatusDenied){
            [dict_setting setValue:[NSNumber numberWithBool:false]  forKey:@"isLocation"];
            isLocationEnable = true;
        }else{
            [dict_setting setValue:[NSNumber numberWithBool:true]  forKey:@"isLocation"];
            isLocationEnable = false;
        }
    }else{
         isLocationEnable = true;
         [dict_setting setValue:[NSNumber numberWithBool:false]  forKey:@"isLocation"];
    }
   
    _myCurrentPosition = true;
    
    if ([self checkUserOutCampus]){
        [dict_setting setValue:[NSNumber numberWithBool:false]  forKey:@"isIncampus"];
    }else{
        [dict_setting setValue:[NSNumber numberWithBool:true]  forKey:@"isIncampus"];
    }
    
    [self checkBluetooth:dict_setting];
}

- (void)checkLocationAvailable{
   if ([CLLocationManager locationServicesEnabled]){
       if ([CLLocationManager authorizationStatus]==kCLAuthorizationStatusDenied){
           isLocationEnable = true;
       }else{
           isLocationEnable = false;
       }
   }else{
        isLocationEnable = true;
   }
}


-(void)checkBluetooth:(NSDictionary *)dict_setting{
    if (callbackBluetooth != nil){
        callbackBluetooth(@[dict_setting]);
        callbackBluetooth = nil;
    }
}

// Initialize Third Party apps..
-(void)initDictionaryOfNavApps {
    self.navAppsNamesWithUrl = [[NSMutableDictionary alloc]init];
    [self.navAppsNamesWithUrl setValue:@"http://maps.apple.com://" forKey:@"Apple Maps"];
    [self.navAppsNamesWithUrl setValue:@"comgooglemaps://" forKey:@"Google Maps"];
    [self.navAppsNamesWithUrl setValue:@"navigon://" forKey:@"Navigon"];
    [self.navAppsNamesWithUrl setValue:@"waze://" forKey:@"Waze"];
    [self.navAppsNamesWithUrl setValue:@"mapswithme://" forKey:@"Maps Me"];
    [self.navAppsNamesWithUrl setValue:@"inroute://" forKey:@"inRoute"];
    [self.navAppsNamesWithUrl setValue:@"here-route://" forKey:@"Here Maps"];
}

#pragma mark - React-Methods

//Method to get all poi's categories
RCT_EXPORT_METHOD(getAllPoiCategories:(RCTResponseSenderBlock)callbackpoiData)
{
    //self.poisListContent = [IDKit sortPOIsDistantlyWithPathID:[self poisPathId] fromLocation:[IDKit getUserLocation]];
    //NSLog(@"getAllPoiCategories");

    self.poisListContent =  [IDKit sortPOIsAlphabeticallyWithPathID:[self poisPathId]];
    self.poisListContent = [IDKit sortPOIsDistantly:self.poisListContent fromLocation:[IDKit getUserLocation]];
    
    self.allPoiSearch = [NSMutableArray array];
    self.allFilterPoisArray = [NSMutableArray array];
    for (int i=0; i<[self.poisListContent count]; i++) {
        IDPoi *poi = self.poisListContent[i];
        
        NSString* floorTitle = [self floorTitleFromLocation:poi.location];
        NSString* facilityTitle = [self facilityTitleFromLocation:poi.location];
        if ([poi.info[@"showInSearch"] intValue]){
           NSMutableDictionary *productDictionary = [[NSMutableDictionary alloc] init];
           [productDictionary setObject:poi.title forKey:@"title"];
            
            NSString *str_floor = [[IDKit getCurrentLanguage]  isEqualToString: @"he"] ? @"קומה" : @"Floor";
            
           if (facilityTitle != nil) {
               [productDictionary setObject:[NSString stringWithFormat:@"%@, %@ %@", facilityTitle, str_floor ,floorTitle] forKey:@"address"];
           }
           else {
               [productDictionary setObject:[NSString stringWithFormat:@"%@ ", facilityTitle] forKey:@"address"];
           }
           [productDictionary setObject:floorTitle forKey:@"floor"];
            [productDictionary setObject:[NSString stringWithFormat:@"%ld", (long)poi.location.floorId] forKey:@"floorId"];
           [productDictionary setObject:poi.info[@"icon"] forKey:@"icon"];
           [productDictionary setObject:poi.identifier forKey:@"poi"];
            
           if ([poi.info objectForKey:kKeywords]) {
               [productDictionary setValue:poi.info[kKeywords]   forKey:@"keywordtext"];
           } else {
               [productDictionary setValue:@""  forKey:@"keywordtext"];
           }
            
           [self.allPoiSearch addObject:productDictionary];
           [self.allFilterPoisArray addObject:poi];
        }
    }
    callbackpoiData(@[self.allPoiSearch]);
}

/*
 Search the main list for pois whose keywords matches searchText; add items that match to the filtered array.
 */
- (void)ifKeywordsContainsText:(NSString *)searchText isMainCategory:(BOOL)isMainCategory
{
  NSMutableSet* set = [NSMutableSet setWithArray:_searchResults];
  NSPredicate * predicate = [NSPredicate predicateWithFormat:@"SELF contains[cd] %@",searchText];
  
  for (IDPoi * poi in (isMainCategory ? self.poisListContent : self.subCategorypoisListContent)){
    if (poi.info[@"keywords"]) {
        NSArray  *keywordsArray = [(NSString *) poi.info[@"keywords"]  componentsSeparatedByString:@","];
        if([[keywordsArray filteredArrayUsingPredicate:predicate] count] > 0){
           // self.matchedString = [keywordsArray filteredArrayUsingPredicate:predicate];
            [set addObject:poi];
        }
    }
  }
   _searchResults = [NSMutableArray arrayWithArray: set.allObjects];
  
}



/*
 Search the main list for pois whose keywords matches searchText; add items that match to the filtered array.
 */
- (void)ifKeywordsBeginText:(NSString *)searchText isMainCategory:(BOOL)isMainCategory
{
  NSMutableSet* set = [NSMutableSet setWithArray:_searchResults];
 // NSPredicate * predicate = [NSPredicate predicateWithFormat:@"SELF contains[cd] %@",searchText];
    
  NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(SELF BEGINSWITH[c] %@)", searchText, searchText];
            
  //NSPredicate *predicate = [NSPredicate predicateWithFormat:@"%@ BEGINSWITH SELF", searchText];
                             
    for (IDPoi * poi in (isMainCategory ? self.poisListContent : self.subCategorypoisListContent)){
    if (poi.info[@"keywords"]) {
        NSArray  *keywordsArray = [(NSString *) poi.info[@"keywords"]  componentsSeparatedByString:@","];
        if([[keywordsArray filteredArrayUsingPredicate:predicate] count] > 0){
           // self.matchedString = [keywordsArray filteredArrayUsingPredicate:predicate];
            [set addObject:poi];
        }
    }
  }
   _searchResults = [NSMutableArray arrayWithArray: set.allObjects];
  
}


//Method to start & stop log while navigation
RCT_EXPORT_METHOD(getFilterAllPoiCategories:(NSString *)searchText isMainCategory:(BOOL)isMainCategory  callback:(RCTResponseSenderBlock)callback)
{
    
    NSArray <IDPoi*> *listContent;
    
    if (isMainCategory){
        listContent = self.poisListContent;
    }else{
        listContent = self.subCategorypoisListContent;
    }
    
    
    //End
     self.searchString = searchText;
     /*
      Update the filtered array based on the search text and scope.
      */
     if ((searchText == nil) || [searchText length] == 0) {
       _searchResults = [[NSMutableArray alloc] initWithArray:listContent];
       return;
     }
    
     [_searchResults removeAllObjects]; // First clear the filtered array.
     /*
      Search the main list for poi whose title matches searchText; add items that match to the filtered array.
      */
    
     NSMutableSet * set = [NSMutableSet set];
     NSArray  *searchArr = [(NSString *) self.searchString  componentsSeparatedByString:@" "];
    
     
     if (searchArr.count == 1){
     
         for (int i=0; i<[listContent count]; i++) {
              
              IDPoi *poi = listContent[i];

              NSArray  *keywordsSearchArray = [(NSString *) poi.info[@"keywords"]  componentsSeparatedByString:@","];
              
              for (int i=0; i<[keywordsSearchArray count]; i++) {
                  NSString *str_key = keywordsSearchArray[i];
                  if (([[str_key lowercaseString] hasPrefix:self.searchString.lowercaseString])) {
                      [set addObject:poi];
                  }
              }
             
              if (([[poi.title lowercaseString] hasPrefix:self.searchString.lowercaseString])) {
                  //do your stuff
                  [set addObject:poi];
              }
             
              if ([[poi.title lowercaseString] containsString:[NSString stringWithFormat:@" %@", self.searchString.lowercaseString]]){
                   [set addObject:poi];
              }
         }
         _searchResults = [NSMutableArray arrayWithArray: [set allObjects]];
        
    }else{
        if (searchArr.count > 1){
             NSPredicate * predicate = [NSPredicate predicateWithFormat:@"SELF.title contains[cd] %@",searchText];
             for (IDPoi * poi in [listContent filteredArrayUsingPredicate:predicate]){
                   [set addObject:poi];
              }
             _searchResults = [NSMutableArray arrayWithArray: [set allObjects]];
             [self ifKeywordsContainsText:searchText isMainCategory:isMainCategory];
        }else{
             NSPredicate *pred = [NSPredicate predicateWithFormat:@"(SELF.title BEGINSWITH[c] %@)", searchText];
                 for (IDPoi * poi in [listContent filteredArrayUsingPredicate:pred]){
                         [set addObject:poi];
                 }
             _searchResults = [NSMutableArray arrayWithArray: [set allObjects]];
             [self ifKeywordsBeginText:searchText isMainCategory:isMainCategory];
        }
    }
    
     NSMutableArray *allPoiSearchFilter = [NSMutableArray array];
    
     for (int i=0; i<[_searchResults count]; i++) {
         IDPoi *poi = _searchResults[i];
         
         NSString* floorTitle = [self floorTitleFromLocation:poi.location];
         NSString* facilityTitle = [self facilityTitleFromLocation:poi.location];
         if ([poi.info[@"showInSearch"] intValue]){
            NSMutableDictionary *productDictionary = [[NSMutableDictionary alloc] init];
            [productDictionary setObject:poi.title forKey:@"title"];
             
             NSString *str_floor = [[IDKit getCurrentLanguage]  isEqualToString: @"he"] ? @"קומה" : @"Floor";
             
            if (facilityTitle != nil) {
                [productDictionary setObject:[NSString stringWithFormat:@"%@, %@ %@", facilityTitle, str_floor ,floorTitle] forKey:@"address"];
            }
            else {
                [productDictionary setObject:[NSString stringWithFormat:@"%@ ", facilityTitle] forKey:@"address"];
            }
             
             [productDictionary setObject:floorTitle forKey:@"floor"];

             
            [productDictionary setObject:[NSString stringWithFormat:@"%ld", (long)poi.location.floorId] forKey:@"floorId"];
            [productDictionary setObject:poi.info[@"icon"] forKey:@"icon"];
            [productDictionary setObject:poi.identifier forKey:@"poi"];
             
            //NSOrderedSet *orderedSet = [NSOrderedSet orderedSetWithArray:self.matchedString];
            //NSArray *arrayWithoutDuplicates = [orderedSet array];
             
             if ([poi.info objectForKey:kKeywords]) {
                    NSArray  *keywordsArray = [(NSString *) poi.info[@"keywords"]  componentsSeparatedByString:@","];
                    [productDictionary setValue:keywordsArray   forKey:@"keywordtext"];
              } else {
                    [productDictionary setValue:@[@""]  forKey:@"keywordtext"];
              }
            [allPoiSearchFilter addObject:productDictionary];
         }
     }
    
     callback(@[allPoiSearchFilter]);
    
     //callback(@[self.searchResults]);
}

  
//Method to get all facility
RCT_EXPORT_METHOD(getBuildinglist:(RCTResponseSenderBlock)callbackFacilityData)
{
    //NSLog(@"getBuildinglist");
    
    self.allCampusFacilityIds = [NSMutableArray array];
    self.allCampusFacilityIds =  [IDKit getFacilityIDsForCampusID:kCampusId].mutableCopy;
    
    self.allCampusFacility = [NSMutableArray array];
    self.allCampusFacility =  [IDKit getFacilityTitlesForCampusID:kCampusId].mutableCopy;
    
    [self.allCampusFacility enumerateObjectsWithOptions:NSEnumerationReverse usingBlock:^(NSString *p, NSUInteger index, BOOL *stop) {
        if ([p isEqualToString:@"Outdoor Campus"]) {
            [self.allCampusFacilityIds removeObjectAtIndex:index];
            [self.allCampusFacility removeObjectAtIndex:index];
        }
    }];
    
    callbackFacilityData(@[self.allCampusFacility]);
}

//Method to set facility map
RCT_EXPORT_METHOD(showBuilding:(NSString *)facilityId)
{
    
    NSInteger int_index = [self.allCampusFacility indexOfObject:facilityId];
    
    //self.allCampusFacility = [IDKit getFacilityForName:facilityId];
    
    NSString *str_facilityId = [self.allCampusFacilityIds  objectAtIndex:int_index];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.mapVC exitFollowMeMode];
        [self.mapVC centerFacilityMapWithFacilityId:str_facilityId atCampusId:kCampusId];
    });
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(10 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
          [self.mapVC hideFacility];
    });
}

//Method to set simulation
RCT_EXPORT_METHOD(setSimulation:(BOOL)isSimulate)
{
    isSimulationOn = isSimulate;
}

// Method to set Handicapped Routing
RCT_EXPORT_METHOD(setHandicappedRouting:(BOOL)isHandicapped)
{
    [IDKit setHandicappedRouting:isHandicapped];
}

//Method to set distance in Feet == 1, meter == 2 and Both == 3
RCT_EXPORT_METHOD(setDistanceType:(NSInteger)distanceType)
{
    distanceIn = distanceType;
}

//Method to set disable sound
RCT_EXPORT_METHOD(setVoiceInstruction:(BOOL)isDisable)
{
    isDisableSound = isDisable;
}

//Method to get all poi's categories
RCT_EXPORT_METHOD(getAllFacilityFloors:(RCTResponseSenderBlock)callback)
{
        //NSLog(@"getAllFacilityFloors");
        NSString *campusId = [[IDKit getCampusIDs] firstObject];
        NSString* facilityId = [IDKit getMaxVenueIdFloorsCountAtCampusId:nil];
        self.myFacilityId = facilityId;
        NSDictionary* dict = [IDKit getInfoForFacilityWithID:facilityId atCmpusWithID:campusId];
        NSMutableArray *ary_floors = [[NSMutableArray alloc] init];
        if (dict!= nil) {
            self.pickerViewDataArray = [[NSArray alloc] init];  //dict[@"floors_titles"];
            self.floorsIndexes = [[NSArray alloc] init];  //dict[@"floors_indexes"];
            self.floorsIndexes = [[dict[@"floors_indexes"] reverseObjectEnumerator] allObjects];
            self.pickerViewDataArray = [[dict[@"floors_titles"] reverseObjectEnumerator] allObjects];
            if (self.pickerViewDataArray != nil){
                for (int i=0; i<self.pickerViewDataArray.count; i++) {
                    NSMutableDictionary *dict_floor = [[NSMutableDictionary alloc] init];
                    [dict_floor setValue:self.floorsIndexes[i] forKey:@"floorId"];
                    
                    NSString *s = @"\u05e7";
                    NSString *floorText = self.pickerViewDataArray[i];
                
                    if ([floorText.lowercaseString isEqualToString:s] && [[IDKit getCurrentLanguage]  isEqualToString: @"en"]){
                        [dict_floor setValue:@"G" forKey:@"floorText"];
                    }else{
                         [dict_floor setValue:self.pickerViewDataArray[i] forKey:@"floorText"];
                    }
                    
                    [ary_floors addObject:dict_floor];
                }
            }
        }
    callback(@[ary_floors]);
}

// Get the formatted distance of navigate user between from and to location
- (NSString *)getFromDistance:(double)distance{
    double timeEstimation =  [self.mapVC getTotalTimeEstimationServerRoute]/60;

    int numdistance = (int)distance;
    int numdistanceft = (int)distance * 3.28;
    int numTotalTime =  ceil((numdistance * 0.75) / 60); //ceil(timeEstimation);

    NSString *ft = @"";
    NSString *meter = @"מטר";
    NSString *min = @"דקות";

    NSString *str_distance =  [NSString stringWithFormat:@" %d %@, %d %@",numdistanceft,ft,numTotalTime,min];

    if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
        ft = @"ft";
        meter = @"m";
        min = @"min";
    }

    switch (distanceIn) {
    case 2:
        str_distance =  [NSString stringWithFormat:@" %d %@, %d %@",numdistance,meter ,numTotalTime,min];
    break;
    case 3:
         str_distance =  [NSString stringWithFormat:@" %d %@ (%d %@), %d %@",numdistanceft,ft,numdistance,meter,numTotalTime,min];
    break;
    default:
       str_distance =  [NSString stringWithFormat:@" %d %@, %d %@",numdistanceft,ft,numTotalTime,min];
    break;
    }

    return str_distance;
}

//Method to set current location in center
RCT_EXPORT_METHOD(showMyLocation)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        self->isOnCampus = true;
        self->isOffCampus = true;
    });
}

//Method to get all poi's categories
RCT_EXPORT_METHOD(getPoiCategories:(RCTResponseSenderBlock)callbackpoiData)
{
       NSMutableArray *tempArray = [[NSMutableArray alloc] init];
       NSArray<IDCategory *> *array = [IDKit getPOIsCategoriesWithFiltersListWithPathID:kCampusId];
       for(IDCategory *category in array){
                    NSMutableDictionary *dict_cat = [[NSMutableDictionary alloc] init];
                    [dict_cat setValue:category.name forKey:@"poitype"];
                    [dict_cat setValue:category.name forKey:@"poidescription"];
                    [dict_cat setValue:[NSNumber numberWithBool:category.showInCategories] forKey:@"showInCatgories"];
                    [dict_cat setValue:[NSNumber numberWithBool:category.showInMapFilter] forKey:@"showInMapFilter"];
                    [dict_cat setValue:category.image forKey:@"poiuri"];
                    [tempArray addObject:dict_cat];
     }

    [tempArray enumerateObjectsWithOptions:NSEnumerationReverse usingBlock:^(NSMutableDictionary *p, NSUInteger index, BOOL *stop) {
        if ([[p objectForKey:@"poitype"] isEqualToString:@"Bridge"]) {
            [tempArray removeObjectAtIndex:index];
        }
    }];
    
    
    callbackpoiData(@[tempArray]);
}

//Method to set all poi's categories
RCT_EXPORT_METHOD(setPoiCategoriesData:(NSArray *)filterCategoryData)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        if (filterCategoryData.count > 0) {
              [self.mapVC setVisiblePOIsWithCategories:filterCategoryData];
              [self.mapVC showAllPois];
        } else {
              NSArray *allPois = [[NSArray alloc] initWithObjects:@"All", nil];
              [self.mapVC setVisiblePOIsWithCategories:allPois];
              [self.mapVC showAllPois];
        }
    });
    
    if (![self.floorIdentifier  isEqualToString:@""]){
        IDPoi * selectedPOI = [self getPOIofIdentifier:self.floorIdentifier];
        [self showPoiOnMap:selectedPOI];
        dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 8 * NSEC_PER_SEC);
        dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
            self->_floorIdentifier = @"";
        });
    }
    
    
    if (_destinationPOI.length > 0){
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            if (self.currentPoi != nil && self.currentPoi.identifier == self->_destinationPOI){
                [self.mapVC setFixedDestinationLocation:self.currentPoi.location];
           //     [self poiSearchFromPoiDetails:self.currentPoi isFrom:false];
            }else {
                IDPoi * selectedPOI = [self getPOIofIdentifier:self->_destinationPOI];
                [self.mapVC setFixedDestinationLocation:selectedPOI.location];
          //       [self poiSearchFromPoiDetails:selectedPOI isFrom:false];
           }
            self->_destinationPOI = @"";
        });
    }
}

//Method to set all poi's categories
RCT_EXPORT_METHOD(showAllPois)
{
    
}
      
//Method to update map with floor id changes
RCT_EXPORT_METHOD(updateMapwithFloorId:(NSString *)floorId)
{
     dispatch_async(dispatch_get_main_queue(), ^{
        if ([floorId integerValue] != self.mapVC.currentPresentedFloorID) {
            //NSLog(@"exitFollowMeMode updateMapwithFloorId");
            [self.mapVC exitFollowMeMode];
            [self.mapVC prepareMapForVenuesForFloorId:floorId :false];
            //NSLog(@"prepareMapForVenuesForFloorId updateMapwithFloorId");
        }
     });
}

-(BOOL)isLocationTurnOn{
    if ([CLLocationManager locationServicesEnabled]){
         if ([CLLocationManager authorizationStatus]==kCLAuthorizationStatusDenied){
             return false;
         }else{
             return true;
         }
    }else{
        return false;
    }
}


//Method to set the marker on from location
RCT_EXPORT_METHOD(setMarkerOnFromLocation:(NSString *)aPoi)
{
    tempFromLocation = [IDKit getUserLocation];
    isLocationOn =  [self isLocationTurnOn];
    dispatch_async (dispatch_get_main_queue(), ^{
         IDPoi * selectedPOI = [self getPOIofIdentifier:aPoi];
         [self poiSearchFromPoiDetails:selectedPOI isFrom:true];
    });
}

//Method to set the marker on to location
RCT_EXPORT_METHOD(setMarkerOntoLocation:(NSString *)aPoi)
{
    _destinationPOI = aPoi;
    dispatch_async (dispatch_get_main_queue(), ^{
        if (self.currentPoi != nil && self.currentPoi.identifier == aPoi){
            [self.mapVC setFixedDestinationLocation:self.currentPoi.location];
            [self poiSearchFromPoiDetails:self.currentPoi isFrom:false];
        }else {
            IDPoi * selectedPOI = [self getPOIofIdentifier:aPoi];
            [self.mapVC setFixedDestinationLocation:selectedPOI.location];
            [self poiSearchFromPoiDetails:selectedPOI isFrom:false];
       }
    });
}

//Method to stop navigation
RCT_EXPORT_METHOD(navigationCanceled)
{
    [self closeBtnTapped];
}

//Method to reset the my current location
RCT_EXPORT_METHOD(resetFromLocation)
{
      gpsBool = true;
}

//Method to start & stop log while navigation
RCT_EXPORT_METHOD(startStopLog:(NSString *)btnStatus callback:(RCTResponseSenderBlock)callback)
{
    if ([btnStatus isEqualToString:@"BEACON"]){
          [[BeaconSimulator shared] startLogging];
          callback(@[@"NOURL"]);
    }else{
          if ([BeaconSimulator shared].filePath) {
               callback(@[[BeaconSimulator shared].filePath]);
          }
        [[BeaconSimulator shared] stopLogging];
    }
}

//Method to get the detail
RCT_EXPORT_METHOD(getPoiDetail:(NSString *)identifier callback:(RCTResponseSenderBlock)callback)
{
          IDPoi * poi = [self getPOIofIdentifier:identifier];
          NSString* floorTitle = [self floorTitleFromLocation:poi.location];
          NSString* facilityTitle = [self facilityTitleFromLocation:poi.location];
          NSMutableDictionary *productDictionary = [[NSMutableDictionary alloc] init];
          [productDictionary setObject:poi.title forKey:@"title"];
          
          NSString *str_floor = [[IDKit getCurrentLanguage]  isEqualToString: @"he"] ? @"קומה" : @"Floor";
          
         if (facilityTitle != nil) {
             [productDictionary setObject:[NSString stringWithFormat:@"%@ ,%@ %@", facilityTitle, str_floor ,floorTitle] forKey:@"address"];
         }
         else {
             [productDictionary setObject:[NSString stringWithFormat:@"%@ ", facilityTitle] forKey:@"address"];
         }
         [productDictionary setObject:floorTitle forKey:@"floor"];
         [productDictionary setObject:poi.info[@"icon"] forKey:@"icon"];
         [productDictionary setObject:poi.identifier forKey:@"poi"];
    
         [productDictionary setObject:[NSString stringWithFormat:@"%ld", (long)poi.location.floorId] forKey:@"floorId"];

         if ([poi.info objectForKey:kKeywords]) {
             [productDictionary setValue:poi.info[kKeywords]   forKey:@"keywordtext"];
         } else {
             [productDictionary setValue:@""  forKey:@"keywordtext"];
         }
        callback(@[productDictionary]);
}

 
//Method to start & stop log while navigation
RCT_EXPORT_METHOD(exportMotionLog:(NSString *)btnStatus callback:(RCTResponseSenderBlock)callback)
{
    if ([btnStatus isEqualToString:@"MOTION"]){
         [[MotionSimulator shared] startLogging];
          callback(@[@"NOURL"]);
    }else{
          if ([MotionSimulator shared].filePath) {
               callback(@[[MotionSimulator shared].filePath]);
          }
         [[MotionSimulator shared] stopLogging];
    }
}


//Method to start & stop log while navigation
RCT_EXPORT_METHOD(claerLogFiles:(NSString *)logfile)
{
    if ([logfile isEqualToString:@"BEACON"]){
        [[BeaconSimulator shared] clearLogFile];
    }else{
        [[MotionSimulator shared] clearLogFile];
    }
}


//Method to start navigation based on selected from and to POI
RCT_EXPORT_METHOD(startNavigation:(NSString *)startPoi andEndPoi:(NSString *)endPoi callback:(RCTResponseSenderBlock)callback)
{
    callbackNavigation = callback;

   
    if (self.from_poi == nil || [self.from_poi.title  isEqualToString:@"My Current Position"]){
        [IDKit setSimplifiedInstruction:true];
        _myCurrentPosition = true;
        IDUserLocation* location = [IDKit getUserLocation];
        if(location != nil && location.isIndoor){
            [self projectLocationTypeChecker];
        }else{
            //if (![IDKit isUserInCampus:MAX_DISTANCE_FROM_CAMPUS]){
             if([self checkUserOutCampus] ) {
                  [self isAvailableAppsExistsWithCompletionHandler:^(NSMutableDictionary *exists) {
                           NSArray * sortedKeys = [[exists allKeys] sortedArrayUsingSelector: @selector(caseInsensitiveCompare:)];
                            callback(@[sortedKeys]);;
                  }];
            }else{
//                if (isLocationEnable){
                    if (gpsBool){
                         callback(@[@"gpsNavigation"]);
                         gpsBool = false;
                    }else{
                        
                        
                        
                        [self projectLocationTypeChecker];
                    }
//                }else{
//                    [self projectLocationTypeChecker];
//                }
            }
        }
    }else{
        [IDKit setSimplifiedInstruction:true];
        _myCurrentPosition = false;
        dispatch_async (dispatch_get_main_queue(), ^{
             //[IDKit stopNavigation];
        });
        [self projectLocationTypeChecker];
    }
}

//Method to start navigation from selected parking to from location
RCT_EXPORT_METHOD(startNavigationToParking:(RCTResponseSenderBlock)callback)
{
    callbackNavigation = callback;
    isFromParking = true;
    [self navigateToParking];
}


//Method to return all third party apps for navigation redirection
RCT_EXPORT_METHOD(navigateThirdParty:(NSString*)selectedItem)
{
    [self navigateUseThirdPartyAppWithTitle:selectedItem andCoordinates:_to_poi.location.outCoordinate];
}

//Method to add current location as parking location
RCT_EXPORT_METHOD(markMyparking:(RCTResponseSenderBlock)callbackmap)
{
    [self saveParking];
    callbackmap(@[@"done"]);
}

//Method to remove parking location
RCT_EXPORT_METHOD(removeParkingLocation:(RCTResponseSenderBlock)callbackmap)
{
    dispatch_async (dispatch_get_main_queue(), ^{
         [IDKit removeParkingLocation];
    });
 
    callbackmap(@[@"done"]);
}
 

// Update Instruction
RCT_EXPORT_METHOD(updateInstructionView:(RCTResponseSenderBlock)callbackmap)
{
    NSMutableArray *ary_instruction = [[NSMutableArray alloc] init];
    NSMutableDictionary *dict_instruct = [[NSMutableDictionary alloc] init];
         
    if (isFromParking == true){
        [dict_instruct  setValue:@"true" forKey:@"isFromParking"];
    }else{
         [dict_instruct  setValue:@"false" forKey:@"isFromParking"];
    }
    total_distance = [self.mapVC getTotalDistanceOfNavigationRoute] ;
    [dict_instruct  setValue:[self getFromDistance:total_distance] forKey:@"distance"];
    [ary_instruction addObject:dict_instruct];
    
    callbackmap(@[ary_instruction]);
}

//Method to manage instruction of current and parking manually based on current instruction
RCT_EXPORT_METHOD(openNavView:(RCTResponseSenderBlock)callbackmap)
{
    _instructionList = [self.instructionController getInstructionsList];
    //NSString * imagePath = [NSString stringWithFormat:@"IndoorKit.bundle/%@/",ICONS_IMAGES_DIRECTORY];
 
    lastInstrId = [NSNumber numberWithInt:-1];
                       
    if(_instructionList != nil){
         NSMutableArray *ary_instruction = [[NSMutableArray alloc] init];
         NSMutableDictionary *dict_obj_parking = [[NSMutableDictionary alloc] init];
        
        for (int i=0; i< _instructionList.routes.count ; i++) {
            //IDRoute *d = [[_instructionList.routes[i] getFirstRoute] instructions];

            if ([_instructionList.routes[i] isKindOfClass:[IDCombinedRoute class]]) {
             //[_instructionList.routes[i] removeDuplicateInstructions];

                for (int k=0; k<[_instructionList.routes[i] routes].count; k++) {

                     NSArray *ary_firstObj =  [[_instructionList.routes[i] routes][k] instructions];

                     for (int j=0; j<ary_firstObj.count; j++) {
                             NSDictionary *dict_obj = [ary_firstObj objectAtIndex:j] ;
                             total_distance = [self.mapVC getTotalDistanceOfNavigationRoute] ;
                             //total_distance = [[dict_obj objectForKey:@"distance"] doubleValue];

                             NSNumber *currentInstrId =  [NSNumber numberWithInt:[[dict_obj objectForKey:@"id"] intValue]];
                             if (currentInstrId != lastInstrId && currentInstrId != [NSNumber numberWithInt:22]){
                                 [ary_instruction addObject:[self setDictionary:dict_obj]];
                                 lastInstrId = currentInstrId;
                             }

                      }
                 }

            }else{
                if (isFromParking == true){
                      NSDictionary *dict_obj =  [[_instructionList.routes[i] instructions] objectAtIndex:i];

                      total_distance = [self.mapVC getTotalDistanceOfNavigationRoute] ;

                     NSNumber *currentInstrId =  [NSNumber numberWithInt:[[dict_obj objectForKey:@"id"] intValue]];
                     if (currentInstrId != lastInstrId && currentInstrId != [NSNumber numberWithInt:22]){
                              [dict_obj_parking  setValue:[self getFromDistance:total_distance] forKey:@"distance"];
                              // total_distance = [[dict_obj objectForKey:@"distance"] doubleValue];
                              // [dict_obj_parking  setValue:[self getFromDistance:total_distance] forKey:@"distance"];
                              [dict_obj_parking  setValue:@"true" forKey:@"isFromParking"];
                              [ary_instruction addObject:dict_obj_parking];
                              [ary_instruction addObject:[self setDictionary:dict_obj]];
                              lastInstrId = currentInstrId;
                     }
                }else{

                    NSArray *ary_firstObj =  [_instructionList.routes[i] instructions];

                    for (int j=0; j<ary_firstObj.count; j++) {
                            NSDictionary *dict_obj = [ary_firstObj objectAtIndex:j];
                            total_distance = [self.mapVC getTotalDistanceOfNavigationRoute] ;
                            //total_distance = [[dict_obj objectForKey:@"distance"] doubleValue];
                            NSNumber *currentInstrId =  [NSNumber numberWithInt:[[dict_obj objectForKey:@"id"] intValue]];
                            if (currentInstrId != lastInstrId && currentInstrId != [NSNumber numberWithInt:22]){
                                [ary_instruction addObject:[self setDictionary:dict_obj]];
                                lastInstrId = currentInstrId;
                           }
                    }
                }
            }
        }
    
        
        int numdistance = (int)[self.mapVC getTotalDistanceOfNavigationRoute] * 3.28;
        
        if (numdistance <= 20){
           // [self playInstructionSound:anInstruction];
            if (self.from_poi == nil || [self.from_poi.title  isEqualToString:@"My Current Position"]){
                if (callbackNavigation != nil){
                    callbackNavigation(@[@"NavigationEnded"]);
                    callbackNavigation = nil;
                }
           }
        }
        
        callbackmap(@[ary_instruction]);
    }
}


- (void)navigationArriveToLocation:(IDLocation *)aLocation nextLocations:(NSArray *)nextLocations {
    if ((nil != nextLocations) && (0 < nextLocations.count)) {
        self.nextLocations = nextLocations;
       // [self presentAlertViewMultiDestinationContinue];
    }
}


//Method to show selected POI on map
RCT_EXPORT_METHOD(startLocationCheck)
{
    dispatch_async (dispatch_get_main_queue(), ^{
        self->isOnCampus = true;
        self->isOffCampus = true;
        [self checkLocationAvailable];
        [self checkBlueDotLocation];
    });
}


//Method to show selected POI on map
RCT_EXPORT_METHOD(showPoi:(NSString *)poiIdentifier)
{
    self.floorIdentifier = poiIdentifier;
    IDPoi * selectedPOI = [self getPOIofIdentifier:poiIdentifier];
    [self showPoiOnMap:selectedPOI];
    
   // NSString *str_interface = [NSString stringWithFormat:@"%@&poi=%@",[IDKit getWebInterfaceUrl],selectedPOI.identifier];
   // NSLog(@"web interface url --- %@",str_interface);
}



//Method to get WebInterfaceURL for sharing in social media
RCT_EXPORT_METHOD(getWebInterfaceURL:(RCTResponseSenderBlock)callback)
{
    NSString *str_interface = [NSString stringWithFormat:@"%@",[IDKit getWebInterfaceUrl]];
    NSLog(@"web interface url --- %@",str_interface);
    callback(@[str_interface]);
}



//Method to show poi details on selected poi
RCT_EXPORT_METHOD(setPoiContent:(NSString *)poiIdentifier callback:(RCTResponseSenderBlock)callback)
{
    if ([self.parkingPoi.identifier isEqualToString:poiIdentifier]){
        callbackParkingSetup = callback;
         [self showPoiDetails:self.parkingPoi];
    }else{
        IDPoi * selectedPOI = [self getPOIofIdentifier:poiIdentifier];
        _parkingPoi = [IDKit getNearbyParkingForPoi:selectedPOI];
        self.currentPoi = selectedPOI;
        callbackParkingSetup = callback;
        [self showPoiDetails:self.currentPoi];
    }
}
        
//Method to check parking is added or not and return status in true/false
RCT_EXPORT_METHOD(hasParkingLocation:(RCTResponseSenderBlock)callbackparking)
{
       IDLocation* locationToPresent = [IDKit getParkingLocation];
      
       if (nil != locationToPresent) {
           callbackparking(@[@"true"]);
       }else{
           callbackparking(@[@"false"]);
       }
}


//Method to get all facilities poi's categories
RCT_EXPORT_METHOD(menuCategories:(RCTResponseSenderBlock)callbackpoiData)
{
    [self loadFacilityCategoriesData];
    callbackpoiData(@[self.poisCategoriesArray]);
}


//Method to get all facilities data poi's categories
RCT_EXPORT_METHOD(poiSubCategories:(NSString *)categoryTitle callback:(RCTResponseSenderBlock)callbackpoiData)
{
    isSelectedSubcategory = true;
    callbackpoiData(@[[self loadPOIsFacilityData:categoryTitle]]);
}

//Method to change staff build route
RCT_EXPORT_METHOD(setStaffOnly:(BOOL)isStaffOnly)
{
    NSNumber *number = [NSNumber numberWithBool:isStaffOnly];
    [IDKit setStaffRouting:number.integerValue];
} 



#pragma mark - Private Category/subcategory Methods

/*** loading shared Data for allPOIData categories Array * **/
- (void)loadFacilityCategoriesData
{
    _poisCategoriesArray = [[NSArray alloc] init];
    NSMutableArray *tempArray = [[NSMutableArray alloc] init];
    NSArray<IDCategory *> *array = [IDKit getPOIsCategoriesWithFiltersListWithPathID:kCampusId];
    for(IDCategory *category in array){
        if(category.showInCategories){
            if (category.name.length > 0) {
                [tempArray addObject:category.name];
            }
        }
    }
    _poisCategoriesArray = [tempArray copy];
    //[poisCategoriesArray sortUsingSelector:@selector(localizedCaseInsensitiveCompare:)];
}


/*** loading shared Data for allPOIData Array * **/
- (NSMutableArray *)loadPOIsFacilityData:(NSString *)categoryTitle {
    NSMutableArray *ary_facilityData = [[NSMutableArray alloc] init];
    _poisSubCategoriesArray = [[NSMutableArray alloc] init];
    
    if (![categoryTitle  isEqualToString:@""]) {
        
          self.subCategorypoisListContent = [IDKit sortPOIsDistantlyWithCategories:@[categoryTitle] atPathID:kCampusId fromLocation: [IDKit getUserLocation]];
          [self.subCategorypoisListContent enumerateObjectsUsingBlock:^(IDPoi *facility, NSUInteger index, BOOL *stop){
             NSString* floorTitle = [self floorTitleFromLocation:facility.location];
             NSString* facilityTitle = [self facilityTitleFromLocation:facility.location];
             if ([facility.info[@"showInSearch"] intValue]){
                NSMutableDictionary *productDictionary = [[NSMutableDictionary alloc] init];
                [productDictionary setObject:facility.title forKey:@"title"];
                 
                 NSString *str_floor = [[IDKit getCurrentLanguage]  isEqualToString: @"he"] ? @"קומה" : @"Floor";
                            
                if (facilityTitle != nil) {
                    [productDictionary setObject:[NSString stringWithFormat:@"%@, %@ %@", facilityTitle, str_floor ,floorTitle] forKey:@"address"];
                }else {
                    [productDictionary setObject:[NSString stringWithFormat:@"%@ ", facilityTitle] forKey:@"address"];
                }
                 
                [productDictionary setObject:floorTitle forKey:@"floor"];
                 
                [productDictionary setObject:facility.info[@"icon"] forKey:@"icon"];
                [productDictionary setObject:facility.identifier forKey:@"poi"];
                 [productDictionary setObject:[NSString stringWithFormat:@"%ld", (long)facility.location.floorId] forKey:@"floorId"];

                 if ([facility.info objectForKey:kKeywords]) {
                     [productDictionary setValue:facility.info[kKeywords]   forKey:@"keywordtext"];
                 } else {
                     [productDictionary setValue:@""  forKey:@"keywordtext"];
                 }
                 
                [ary_facilityData addObject:productDictionary];
                [_poisSubCategoriesArray addObject:facility];
             }
              
              dispatch_async (dispatch_get_main_queue(), ^{
                 [self.mapVC setShowAllOnZoomLevel:facility :true];
              });
          }];
    }
    
    return ary_facilityData;
}
 

#pragma mark - Private Instruction Methods

//SM Need to manage code....

- (NSMutableDictionary *)setDictionary:(NSDictionary *)dict_obj{

    if (![IDKit getSimplifiedInstruction]){
        
        if ([[dict_obj objectForKey:@"id"] intValue] == 1){
            return [self getInstruction:[dict_obj objectForKey:@"text"] andImage:@"straight"];
        }else if ([[dict_obj objectForKey:@"id"] intValue] == 0){
                NSString *str_instruction = @"";
                if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
                    str_instruction = @"Stay on the left";
                }else{
                    str_instruction = @"תצמד לשמאל";
                }
            return [self getInstruction:str_instruction andImage:@"left_hall"];
        }else if ([[dict_obj objectForKey:@"id"] intValue] == 15){
            NSString *str_instruction = @"";
            if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
                str_instruction = @"Stay on the right";
            }else{
                str_instruction = @"תצמד לימין";
            }
            return [self getInstruction:str_instruction andImage:@"right_hall"];
        }
        else if ([[dict_obj objectForKey:@"id"] intValue] == 2 || [[dict_obj objectForKey:@"id"] intValue] == 14 ){
            
            if (![[[dict_obj objectForKey:@"parameter"] stringValue]  isEqual: @""]){
                
                   NSDictionary *startFloorTitle =  [IDKit getInfoForFloorID:[[dict_obj objectForKey:@"parameter"] intValue]
                              inFacilityWithID:[dict_obj objectForKey:@"facilityId"]
                                 atCmpusWithID:[dict_obj objectForKey:@"campusId"]];
                   
                   NSString *str_instruction = @"";
                   
                   if ([[dict_obj objectForKey:@"proceed"] isEqualToString:@"up"]){
                       if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
                           str_instruction = @"Go up to floor";
                       }else{
                           str_instruction = @"עלה לקומה";
                       }
                       return [self getInstruction:[NSString stringWithFormat:@"%@ %@",str_instruction,[startFloorTitle objectForKey:@"title"]] andImage:@"elevator_up"];
                   }else{
                       
                       if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
                           str_instruction = @"Go down to floor";
                       }else{
                           str_instruction = @"תרד לרצפה";
                       }
                       
                       return [self getInstruction:[NSString stringWithFormat:@"%@ %@",str_instruction,[startFloorTitle objectForKey:@"title"]] andImage:@"elevator_up"];
                   }
            }
            return [self getInstruction:[dict_obj objectForKey:@"text"] andImage:@"elevator_up"];
        }else if ([[dict_obj objectForKey:@"id"] intValue] == 3){
            return [self getInstruction:[dict_obj objectForKey:@"text"] andImage:@"turn_left"];
        }else if ([[dict_obj objectForKey:@"id"] intValue] == 4){
            return [self getInstruction:[dict_obj objectForKey:@"text"] andImage:@"turn_right"];
        }else if ([[dict_obj objectForKey:@"id"] intValue] == 5){
            
            NSString *str_destination;
            BOOL isCompoundInstuction = (nil != [dict_obj objectForKey:@"text"]);

            if (isCompoundInstuction){
                str_destination = [dict_obj objectForKey:@"text"];
            }else{
                if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
                    str_destination = @"Follow the highlighted path";
                }else{
                    str_destination = @"בצע את הנתיב המודגש";
                }
            }
            
            return [self getInstruction:str_destination andImage:@"destination3"];
        }else if ([[dict_obj objectForKey:@"id"] intValue] == 6){
            return [self getInstruction:[dict_obj objectForKey:@"text"] andImage:@"continue_to_path"];
        }else if ([[dict_obj objectForKey:@"id"] intValue] == 12 || [[dict_obj objectForKey:@"id"] intValue] == 11 || [[dict_obj objectForKey:@"id"] intValue] == 7){
            
            NSString *str_instruction = @"";
            if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
                str_instruction = @"Follow the line";
            }else{
                str_instruction = @"עקוב אחר הקו";
            }
            return  [self getInstruction:str_instruction andImage:@"continue_to_path"];
        
        }else{
            NSString *str_instruction = @"";
            if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
                str_instruction = @"Follow the line";
            }else{
                str_instruction = @"עקוב אחר הקו";
            }
            
            return [self getInstruction:str_instruction andImage:@"continue_to_path"];
        }
     
     }else{
         
         NSString *str_feet = @"מטר";
         if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
           str_feet = @"ft";
         }
         
         if ([[dict_obj objectForKey:@"id"] intValue] == 5){
                NSString *str_destination;
                BOOL isCompoundInstuction = (nil != [dict_obj objectForKey:@"text"]);
             
                if (isCompoundInstuction){
                    str_destination = [dict_obj objectForKey:@"text"];
                }else{
                    if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
                        str_destination = @"Arrive at desination";
                    }else{
                        str_destination = @"בצע את הנתיב המודגש";
                    }
                }
                return [self getInstruction:str_destination andImage:@"type_5"];
          
         }else if ([[dict_obj objectForKey:@"id"] intValue] == 12){
             NSString *str_instruction = @"";
             
             BOOL isCompoundInstuction = (nil != [dict_obj objectForKey:@"text"]);
                         
             if (isCompoundInstuction){
                str_instruction = [dict_obj objectForKey:@"text"];
             }else{
                 if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
                     str_instruction = @"Walk";
                 }else{
                     str_instruction = @"";
                 }
             }
             
             BOOL isOutdoorInstuctionDistance = (nil != [dict_obj objectForKey:@"simplifiedInstruction"]);
             
             if (isOutdoorInstuctionDistance){
                 if (self.from_poi == nil || [self.from_poi.title  isEqualToString:@"My Current Position"]){
                     return [self getInstruction:str_instruction andImage:[dict_obj objectForKey:@"image_name"]];
                 }else{
                     int numdistanceft = (int)[[dict_obj objectForKey:@"simplifiedInstruction"] doubleValue] * 3.28;
                     NSString *str_textDistance = [NSString stringWithFormat:@"%@ %d %@",str_instruction,numdistanceft,str_feet];
                     return [self getInstruction:str_textDistance andImage:[dict_obj objectForKey:@"image_name"]];
                 }
              }else{
                 int numdistanceft = (int)[[dict_obj objectForKey:@"distance"] doubleValue] * 3.28;
                 NSString *str_textDistance = [NSString stringWithFormat:@"%@ %d %@",str_instruction,numdistanceft,str_feet];
                 return [self getInstruction:str_textDistance andImage:@"type_1"];
             }
         }else{
             
             if (self.from_poi == nil || [self.from_poi.title  isEqualToString:@"My Current Position"]){
                return [self getInstruction:[dict_obj objectForKey:@"text"] andImage:[dict_obj objectForKey:@"image_name"]];
             }else{
                 if ([[dict_obj objectForKey:@"id"] intValue] == 1){
                     int numdistanceft = (int)[[dict_obj objectForKey:@"distance"] doubleValue] * 3.28;
                     NSString *str_textDistance = [NSString stringWithFormat:@"%@ %d %@",[dict_obj objectForKey:@"text"],numdistanceft,str_feet];
                     return [self getInstruction:str_textDistance andImage:[dict_obj objectForKey:@"image_name"]];
                 }else{
                     return [self getInstruction:[dict_obj objectForKey:@"text"] andImage:[dict_obj objectForKey:@"image_name"]];
                 }
             }
         
         }
         
        // return [self getInstruction:[dict_obj objectForKey:@"text"] andImage:@"type_1"];
     }
            
     return [[NSMutableDictionary alloc] init];
}


// Get the instruction dict for setting image and text..
- (NSMutableDictionary *)getInstruction:(NSString *)str_text andImage:(NSString *)img_icon{
    
        NSMutableDictionary *dict_instruct = [[NSMutableDictionary alloc] init];
    
        if (isFromParking == true){
            
            [dict_instruct  setValue:@"true" forKey:@"isFromParking"];
            
            total_distance = [self.mapVC getTotalDistanceOfNavigationRoute] ;
            [dict_instruct  setValue:[self getFromDistance:total_distance] forKey:@"distance"];
            
            //[dict_instruct  setValue:[self getFromDistance:[[self.cuttentInstruction objectForKey:@"distance"] doubleValue]] forKey:@"distance"];
            
            if ([str_text isEqualToString:@"Follow the line"]){
                
                NSString *str_followLine = @"";
                               
                if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
                      str_followLine = @"Follow the line";
                }else{
                     str_followLine = @"עקוב אחר הקו";
                }
                
                if ([self.cuttentInstruction objectForKey:@"exit_poi_title"]) {
                     [dict_instruct  setValue:[NSString stringWithFormat:@"%@ %@", str_followLine ,[self.cuttentInstruction objectForKey:@"exit_poi_title"]] forKey:@"text"];
                }else{
                     [dict_instruct  setValue:[NSString stringWithFormat:@"%@ %@",str_followLine, [self.cuttentInstruction objectForKey:@"poiDestinationId"]] forKey:@"text"];
                }
                
//                if ([self.cuttentInstruction objectForKey:@"exit_poi_title"]) {
//                     [dict_instruct  setValue:[NSString stringWithFormat:@"Follow the line %@", [self.cuttentInstruction objectForKey:@"exit_poi_title"]] forKey:@"text"];
//                }else{
//                     [dict_instruct  setValue:[NSString stringWithFormat:@"Follow the line %@", [self.cuttentInstruction objectForKey:@"poiDestinationId"]] forKey:@"text"];
//                }
                
                // [dict_instruct  setValue:[NSString stringWithFormat:@"Follow the line %@", [self.cuttentInstruction objectForKey:@"poiDestinationId"]] forKey:@"text"];
            }else{
                [dict_instruct  setValue:str_text forKey:@"text"];
            }
            
            // [dict_instruct  setValue:str_text forKey:@"text"];
             NSString * imagePath = [NSString stringWithFormat:@"IndoorKit.bundle/%@/",ICONS_IMAGES_DIRECTORY];
             NSURL*url = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:
                                                       [imagePath stringByAppendingString:img_icon] ofType:@"png"]];
             UIImage *img = [UIImage imageWithContentsOfFile:url.path];
             NSData *imageData = UIImagePNGRepresentation(img);
             NSString * base64String = [imageData base64EncodedStringWithOptions:0];
             [dict_instruct setValue:base64String forKey:@"base64"];
            
             if ([str_text isEqualToString:@"You have arrived at your destination"]){
                      [dict_instruct setValue:@"false" forKey:@"color"];
             }else{
                    [dict_instruct setValue:@"true" forKey:@"color"];
             }
            
             [dict_instruct setValue:[NSNumber numberWithBool:!self.myCurrentPosition]  forKey:@"staticMsg"];
            
             //[_bridge.eventDispatcher sendAppEventWithName:@"setInstruction" body:dict_instruct];
        
        }else{
                 [dict_instruct  setValue:@"false" forKey:@"isFromParking"];
                 if (self.from_poi == nil || [self.from_poi.title  isEqualToString:@"My Current Position"]){
                   NSString* floorTitle = [self floorTitleFromLocation:self.to_poi.location];
                   NSString* facilityTitle = [self facilityTitleFromLocation:self.to_poi.location];
                   [dict_instruct  setValue:self.to_poi.title forKey:@"dname"];
                     
                   NSString *str_floor = [[IDKit getCurrentLanguage]  isEqualToString: @"he"] ? @"קומה" : @"Floor";
                     
                   if (facilityTitle != nil) {
                           [dict_instruct setObject:[NSString stringWithFormat:@"%@, %@ %@", facilityTitle ,str_floor ,floorTitle] forKey:@"dinfo"];
                   }
                    else {
                           [dict_instruct setObject:[NSString stringWithFormat:@"%@ ", facilityTitle] forKey:@"dinfo"];
                   }
                     
                     
                     total_distance = [self.mapVC getTotalDistanceOfNavigationRoute] ;
                     [dict_instruct  setValue:[self getFromDistance:total_distance] forKey:@"distance"];
                     
                   //[dict_instruct  setValue:[self getFromDistance:total_distance] forKey:@"distance"];
                     
                     
                     if ([str_text isEqualToString:@"Follow the line"]){
                         
                         NSString *str_followLine = @"";
                                                 
                         if ([[IDKit getCurrentLanguage] isEqualToString:@"en"]){
                               str_followLine = @"Follow the line";
                         }else{
                               str_followLine = @"עקוב אחר הקו";
                         }
                         
                         if ([self.cuttentInstruction objectForKey:@"exit_poi_title"]) {
                              [dict_instruct  setValue:[NSString stringWithFormat:@"%@ %@",str_followLine, [self.cuttentInstruction objectForKey:@"exit_poi_title"]] forKey:@"text"];
                         }else{
                              [dict_instruct  setValue:[NSString stringWithFormat:@"%@ %@",str_followLine, [self.cuttentInstruction objectForKey:@"poiDestinationId"]] forKey:@"text"];
                         }
                         
                     }else{
                         [dict_instruct  setValue:str_text forKey:@"text"];
                     }
                     
                     NSString * imagePath = [NSString stringWithFormat:@"IndoorKit.bundle/%@/",ICONS_IMAGES_DIRECTORY];
                     NSURL*url = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:
                                                               [imagePath stringByAppendingString:img_icon] ofType:@"png"]];
                     UIImage *img = [UIImage imageWithContentsOfFile:url.path];
                     NSData *imageData = UIImagePNGRepresentation(img);
                     NSString * base64String = [imageData base64EncodedStringWithOptions:0];
                     [dict_instruct setValue:base64String forKey:@"base64"];
                     
                     if ([str_text isEqualToString:@"You have arrived at your destination"]){
                           [dict_instruct setValue:@"false" forKey:@"color"];
                      }else{
                            [dict_instruct setValue:@"true" forKey:@"color"];
                     }
                     
                     [dict_instruct setValue:[NSNumber numberWithBool:!self.myCurrentPosition]  forKey:@"staticMsg"];
                     
                     
//                   if ([[self.cuttentInstruction objectForKey:@"id"] intValue] == 11){
//                       [dict_instruct  setValue:[NSString stringWithFormat:@"Follow the line %@", [self.cuttentInstruction objectForKey:@"exit_poi_title"]] forKey:@"text"];
//                       NSString * imagePath = [NSString stringWithFormat:@"IndoorKit.bundle/%@/",ICONS_IMAGES_DIRECTORY];
//                       NSURL*url = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:
//                                                                 [imagePath stringByAppendingString:@"continue_to_path"] ofType:@"png"]];
//                       UIImage *img = [UIImage imageWithContentsOfFile:url.path];
//                       NSData *imageData = UIImagePNGRepresentation(img);
//                       NSString * base64String = [imageData base64EncodedStringWithOptions:0];
//                       [dict_instruct setValue:base64String forKey:@"base64"];
//                      //  [ary_instruction addObject:[self getInstruction:@"Follow the line" andImage:@"Instruction/continue_to_path"]];
//                   }
                     
                     
             }else {
                 
                    // let numberFT = Double(truncating: (mapVC?.getTotalDistanceOfNavigationRoute()) ?? 0)
                 
                      total_distance = [self.mapVC getTotalDistanceOfNavigationRoute] ;
                      [dict_instruct  setValue:[self getFromDistance:total_distance] forKey:@"distance"];
                      [dict_instruct  setValue:str_text forKey:@"text"];
                      NSString * imagePath = [NSString stringWithFormat:@"IndoorKit.bundle/%@/",ICONS_IMAGES_DIRECTORY];
                      NSURL*url = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:
                                                                [imagePath stringByAppendingString:img_icon] ofType:@"png"]];
                      UIImage *img = [UIImage imageWithContentsOfFile:url.path];
                      NSData *imageData = UIImagePNGRepresentation(img);
                      NSString * base64String = [imageData base64EncodedStringWithOptions:0];
                      [dict_instruct setValue:base64String forKey:@"base64"];
                  
                      if ([str_text isEqualToString:@"You have arrived at your destination"]){
                          [dict_instruct setValue:@"false" forKey:@"color"];
                      }else{
                          [dict_instruct setValue:@"true" forKey:@"color"];
                      }
                 
                      [dict_instruct setValue:[NSNumber numberWithBool:!self.myCurrentPosition] forKey:@"staticMsg"];
                      [_bridge.eventDispatcher sendAppEventWithName:@"setInstruction"
                                                                         body:dict_instruct];
             }
        }
    
    return dict_instruct;
}


#pragma mark - Private POI Details Methods

//Get the location of requested POI's
- (NSString*)getLocationStringForPoi:(IDPoi*)aPoi {
    IDLocation *location = aPoi.location;
    NSString *floorTitle = [IDKit getInfoForFloorID:location.floorId
                                   inFacilityWithID:location.facilityId
                                      atCmpusWithID:location.campusId][@"title"];
    if ([floorTitle hasPrefix:@"L"] && [floorTitle length] > 1) {
        floorTitle = [floorTitle substringFromIndex:1];
    }
    NSString *locationDesc;
    
    NSDictionary *campusDict = [IDKit getInfoForCampusWithID:location.campusId];
    NSDictionary *facilityDict = [IDKit getInfoForFacilityWithID:location.facilityId atCmpusWithID:location.campusId];
    locationDesc = [NSString stringWithFormat:@"%@ %@%@", campusDict[kTitle], facilityDict[kTitle], (aPoi.location.isExternal != true  ? [NSString stringWithFormat:@", Floor %@", floorTitle] : @"")];
    
    if ([locationDesc containsString:@"ach hospital"]) {
        locationDesc = [locationDesc stringByReplacingOccurrencesOfString:@"ach hospital" withString:@"American Medical Center"];
    } else if ([locationDesc containsString:@"ach sturgis2"]) {
        locationDesc = [locationDesc stringByReplacingOccurrencesOfString:@"ach sturgis2" withString:@"American Medical Center, Sturgis"];
    }
    return locationDesc;
}

// Get the all POI's Details to show in POI details screen
- (void)showPoiDetails:(IDPoi *)poi{

    NSString* floorTitle = [self floorTitleFromLocation:poi.location];
    NSString* facilityTitle = [self facilityTitleFromLocation:poi.location];
      
    NSMutableDictionary *dict_poiDetails = [[NSMutableDictionary alloc] init];
    
    //if (poi == self.parkingPoi)
   // {
      //  [dict_poiDetails setValue:self.currentPoi.identifier forKey:@"poi"];
    //}else{
        [dict_poiDetails setValue:_parkingPoi.title forKey:@"closestParkingName"];
        [dict_poiDetails setValue:self.parkingPoi.identifier forKey:@"closestParkingPoi"];
    //}
    
    [dict_poiDetails setValue:poi.identifier forKey:@"poi"];
    [dict_poiDetails  setValue:poi.title forKey:@"poiNameTv"];
    
    
    [dict_poiDetails setValue: nil != poi.info[@"visit"]? poi.info[@"visit"]:@
    "" forKey:@"hoursTv"];

    [dict_poiDetails  setValue:poi.description != nil? poi.description:@
    "" forKey:@"description"];
    
    
    [dict_poiDetails  setValue:poi.title forKey:@"title"];
    [dict_poiDetails setObject:[NSString stringWithFormat:@"%ld", (long)poi.location.floorId] forKey:@"floorId"];

    NSString *str_floor = [[IDKit getCurrentLanguage]  isEqualToString: @"he"] ? @"קומה" : @"Floor";
    
    if (facilityTitle != nil) {
            [dict_poiDetails setObject:[NSString stringWithFormat:@"%@, %@ %@", facilityTitle, str_floor ,floorTitle] forKey:@"poiInfo"];
            [dict_poiDetails setObject:[NSString stringWithFormat:@"%@, %@ %@", facilityTitle, str_floor ,floorTitle] forKey:@"address"];
    }
     else {
            [dict_poiDetails setObject:[NSString stringWithFormat:@"%@ ", facilityTitle] forKey:@"poiInfo"];
            [dict_poiDetails setObject:[NSString stringWithFormat:@"%@ ", facilityTitle] forKey:@"address"];
    }
    
    [dict_poiDetails setValue:poi.categoriesWithFilters[0].name forKey:@"category"];
//    [dict_poiDetails setValue:[self getLocationStringForPoi:poi] forKey:@"hoursTv"];
    [dict_poiDetails setValue:poi.info[kUrl] forKey:@"poiUrl"];
    [dict_poiDetails setValue:poi.info[kEmail]  forKey:@"emails"];
    [dict_poiDetails setValue:[poi.info[kNurse] componentsSeparatedByString:@","] forKey:@"phone1"];
    [dict_poiDetails setValue:[poi.info[kStation] componentsSeparatedByString:@","] forKey:@"phone2"];
    [dict_poiDetails setValue:poi.info[kKeywords]   forKey:@"keywordtext"];
    //[dict_poiDetails setValue:parkingPoi.categories[0] forKey:@"mediaUrl"];
    [dict_poiDetails setValue:[NSString stringWithFormat:@"%ld", (long)poi.location.floorId] forKey:@"floor"];
    [dict_poiDetails setValue:[self getMediaFromPoi:poi] forKey:@"multiMedia"];    ;
    callbackParkingSetup(@[dict_poiDetails]);
    
}

// Get image and video of poi..
- (NSMutableArray *)getMediaFromPoi:(IDPoi *)aPoi {
  
    NSArray *galleryArray = _currentPoi.info[kGalleries] ;
    
    NSMutableArray *mediayArray = [[NSMutableArray alloc] init];
    
    if ([_currentPoi.info[kHead] length] > 0) {
        NSMutableArray *gallery = [[NSMutableArray alloc] init];
        [gallery addObject:_currentPoi.info[kHead] ];
        [gallery addObjectsFromArray:galleryArray];
        galleryArray = [gallery copy];
    }
    
   BOOL isExistVideo = ((NSNumber*)_currentPoi.info[@"isMedia"]).boolValue;
   BOOL  isExistPhotos = galleryArray.count  || [_currentPoi.info[kHead] length];
   
   // _carousel.scrollEnabled = _galleryArray.count > 1 || (isExistPhotos && isExistVideo);
    NSString *imageUrl;
    
    if (isExistPhotos) {
        NSString *projectId = [IDKit getProjectId];
        imageUrl = [NSString stringWithFormat:@"%@res/%@/%@/%@/",
                    BASE_URL,
                    projectId,
                    _currentPoi.location.campusId,
                    _currentPoi.location.facilityId];
    }
    
    for (int i=0; i<galleryArray.count; i++) {
        [mediayArray addObject:[NSString stringWithFormat:@"%@%@",imageUrl,galleryArray[i]]];
    }
    
    
    if (isExistVideo) {
        [mediayArray addObject:_currentPoi.info[@"multiMedia"]];
    }
    
    if (!isExistVideo && !isExistPhotos) {
        //NSDictionary* userInfo = [[JsonDataManager sharedManager] homeDict][@"homescreeninfos"];
       // defaultPhotoUrl = kDetailsIconUrlByAppendName(userInfo[@"facimage"]);
    }
    
    return mediayArray;
}


#pragma mark - Private POI search Method

// Need to manage code in common function
// Apply category search by selecting category.
- (void)poiSearchFromPoiDetails:(IDPoi *)aPoi  isFrom:(BOOL)isfrom{
    
   // IDPoi *aPoi = [self.allFilterPoisArray objectAtIndex:[selectedpoi intValue]];
    
    if (aPoi == nil) {
        return;
    }
    
   // dispatch_async (dispatch_get_main_queue(), ^{
       //[IDKit stopNavigation];
    //});
    

    if (isfrom) {
        _from_poi = aPoi;
        if([aPoi.title isEqualToString:@"My Current Position"] || ([aPoi.title isEqualToString:@""] || (aPoi == nil))){
              _myCurrentPosition = true;
        }else{
            _myCurrentPosition = false;
        }
        dispatch_async (dispatch_get_main_queue(), ^{
            [self.mapVC presentPoiOnMapWithPoi:self.from_poi];
        });
    } else {
        _to_poi = aPoi;
        
        dispatch_async (dispatch_get_main_queue(), ^{
            [self.mapVC presentPoiOnMapWithPoi:self.to_poi];
        });
        
        _isNeedShowAlertChooseMap = YES;
    }
    
    [self setDefaultLocationInCampusWithFromPoi:_from_poi];

    dispatch_async (dispatch_get_main_queue(), ^{
        [self.mapVC presentPoiOnMapWithPoi:aPoi];
    });
}



// Apply category search by selecting category.
- (void)poiSearch:(NSString *)selectedpoi  isFrom:(BOOL)isfrom{
    
    IDPoi *aPoi = [self.allFilterPoisArray objectAtIndex:[selectedpoi intValue]];
    
    if (isSelectedSubcategory && _poisSubCategoriesArray.count > 0){
        aPoi = [self.poisSubCategoriesArray objectAtIndex:[selectedpoi intValue]];
    }
    
    if (aPoi == nil) {
        return;
    }
    
    dispatch_async (dispatch_get_main_queue(), ^{
       [IDKit stopNavigation];
    });
    
    if (isfrom) {
        _from_poi = aPoi;
        if([aPoi.title isEqualToString:@"My Current Position"] || ([aPoi.title isEqualToString:@""] || (aPoi == nil))){
              _myCurrentPosition = true;
        }else{
            _myCurrentPosition = false;
        }
        dispatch_async (dispatch_get_main_queue(), ^{
            [self.mapVC presentPoiOnMapWithPoi:self.from_poi];
        });
    } else {
        _to_poi = aPoi;
        
        dispatch_async (dispatch_get_main_queue(), ^{
            [self.mapVC presentPoiOnMapWithPoi:self.to_poi];
        });
        
        _isNeedShowAlertChooseMap = YES;
    }
    
    [self setDefaultLocationInCampusWithFromPoi:_from_poi];

    dispatch_async (dispatch_get_main_queue(), ^{
        [self.mapVC presentPoiOnMapWithPoi:aPoi];
        self->isSelectedSubcategory = false;
    });
}

 

#pragma mark - Private Third Party Methods

// Check available third party apps
- (void)isAvailableAppsExistsWithCompletionHandler:(void(^)(NSMutableDictionary *exists)) completion
{
    NSMutableDictionary *availableApps = [[NSMutableDictionary alloc] init];
    dispatch_async(dispatch_get_main_queue(), ^{

        for(id key in self->_navAppsNamesWithUrl) {
              BOOL  isOpen = [[UIApplication sharedApplication] canOpenURL:
                       [NSURL URLWithString: [self.navAppsNamesWithUrl objectForKey:key]]];
              if (isOpen) {
                        NSString *url = [self.navAppsNamesWithUrl objectForKey:key];
                        [availableApps setValue:url forKey:key];
              }else {
                
              }
       }
        completion(availableApps);

        });
}

-(void)navigateUseThirdPartyAppWithTitle: (NSString *)aTitle andCoordinates:(CLLocationCoordinate2D)aCoordinates {
    
    NSString *baseUrl;
    NSString *url;
    baseUrl = [_navAppsNamesWithUrl objectForKey:aTitle];
    
    if([aTitle isEqualToString:@"Apple Maps"]) {
        url = [NSString stringWithFormat:@"%@?daddr=%f,%f", baseUrl, aCoordinates.latitude, aCoordinates.longitude];
    }

    if([aTitle isEqualToString:@"Google Maps"]) {
        url = [NSString stringWithFormat:@"%@?saddr=&daddr=(%f),(%f)", baseUrl, aCoordinates.latitude, aCoordinates.longitude];
    }
    
    if([aTitle isEqualToString:@"Navigon"]) {
        url = [NSString stringWithFormat:@"%@coordinate/Parking/%f/%f", baseUrl, aCoordinates.longitude, aCoordinates.latitude];
    }
    
    if([aTitle isEqualToString:@"Waze"]) {
        url = [NSString stringWithFormat:@"%@?ll=%f,%f&navigate=yes", baseUrl, aCoordinates.latitude, aCoordinates.longitude];
    }
    
    if([aTitle isEqualToString:@"Maps Me"]) {
        url = [NSString stringWithFormat:@"%@map?v=1&ll=%f,%f", baseUrl,aCoordinates.latitude, aCoordinates.longitude];
    }
    
    if([aTitle isEqualToString:@"inRoute"]) {
        url = [NSString stringWithFormat:@"%@coordinates?action=opt&loc=/Parking/%f/%f", baseUrl,aCoordinates.latitude, aCoordinates.longitude];
    }
    
    if([aTitle isEqualToString:@"Here Maps"]) {
        url = [NSString stringWithFormat:@"%@mylocation/%f,%f,Parking", baseUrl,aCoordinates.latitude, aCoordinates.longitude];
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
       [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url] ];
    });
}

#pragma mark - Private Parking Methods

- (void)saveParking{
    
    IDLocation* currentLocation = [IDKit getUserLocation];
    dispatch_async (dispatch_get_main_queue(), ^{
      [IDKit setCurrentLocationAsParking];
      [self.mapVC presentLocation:currentLocation];
    });
}

- (void)navigateToParking{
    [IDKit setNavigationSimplifiedInstructionStatus:YES];
    isFromParking = true;
    [[NavigationManager sharedManager] setParkingLocation:[IDKit getParkingLocation]];
    IDLocation *fromLocation =  [IDKit getUserLocation];
    [IDKit callServerRouteAPIFrom:fromLocation toLocation:[[NavigationManager sharedManager] parkingLocation]];
}
  
- (void)deleteParking{
    [IDKit removeParkingLocation];
}

        
#pragma mark - Private Stop Navigation Methods

// Method to stop engine and reinitialize the map view
- (void)closeBtnTapped {
    
    [self.mapVC setMapRotationMode:KIDMapRotationNavigation];
    
    if (self.from_poi != nil || ![self.from_poi.title  isEqualToString:@"My Current Position"]){
        if (tempFromLocation != nil){
           [IDKit setCurrentUserLocation];
        }
    }
    
    isFromParking = false;
    gpsBool = true;
    _from_poi = nil;
    _to_poi = nil;
    self.cuttentInstruction = @{};
    self.playCuttentInstruction = @{};
    [self stopNavigationTapped];
   
   // [[NavigationManager sharedManager] setDefaultUserLocationForFromLocation:tempFromLocation isPutUserInCampus:self.mapVC.putUserInCampus];
    dispatch_async (dispatch_get_main_queue(), ^{
        [self.mapVC setFixedDestinationLocation:nil];
        [IDKit setDisplayUserLocationIcon:false];
        [self.mapVC setUserMarkerDisplay:false];
        [self.mapVC placeOriginMarker:false];
    });
    
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 0.0 * NSEC_PER_SEC);
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
         self->isOnCampus = true;
         self->isOffCampus = true;
    });
}


#pragma mark - Private Start Navigation Methods

- (void)projectLocationTypeChecker{
    //IDProjectLocationType projectLocationType = [IDKit getProjectLocationType];
    if(!_from_poi){
        IDPoi *myLocationPoi = [self myLocationPoi];
         _from_poi = myLocationPoi;
         _myCurrentPosition = true;
         if (![[NavigationManager sharedManager] isGoNavigation]){
             dispatch_async (dispatch_get_main_queue(), ^{
                [[NavigationManager sharedManager] setDefaultUserLocationForFromLocation:nil isPutUserInCampus:self.mapVC.putUserInCampus];
             });
         }
         [self updateHeaderViewWithFrom:_from_poi to:_to_poi];
    }else{
        //[[NavigationManager sharedManager] setDefaultUserLocationForFromLocation:self.from_poi.location isPutUserInCampus:self.mapVC.putUserInCampus];
        _myCurrentPosition = false;
    }
    
    [self startNavigationToPoi:_to_poi];
}


- (void)startNavigationToPoi:(IDPoi *)aToPoi{
    
    isFromParking = false;
    
    gpsBool = true;
   
    if (aToPoi == nil) {
        return;
    }

    [IDKit resetFloorsInRoute];

    [[NavigationManager sharedManager] setFromLocation:_from_poi.location];
    [[NavigationManager sharedManager] setToPoi:_to_poi];
    
    if (self.from_poi == nil || [self.from_poi.title  isEqualToString:@"My Current Position"]){
       [IDKit setNavigationSimplifiedInstructionStatus:YES];
       IDLocation *fromLocation =  [IDKit getUserLocation];
       if (!fromLocation.indoor) {
           fromLocation.floorId = [self.mapVC getEntranceFloorId].integerValue;
       }
        [IDKit callServerRouteAPIFrom:fromLocation toLocation:self.to_poi.location];
    }else{
        [IDKit setNavigationSimplifiedInstructionStatus:NO];
        IDUserLocation *location = [[IDUserLocation alloc] initWithCampusId:self.from_poi.location.campusId
            facilityId:self.from_poi.location.facilityId outCoordinate:self.from_poi.location.outCoordinate
            inCoordinate:self.from_poi.location.inCoordinate floorId:self.from_poi.location.floorId segmentId:0 andAttitude:0];
        [IDKit setForceUserLocation:location];
        [IDKit callServerRouteAPIFrom:self.from_poi.location toLocation:self.to_poi.location];
    }
}

 
-(BOOL)checkUserOutCampus {
    NSDictionary *info = [IDKit getInfoForCampusWithID:kCampusId];
    CLLocation *location = nil;
    IDUserLocation* userLocation = [IDKit getUserLocation];
    if (info[@"default_location"] != nil) {
        location = info[@"default_location"];
    } else if (info[@"location"] != nil) {
        location = info[@"location"];
    }
    
    CLLocation *locA = [[CLLocation alloc] initWithLatitude:location.coordinate.latitude
                                                  longitude:location.coordinate.longitude];
    CLLocation *locB = [[CLLocation alloc] initWithLatitude:userLocation.outCoordinate.latitude
                                                  longitude:userLocation.outCoordinate.longitude];
    CLLocationDistance distance = [locA distanceFromLocation:locB];
    
    if(distance > 800 && _myCurrentPosition && userLocation.facilityId == nil ){
        return true;
    } else {
        return false;
    }
}

// single
- (void)navigateToPoi:(IDPoi *)aPoi {
   // dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(8.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
       [self startNavigateToLocation:aPoi.location];
   // });
}
 
-(void)startNavigateToLocation:(IDLocation *)aLocation {
  
    [IDKit setAutomaticReRoute:false];
    
    [self.instructionController setDestinationTitle:[aLocation poi].title];
    if (self.from_poi == nil || [self.from_poi.title  isEqualToString:@"My Current Position"]){
        dispatch_async (dispatch_get_main_queue(), ^{
        //      [self.mapVC setUserMarkerDisplay:false];
              [self.mapVC placeOriginMarker:false];
        });
        [self.mapVC setMapRotationMode:KIDMapRotationNavigation];
    }else{
        [self.mapVC setMapRotationMode:kIDMapRotationStatic];
        IDLocation *fromLocation = [NavigationManager sharedManager].fromLocation ? : [IDKit getUserLocation];
        if (!fromLocation.indoor) {
            fromLocation.floorId = [self.mapVC getEntranceFloorId].integerValue;
        }
        [self.mapVC presentLocation:self.from_poi.location];
   
        dispatch_async (dispatch_get_main_queue(), ^{
              [self.mapVC setUserMarkerDisplay:false];
              [self.mapVC placeOriginMarker:true];
        });
    }
  
    BOOL result;
    
    result = [IDKit startNavigateToLocation:aLocation
                                withOptions:kNavigationOptionStaff
                                andDelegate:self];
     self.mapVC.Route = false;

    if (result) {
                       
                IDLocation *fromLocation = [NavigationManager sharedManager].fromLocation ? [NavigationManager sharedManager].fromLocation  : [IDKit getUserLocation];
            
                 if (!fromLocation.indoor) {
                     fromLocation.floorId = [self.mapVC getEntranceFloorId].integerValue;
                 }
                 
                 NSMutableArray *floorAryCheck = [[NSMutableArray alloc] init];
                              
                 long fromlocation = [self.floorsIndexes indexOfObject:[NSString stringWithFormat:@"%ld",(long)fromLocation.floorId]];
                 long tolocation = [self.floorsIndexes indexOfObject:[NSString stringWithFormat:@"%ld",(long)aLocation.floorId]];
                 NSString *str_fromlocation =  [self.pickerViewDataArray objectAtIndex:fromlocation];
                 NSString *str_tolocation =  [self.pickerViewDataArray objectAtIndex:tolocation];
                 _navigateFloorsList = [[NSMutableArray alloc] init];
                 NSMutableDictionary *dict_fromFloor = [[NSMutableDictionary alloc] init];
                 [dict_fromFloor setValue:[NSString stringWithFormat:@"%ld",(long)fromLocation.floorId] forKey:@"floorId"];
                 [dict_fromFloor setValue:str_fromlocation forKey:@"floorText"];
                 [_navigateFloorsList addObject:dict_fromFloor];
                 [floorAryCheck addObject:[NSString stringWithFormat:@"%ld",(long)fromLocation.floorId]];
                        
                 
                 if (str_tolocation != str_fromlocation){
                    NSMutableDictionary *dict_toFloor = [[NSMutableDictionary alloc] init];
                    [dict_toFloor setValue:[NSString stringWithFormat:@"%ld",(long)aLocation.floorId] forKey:@"floorId"];
                    [dict_toFloor setValue:str_tolocation forKey:@"floorText"];
                    [_navigateFloorsList addObject:dict_toFloor];
                    [floorAryCheck addObject:[NSString stringWithFormat:@"%ld",(long)aLocation.floorId]];
                 }
                  
                 NSMutableArray *routeArr = [[NSMutableArray alloc] init];
                 routeArr = [[IDKit getFloorsInRoute] mutableCopy];

                 for (int i=0; i< routeArr.count; i++) {
                     if (![[NSString stringWithFormat:@"%ld",(long)fromLocation.floorId] isEqualToString:routeArr[i]] && ![[NSString stringWithFormat:@"%ld",(long)aLocation.floorId] isEqualToString:routeArr[i]] && ![floorAryCheck containsObject:routeArr[i]]){
                         long extralocation = [self.floorsIndexes indexOfObject:routeArr[i]];
                         NSString *str_extraLocation =  [self.pickerViewDataArray objectAtIndex:extralocation];
                         NSMutableDictionary *dict_toFloor = [[NSMutableDictionary alloc] init];
                         [dict_toFloor setValue:routeArr[i] forKey:@"floorId"];
                         [dict_toFloor setValue:str_extraLocation forKey:@"floorText"];
                         [_navigateFloorsList addObject:dict_toFloor];
                         [floorAryCheck addObject:routeArr[i]];
                     }
                 }
           
                 [self.bridge.eventDispatcher sendAppEventWithName:@"updateFloor"
                 body:_navigateFloorsList];
        
        } else {
            
            if (callbackNavigation != nil){
                callbackNavigation(@[@"NavigationFailed"]);
                callbackNavigation = nil;
            }
                   
       }
}


-(void)startSimulateToLocation:(IDLocation *)aLocation {
    IDLocation* fromLoc;
    
    fromLoc = [IDKit getUserLocation];
    
    if (fromLoc.campusId == nil) {
        fromLoc.campusId = aLocation.campusId;
    }
    if (fromLoc.isExternal) {
        fromLoc.inCoordinate = CGPointMake(0, 0);
        fromLoc.rawCoordinate = fromLoc.inCoordinate;
    }
    if (nil != fromLoc) {
        [IDKit startSimulationNavigationToLocation:aLocation
                                      fromLocation: fromLoc
                                       withOptions:kNavigationOptionStaff
                                       andDelegate:self];
    }
    fromFloorIdSimulation = fromLoc.floorId;
}

#pragma mark - Private Helper Methods


-(IDPoi *)getPOIofIdentifier:(NSString *)poiIdentifer{
    IDQuery * query = [IDQuery queryWithClass:IDPoi.class];
    [query addQueryParameter:poiIdentifer whereProperty:@"identifier" withType:kQueryMATCHES];
    IDPoi * POI = [[query fetchObjects] firstObject];
    return POI;
}


- (void)showPoiOnMap:(IDPoi*)aPoi{
     dispatch_async (dispatch_get_main_queue(), ^{
            [self.mapVC setShowAllOnZoomLevel:aPoi :true];
            [self.mapVC presentPoiOnMapWithPoi:aPoi];
            [self.mapVC showBubbleForPoi:aPoi];
    });
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.mapVC showBubbleForPoi:aPoi];
    });
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(8 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
       self->isOnCampus = true;
       self->isOffCampus = true;
   });
    
}


//Get the campus ID
- (NSString*)poisPathId{
    return [NSString stringWithFormat:@"%@",kCampusId];
}

// Get the facility title of location
- (NSString*)facilityTitleFromLocation:(IDLocation*)location
{
    return [IDKit getInfoForFacilityWithID:location.facilityId atCmpusWithID:location.campusId][@"title"];
}

// Get the floor title of location
- (NSString*)floorTitleFromLocation:(IDLocation*)location
{
    return [IDKit getInfoForFloorID:location.floorId
                   inFacilityWithID:location.facilityId
                      atCmpusWithID:location.campusId][@"title"];
}

// Stop engine navigation
 -(void)stopNavigationTapped {
    dispatch_async (dispatch_get_main_queue(), ^{
       [IDKit stopNavigation];
    });
}


- (void)setDefaultLocationInCampusWithFromPoi:(IDPoi*)poi {
    _from_poi = poi;
    if (_to_poi) {
        dispatch_async (dispatch_get_main_queue(), ^{
            [[NavigationManager sharedManager] setDefaultUserLocationForFromLocation:poi.location isPutUserInCampus:[self.mapVC putUserInCampus]];
        });
    }
    [self updateHeaderViewWithFrom:_from_poi to:_to_poi];
}

- (void)updateHeaderViewWithFrom:(IDPoi*)aFromPoi to:(IDPoi*)aToPoi {
    IDProjectLocationType projectLocationType = [IDKit getProjectLocationType];
    if (projectLocationType == kNO_LOCATION) {
    } else {
        if([aFromPoi.title isEqualToString:@"My Current Position"] || ([aToPoi.title isEqualToString:@""] || (aToPoi == nil))){
            
            if (self.isIndoorLocation){
                dispatch_async (dispatch_get_main_queue(), ^{
                    [self.mapVC presentLocation:[IDKit getUserLocation]];
                    [self.mapVC showMyPosition];
               });
            }else{
                 dispatch_async (dispatch_get_main_queue(), ^{
                      [self.mapVC showMyPosition];
                });
            }
        }
    }
    if (aFromPoi) {
        dispatch_async (dispatch_get_main_queue(), ^{
            [self.mapVC presentPoiOnMapWithPoi:aFromPoi];
        });
        
    } else if (aToPoi) {
        dispatch_async (dispatch_get_main_queue(), ^{
             [self.mapVC presentPoiOnMapWithPoi:aToPoi];
        });
    }
    
    if( (aToPoi.title != nil)  && [self checkUserOutCampus] && _isNeedShowAlertChooseMap) {
        _isNeedShowAlertChooseMap = NO;
    }
}

- (void)updateFloorsPickerData
{
     // [self.bridge.eventDispatcher sendAppEventWithName:@"OnFloorChange"
       //                                            body:@{@"floorId": [NSString stringWithFormat:@"%ld",(long)self.mapVC.currentPresentedFloorID]}];
}

- (IDPoi *)myLocationPoi {
    NSDictionary* info  = [IDKit getInfoForCampusWithID:kCampusId];
    CLLocation *clocation = nil;
    IDLocation *myLocation;
    IDPoi *myPoiLocation;
    NSDictionary* infoDictionary = [[NSDictionary alloc]initWithObjectsAndKeys:
                                    [UIImage imageNamed :@"userIcon"], @"icon",
                                    nil];

        if(![IDKit isUserInCampus:MAX_DISTANCE_FROM_CAMPUS]  && [self.mapVC putUserInCampus]){
            //get the default location for the campus
            if (info[@"default_location"] != nil) {
                clocation = info[@"default_location"];
            } else if (info[@"location"] != nil) {
                clocation = info[@"location"];
            }
            if (clocation != nil) {
                // Example for default Location
                IDUserLocation* location = [[IDUserLocation alloc] initWithCampusId:kCampusId
                                                                         facilityId:@"hospital"
                                                                      outCoordinate:clocation.coordinate
                                                                       inCoordinate:CGPointZero
                                                                         andFloorId:0];
                myLocation = location;
            }
            NSDictionary* infoDictionary = [[NSDictionary alloc]initWithObjectsAndKeys:
                                            [UIImage imageNamed :@"userIcon"],@"icon",
                                            nil];
            myPoiLocation = [[IDPoi alloc]initPoiWithTitle:@"My Current Position"
                                                  subtitle:nil
                                               description:nil
                                                identifier:nil
                                                categories:nil
                                                  location:myLocation
                                                   andInfo:infoDictionary];
            
        } else{
            IDUserLocation* location = [IDKit getUserLocation];
            myLocation = location;
            myPoiLocation = [[IDPoi alloc]initPoiWithTitle:@"My Current Position"
                                                  subtitle:nil
                                               description:nil
                                                identifier:nil
                                                categories:nil
                                                  location:nil
                                                   andInfo:infoDictionary];
        }
    return myPoiLocation;
}


#pragma mark IDDualMapViewControllerDelegate

- (UIImage *)mapIconForUserAnnotaion
{
    
    NSString * imagePath = [NSString stringWithFormat:@"IndoorKit.bundle/%@/",ICONS_MAP_DIRECTORY];
       NSURL*url = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:
                                                 [imagePath stringByAppendingString:@"userIcon"] ofType:@"png"]];
    UIImage *img = [UIImage imageWithContentsOfFile:url.path];
    
    return img;
}

- (UIImage*)mapIconForPoi:(IDPoi *)aPoi
{
    return aPoi.info[kIcon];
}

- (UIImage *)mapIconForParkingAnnotaion
{
    return nil;
}

- (UIColor *)mapColorForRoute
{
    return [UIColor colorWithRed:27.0/255.0 green:66.0/255.0 blue:151.0/255.0 alpha:1.0];
}

- (void)mapDidTapPOI:(IDPoi *)aPoi
{
    [self.mapVC setMapBubbleMode:YES forPoi:aPoi];
    //[self.mapVC showBubbleForPoi:aPoi];
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.mapVC setMapBubbleMode:NO forPoi:aPoi];
        //[self.mapVC hideBubbleForPoi:aPoi];
    });
}

- (void)mapDidTapCalloutOfPOI:(IDPoi *)aPoi
{
    _parkingPoi = [IDKit getNearbyParkingForPoi:aPoi];
     //NSArray *allParking = [IDKit getPOIsWithCategories:@[@"Parking"] atPathID:aPoi.location.campusId];
    _currentPoi = aPoi;
    [self.bridge.eventDispatcher sendAppEventWithName:@"setPoiDetailView"
                                                       body:@{@"poi":aPoi.identifier}];
}

- (void)mapDidTapMyLocationButton
{
    
     //[self checkBlueDotLocation];
    
     [self.mapVC showMyPosition];
}

- (void)mapDidTapUserAnnotaionIcon:(UIImage *)anAnnotationView
{
}

- (void)mapDidTapParkingAnnotaionIcon:(UIImage *)anAnnotationView
{
}

- (void)mapDidTapLabelAnnotaionIcon:(UIImage *)aLabelIcon forLabel:(IDLabel*)aLabel
{
}

- (void)mapDidTapAtCoordinate:(CLLocationCoordinate2D)coordinate
                   facilityId:(NSString *)facilityId
                      floorId:(NSString *)floorId
{
    
}

- (void)mapDidLongPressAtCoordinate:(CLLocationCoordinate2D)coordinate
                         facilityId:(NSString *)facilityId
                            floorId:(NSString *)floorId
{
    
}

-(void) mapTapMultiPoiIcon:(IDPoi *)poi {
    
}

- (void)mapDidChangedFocusToLocationWithID:(NSString *)aLocationId
{
    [self updateFloorsPickerData];
}

- (void)mapDidChangeFloorId:(NSInteger)aFloorId atFacilityId:(NSString *)aFacilityId
{
    if (self.pickerViewDataArray != nil){
        
        if (selectedFloor != aFloorId){
            [self.bridge.eventDispatcher sendAppEventWithName:@"OnFloorChange"
            body:@{@"floorId": [NSString stringWithFormat:@"%ld",(long)aFloorId]}];
        }
        selectedFloor = aFloorId;
    }
}

- (void)mapDidChangeZoomLevel:(CGFloat)aZoomLevel {
}

- (void)mapFollowUser:(BOOL)mode {
}

#pragma mark -
#pragma mark - IDNavigationDelegate

- (void)navigationUpdateWithStatus:(IDNavigationStatus)aStatus {
    NSString *str_status;
    
    ////NSLog(@"------Navigation state %ld",(long)aStatus);
    
    switch (aStatus) {
        case kNavigationStart: {
            str_status = @"STARTED";
        }
            break;
        case kNavigationNavigate: {
            if (self.from_poi == nil || [self.from_poi.title  isEqualToString:@"My Current Position"]){
                str_status = @"STARTED";
            }else{
                str_status = @"NAVIGATE";
            }
        }
            break;
        case kNavigationEnded: {
              str_status = @"DESTINATION_REACHED";
              dispatch_async (dispatch_get_main_queue(), ^{
                  [IDKit stopNavigation];
              });
         }
            break;
            
        case kNavigationStopped: {
            str_status = @"STOPED";
            
            // [IDKit stopNavigation];
        }
            break;
            
        default:
            break;
    }
       total_distance = [self.mapVC getTotalDistanceOfNavigationRoute] ;
       [_bridge.eventDispatcher sendAppEventWithName:@"onNavigationStateChanged"
                                                 body:str_status];
}



- (void)isServerRerouteAPICallSuccess:(BOOL)aStatus{
    
        IDLocation *fromLocation = [NavigationManager sharedManager].fromLocation ? [NavigationManager sharedManager].fromLocation  : [IDKit getUserLocation];
               
        if (!fromLocation.indoor) {
            fromLocation.floorId = [self.mapVC getEntranceFloorId].integerValue;
        }
        
        NSMutableArray *floorAryCheck = [[NSMutableArray alloc] init];
                     
        long fromlocation = [self.floorsIndexes indexOfObject:[NSString stringWithFormat:@"%ld",(long)fromLocation.floorId]];
        long tolocation = [self.floorsIndexes indexOfObject:[NSString stringWithFormat:@"%ld",(long)self.to_poi.location.floorId]];
        NSString *str_fromlocation =  [self.pickerViewDataArray objectAtIndex:fromlocation];
        NSString *str_tolocation =  [self.pickerViewDataArray objectAtIndex:tolocation];
        _navigateFloorsList = [[NSMutableArray alloc] init];
        NSMutableDictionary *dict_fromFloor = [[NSMutableDictionary alloc] init];
        [dict_fromFloor setValue:[NSString stringWithFormat:@"%ld",(long)fromLocation.floorId] forKey:@"floorId"];
        [dict_fromFloor setValue:str_fromlocation forKey:@"floorText"];
        [_navigateFloorsList addObject:dict_fromFloor];
        [floorAryCheck addObject:[NSString stringWithFormat:@"%ld",(long)fromLocation.floorId]];
              
        
        if (str_tolocation != str_fromlocation){
           NSMutableDictionary *dict_toFloor = [[NSMutableDictionary alloc] init];
           [dict_toFloor setValue:[NSString stringWithFormat:@"%ld",(long)self.to_poi.location.floorId] forKey:@"floorId"];
           [dict_toFloor setValue:str_tolocation forKey:@"floorText"];
           [_navigateFloorsList addObject:dict_toFloor];
           [floorAryCheck addObject:[NSString stringWithFormat:@"%ld",(long)self.to_poi.location.floorId]];
        }
         
        
        NSMutableArray *routeArr = [[NSMutableArray alloc] init];
        routeArr = [[IDKit getFloorsInRoute] mutableCopy];

        for (int i=0; i< routeArr.count; i++) {
            if (![[NSString stringWithFormat:@"%ld",(long)fromLocation.floorId] isEqualToString:routeArr[i]] && ![[NSString stringWithFormat:@"%ld",(long)self.to_poi.location.floorId] isEqualToString:routeArr[i]] && ![floorAryCheck containsObject:routeArr[i]]){
                long extralocation = [self.floorsIndexes indexOfObject:routeArr[i]];
                NSString *str_extraLocation =  [self.pickerViewDataArray objectAtIndex:extralocation];
                NSMutableDictionary *dict_toFloor = [[NSMutableDictionary alloc] init];
                [dict_toFloor setValue:routeArr[i] forKey:@"floorId"];
                [dict_toFloor setValue:str_extraLocation forKey:@"floorText"];
                [_navigateFloorsList addObject:dict_toFloor];
                [floorAryCheck addObject:routeArr[i]];
            }
        }
  
        [self.bridge.eventDispatcher sendAppEventWithName:@"updateFloor"
        body:_navigateFloorsList];
    
}


- (void)isServerAPICallSuccess:(BOOL)aStatus  typeError:(NSInteger)type{
    
    //[self.mapVC setMapBubblesModeForNavigation:false];
    
     if (aStatus){
         if(isFromParking){
             dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC);
                        dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                  [[NavigationManager sharedManager] parkingLocation].campusId =kCampusId;
                  [self startNavigateToLocation:[[NavigationManager sharedManager] parkingLocation]];
             });
         }else{
             dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC);
                dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                    [self navigateToPoi:self.to_poi];
             });
         }
       }else{
           
           if (callbackNavigation != nil){
               if(type == 1){
                   callbackNavigation(@[@"NetworkFailed"]);
               }else{
                   callbackNavigation(@[@"NavigationFailed"]);
               }
               callbackNavigation = nil;
           }
           
           
       }
}


- (void)updateWithInstruction:(NSDictionary *)anInstruction andStatus:(IDNavigationStatus)aStatus {
    
    NSMutableDictionary* dic = [NSMutableDictionary dictionaryWithDictionary:anInstruction];
    BOOL isCompoundInstuction = (nil != anInstruction[@"compound"]);

    if (isCompoundInstuction){
        anInstruction = anInstruction[@"compound"];
    }
    
    int numdistance = (int)[self.mapVC getTotalDistanceOfNavigationRoute] * 3.28;
    
    if (numdistance <= 20){
        [self playInstructionSound:anInstruction];
        //if (self.from_poi == nil || [self.from_poi.title  isEqualToString:@"My Current Position"]){
           // if (callbackNavigation != nil){
               // callbackNavigation(@[@"NavigationEnded"]);
                //callbackNavigation = nil;
           // }
       //}
    }else
        
        if ([[anInstruction objectForKey:@"id"] intValue] == 5){
         [self playInstructionSound:anInstruction];
         if (self.from_poi == nil || [self.from_poi.title  isEqualToString:@"My Current Position"]){
             if (callbackNavigation != nil){
                 callbackNavigation(@[@"NavigationEnded"]);
                 //callbackNavigation = nil;
             }
        }
    }else{
         if (self.cuttentInstruction.count == 0){
              [self playInstructionSound:anInstruction];
          }else if ([[self.cuttentInstruction objectForKey:@"id"] intValue] != [[anInstruction objectForKey:@"id"] intValue]){
              [self playInstructionSound:anInstruction];
          }
    }
}


-(void)playInstructionSound:(NSDictionary *)anInstruction{
      self.cuttentInstruction = anInstruction;
       if (self.from_poi == nil || [self.from_poi.title  isEqualToString:@"My Current Position"]){
             NSMutableDictionary *dict_instruct =   [self setDictionary:self.cuttentInstruction];
             [_bridge.eventDispatcher sendAppEventWithName:@"setInstruction"
                                                                        body:dict_instruct];
           
             if ([[anInstruction objectForKey:@"id"] intValue] == 5 && !isDisableSound){
                 [self.instructionController playSoundForInstruction:anInstruction];
             }
       }else{
           if (tempFromLocation.isIndoor && tempFromLocation != nil){
           }
       }
}


-(void)playSoundForInstruction:(NSDictionary *)anInstruction{
    
       BOOL isCompoundInstuction = (nil != anInstruction[@"compound"]);
       if (isCompoundInstuction){
           anInstruction = anInstruction[@"compound"];
       }
       if ([[anInstruction objectForKey:@"id"] intValue] == 5){
            //[self playSound:anInstruction];
       }else{
            if (self.playCuttentInstruction.count == 0){
                [self playSound:anInstruction];
            }else if ([[self.playCuttentInstruction objectForKey:@"id"] intValue] != [[anInstruction objectForKey:@"id"] intValue]){
                [self playSound:anInstruction];
            }
       }
}


-(void)playSound:(NSDictionary *)anInstruction{
    _playCuttentInstruction = anInstruction;
      if ((self.from_poi == nil || [self.from_poi.title  isEqualToString:@"My Current Position"]) && !isDisableSound){
               [self.instructionController playSoundForInstruction:anInstruction];
       }else{
           if (tempFromLocation.isIndoor && tempFromLocation != nil && !isDisableSound){
              [self.instructionController playSoundForInstruction:anInstruction];
           }
       }
}

#pragma mark -
#pragma mark IDLocationListener

- (void)locationDetectionStatusChanged:(IDLocationDetectionStatus)aStatus {}

- (void)updateUserLocationWithLocation:(IDUserLocation *)aLocation {
    
    if((aLocation != nil && aLocation.isIndoor) || [self checkUserOutCampus] ){
        _isIndoorLocation = false;
        [self.bridge.eventDispatcher sendAppEventWithName:@"gpsMsg"
                                                              body:[NSNumber numberWithBool:false]];
    }else{
        _isIndoorLocation = true;
        [self.bridge.eventDispatcher sendAppEventWithName:@"gpsMsg"
                                                        body:[NSNumber numberWithBool:true]];
    }
}

- (void)locationManager:(CLLocationManager *)manager
       didUpdateHeading:(CLHeading *)newHeading
{
    
    
}

- (void)regionEventChangedForCampusId:(NSString*)aCampusId
                            withEvent:(IDRegionEventType)anEventType {}

- (void)regionEventChangedForFacilityWithID:(NSString*)aFacilityId
                                   campusId:(NSString*)aCampusId
                                  withEvent:(IDRegionEventType)anEventType {}


@end


