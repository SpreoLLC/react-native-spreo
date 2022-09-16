//
//  DownloadManager.m
//  DoubleConversion
//
//  Created by Ashish on 18/12/19.
//
 
#import "DownloadManager.h"


@implementation DownloadManager

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE()

RCTResponseSenderBlock callbackSetupDownload;

#pragma mark - React-Native Methods

//Method to set language
RCT_EXPORT_METHOD(updateLanguage:(NSString *)language callback:(RCTResponseSenderBlock)callback)
{
     if ([language isEqualToString:@"hebrew"]){
          [IDKit setCurrentLanguage:@"he"];
    }else{
          [IDKit setCurrentLanguage:@"en"];
    }
    
    callback(@[@"done"]);
}

//Method to set the spero key to download the map data..
RCT_EXPORT_METHOD(setupAPIKey:(NSString *)sperokey)
{
    //[IDKit setCurrentUserLocation];
    NSLog(@"---status update -1 %@",[IDKit getCurrentLanguage]);
    IDError *error;
    [IDKit setShowNavigationMarkers:YES];
    [IDKit setZipPackageWithoutMaps:true];
    [IDKit setExitCloseToOrigin:YES];
    [IDKit setTileCaching:YES];
    [IDKit setAutomaticReRoute:true];
    [IDKit setNoOutdoorCampus: true];
   // [IDKit isDuringSimulation];
    [IDKit setAPIKey:sperokey error:&error];
    
    
    if (error) {
       // [self.bridge.eventDispatcher sendAppEventWithName:@"DownloadStatus"
       // body:[NSString stringWithFormat: @"IDKit error! %d - %@",(int)error.code, error.domain]];
    }
    
    [IDKit checkForDataUpdatesAndInitialiseWithDelegate:self];
}


#pragma mark - IDDataUpdateDelegate

- (void)dataUpdateStatus:(IDDataUpdateStatus)status
{
    
    NSLog(@"version %@",[IDKit version]);
    
    NSLog(@"---status update %ld",(long)status);
    
    switch (status) {
        case kIDDataUpdateCheckForUpdates:
            // do something, display the user the current status
            break;
        case kIDDataUpdateCopyFiles:
            // do something, display the user the current status
            break;
        case kIDDataUpdateDataDownload:
            // do something, display the user the current status
            break;
        case kIDDataUpdateInitialising:
            // do something, display the user the current status
            break;
        case kIDDataUpdateDone:
            // do something, display the user the current status
            //NSLog(@"---status update done");
            // when done, can start user location tracking
            [self onDataUpdateDone];
            
            break;
        default:
            break;
    }
}

- (void)dataUpdateFailedWithError:(IDError *)anError
{
   [self.bridge.eventDispatcher sendAppEventWithName:@"DownloadStatus"
                 body:@"Network Connection Error\nData download operation faild, please check your network reachability\n and reopen the app"];
    
    
   // if (callbackSetupDownload != nil){
 //     callbackSetupDownload(@[@"",@"Network Connection Error\nData download operation faild, please check your network reachability\n and reopen the app"]);
       // callbackSetupDownload = nil;
    //}
}


#pragma mark -
#pragma mark private

//Callback to react native on download complete
- (void)onDataUpdateDone
{
    
//    NSURL *MyURL = [[NSBundle mainBundle]
//    URLForResource: @"iOSSpreoSDKDualBeaconsLogs" withExtension:@"txt"];

//    NSURL *MyURL = [[NSBundle mainBundle]
//       URLForResource: @"issue2" withExtension:@"txt"];
////
//    [BeaconSimulator shared].importFilePathURL = MyURL;
//
//    [BeaconSimulator shared].isSimulating = YES;
    
    
     //if (callbackSetupDownload != nil){
        //[IDKit checkForDataUpdatesAndInitialiseWithDelegate:nil];
       [IDKit startUserLocationTrack];
//       [IDKit setDisplayUserLocationIcon:true];
//
//       [IDKit setDefaultUserLocation:[[NavigationManager sharedManager] getDefaultUserLocation].outCoordinate];
//       [[IDKit getMapViewController] mapReload];
       
    
       
            //code to be executed on the main queue after delay
             [self.bridge.eventDispatcher sendAppEventWithName:@"DownloadStatus"
                                  body:@"Download Completed"];
               
       // });
    
         //callbackSetupDownload = nil;
     //}
}


@end
