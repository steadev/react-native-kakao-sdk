// RNKakaoLogins.m

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(RNKakaoLogins, NSObject)

RCT_EXTERN_METHOD(initializeKakao:(RCTPromiseResolveBlock *)resolve rejecter:(RCTPromiseRejectBlock *)reject);
RCT_EXTERN_METHOD(login:(NSArray *)serviceTerms resolver:(RCTPromiseResolveBlock *)resolve rejecter:(RCTPromiseRejectBlock *)reject);
RCT_EXTERN_METHOD(loginWithNewScopes:(NSArray *)scopes resolver:(RCTPromiseResolveBlock *)resolve rejecter:(RCTPromiseRejectBlock *)reject);
RCT_EXTERN_METHOD(logout:(RCTPromiseResolveBlock *)resolve rejecter:(RCTPromiseRejectBlock *)reject);
RCT_EXTERN_METHOD(unlink:(RCTPromiseResolveBlock *)resolve rejecter:(RCTPromiseRejectBlock *)reject);
RCT_EXTERN_METHOD(getProfile:(RCTPromiseResolveBlock *)resolve rejecter:(RCTPromiseRejectBlock *)reject);
RCT_EXTERN_METHOD(getAccessToken:(RCTPromiseResolveBlock *)resolve rejecter:(RCTPromiseRejectBlock *)reject);
RCT_EXTERN_METHOD(sendLinkFeed:(NSDictionary *)data resolver:(RCTPromiseResolveBlock *)resolve rejecter:(RCTPromiseRejectBlock *)reject);

@end
