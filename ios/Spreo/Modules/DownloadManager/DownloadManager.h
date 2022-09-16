//
//  DownloadManager.h
//  DoubleConversion
//
//  Created by Ashish on 18/12/19.
//

#import "RCTViewManager.h"
#import <React/RCTBridgeModule.h>
#import <IndoorKit/IndoorKit.h>
#import "Global.h"
#import "NavigationManager.h"


NS_ASSUME_NONNULL_BEGIN

@interface DownloadManager : RCTViewManager<RCTBridgeModule,IDDataUpdateDelegate> 

@property (nonatomic, weak)   RCTBridge *bridge;

@end

NS_ASSUME_NONNULL_END
