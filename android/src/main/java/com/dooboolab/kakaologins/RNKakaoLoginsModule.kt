package com.dooboolab.kakaologins

import com.facebook.react.bridge.*
import com.kakao.sdk.common.KakaoSdk.init
import com.kakao.sdk.common.model.AuthError
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User
import com.kakao.sdk.auth.TokenManagerProvider
import java.text.SimpleDateFormat
import java.util.*

class RNKakaoLoginsModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private fun dateFormat(date: Date?): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(date)
    }

    override fun getName(): String {
        return "RNKakaoLogins"
    }

    @ReactMethod
    private fun login(serviceTerms: ReadableArray?, promise: Promise) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(reactContext)) {
            reactContext.currentActivity?.let {
                UserApiClient.instance.loginWithKakaoTalk(it) { token, error: Throwable? ->
                    if (error != null) {
                        if (error is AuthError && error.statusCode == 302) {
                            this.loginWithKakaoAccount(promise)
                            return@loginWithKakaoTalk
                        }
                        promise.reject("RNKakaoLogins", error.message, error)
                        return@loginWithKakaoTalk
                    }

                    if (token != null) {
                        val (accessToken, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt, idToken, scopes) = token
                        val map = Arguments.createMap()
                        map.putString("accessToken", accessToken)
                        map.putString("refreshToken", refreshToken)
                        map.putString("idToken", idToken)
                        map.putString("accessTokenExpiresAt", dateFormat(accessTokenExpiresAt))
                        map.putString("refreshTokenExpiresAt", dateFormat(refreshTokenExpiresAt))
                        val scopeArray = Arguments.createArray()
                        if (scopes != null) {
                            for (scope in scopes) {
                                scopeArray.pushString(scope)
                            }
                        }
                        map.putArray("scopes", scopeArray)
                        promise.resolve(map)
                        return@loginWithKakaoTalk
                    }

                    promise.reject("RNKakaoLogins", "Token is null")
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(reactContext) { token, error: Throwable? ->
                if (error != null) {
                    promise.reject("RNKakaoLogins", error.message, error)
                    return@loginWithKakaoAccount
                }

                if (token != null) {
                    val (accessToken, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt, idToken, scopes) = token
                    val map = Arguments.createMap()
                    map.putString("accessToken", accessToken)
                    map.putString("refreshToken", refreshToken)
                    map.putString("idToken", idToken)
                    map.putString("accessTokenExpiresAt", dateFormat(accessTokenExpiresAt))
                    map.putString("refreshTokenExpiresAt", dateFormat(refreshTokenExpiresAt))
                    val scopeArray = Arguments.createArray()
                    if (scopes != null) {
                        for (scope in scopes) {
                            scopeArray.pushString(scope)
                        }
                    }
                    map.putArray("scopes", scopeArray)
                    promise.resolve(map)
                    return@loginWithKakaoAccount
                }

                promise.reject("RNKakaoLogins", "Token is null")
            }
        }
    }

    @ReactMethod
    private fun loginWithKakaoAccount(promise: Promise) {
        UserApiClient.instance.loginWithKakaoAccount(reactContext) { token, error: Throwable? ->
            if (error != null) {
                promise.reject("RNKakaoLogins", error.message, error)
                return@loginWithKakaoAccount
            }

            if (token == null) {
                promise.reject("RNKakaoLogins", "Token is null")
                return@loginWithKakaoAccount
            }

            if (token != null) {
                val (accessToken, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt, idToken, scopes) = token
                val map = Arguments.createMap()
                map.putString("accessToken", accessToken)
                map.putString("refreshToken", refreshToken)
                map.putString("idToken", idToken)
                map.putString("accessTokenExpiresAt", dateFormat(accessTokenExpiresAt))
                map.putString("refreshTokenExpiresAt", dateFormat(refreshTokenExpiresAt))
                val scopeArray = Arguments.createArray()
                if (scopes != null) {
                    for (scope in scopes) {
                        scopeArray.pushString(scope)
                    }
                }
                map.putArray("scopes", scopeArray)
                promise.resolve(map)
                return@loginWithKakaoAccount
            }
        }
    }

    @ReactMethod
    private fun logout(promise: Promise) {
        UserApiClient.instance.logout { error: Throwable? ->
            if (error != null) {
                promise.reject("RNKakaoLogins", error.message, error)
                return@logout
            }
            promise.resolve("Successfully logged out")
            null
        }
    }

    @ReactMethod
    private fun unlink(promise: Promise) {
        UserApiClient.instance.unlink { error: Throwable? ->
            if (error != null) {
                promise.reject("RNKakaoLogins", error.message, error)
                return@unlink
            }
            promise.resolve("Successfully unlinked")
            null
        }
    }

    @ReactMethod
    private fun getAccessToken(promise: Promise) {
        val accessToken = TokenManagerProvider.instance.manager.getToken()?.accessToken

         UserApiClient.instance.accessTokenInfo { token, error: Throwable? ->
            if (error != null) {
                promise.reject("RNKakaoLogins", error.message, error)
                return@accessTokenInfo
            }

            if (token != null && accessToken != null) {
                val (expiresIn) = token
                val map = Arguments.createMap()
                map.putString("accessToken", accessToken.toString())
                map.putString("expiresIn", expiresIn.toString())
                promise.resolve(map)
                return@accessTokenInfo
            }

            promise.reject("RNKakaoLogins", "Token is null")
         }
    }

    private fun convertValue(`val`: Boolean?): Boolean {
        return `val` ?: false
    }

    @ReactMethod
    private fun getProfile(promise: Promise) {
        UserApiClient.instance.me { user: User?, error: Throwable? ->
            if (error != null) {
                promise.reject("RNKakaoLogins", error.message, error)
                return@me
            }

            if (user != null) {
                val map = Arguments.createMap()
                map.putString("id", user.id.toString())
                val kakaoUser = user.kakaoAccount
                if (kakaoUser != null) {
                    map.putString("name", kakaoUser.name.toString())
                    map.putString("email", kakaoUser!!.email.toString())
                    map.putString("nickname", kakaoUser.profile?.nickname)
                    map.putString("profileImageUrl", kakaoUser.profile?.profileImageUrl)
                    map.putString("thumbnailImageUrl", kakaoUser.profile?.thumbnailImageUrl)

                    map.putString("phoneNumber", kakaoUser.phoneNumber.toString())
                    map.putString("ageRange", kakaoUser!!.ageRange.toString())
                    map.putString("birthday", kakaoUser.birthday.toString())
                    map.putString("birthdayType", kakaoUser.birthdayType.toString())
                    map.putString("birthyear", kakaoUser.birthyear.toString())
                    map.putString("gender", kakaoUser.gender.toString())
                    map.putBoolean("isEmailValid", convertValue(kakaoUser.isEmailValid))
                    map.putBoolean("isEmailVerified", convertValue(kakaoUser.isEmailVerified))
                    map.putBoolean("isKorean", convertValue(kakaoUser.isKorean))
                    map.putBoolean("ageRangeNeedsAgreement", convertValue(kakaoUser.ageRangeNeedsAgreement))
                    map.putBoolean("birthdayNeedsAgreement", convertValue(kakaoUser.birthdayNeedsAgreement))
                    map.putBoolean("birthyearNeedsAgreement", convertValue(kakaoUser.birthyearNeedsAgreement))
                    map.putBoolean("emailNeedsAgreement", convertValue(kakaoUser.emailNeedsAgreement))
                    map.putBoolean("genderNeedsAgreement", convertValue(kakaoUser.genderNeedsAgreement))
                    map.putBoolean("isKoreanNeedsAgreement", convertValue(kakaoUser.isKoreanNeedsAgreement))
                    map.putBoolean("phoneNumberNeedsAgreement", convertValue(kakaoUser.phoneNumberNeedsAgreement))
                    map.putBoolean("profileNeedsAgreement", convertValue(kakaoUser.profileNeedsAgreement))
                }
                promise.resolve(map)
                return@me
            }

            promise.reject("RNKakaoLogins", "User is null")
        }
    }

    companion object {
        private const val TAG = "RNKakaoLoginModule"
    }

    init {
        val kakaoAppKey = reactContext.resources.getString(
                reactContext.resources.getIdentifier("kakao_app_key", "string", reactContext.packageName))
        init(reactContext, kakaoAppKey)
    }
}

// import android.util.Log
// import androidx.appcompat.app.AppCompatActivity
// import com.getcapacitor.JSArray
// import com.getcapacitor.JSObject
// import com.getcapacitor.PluginCall
// import com.kakao.sdk.user.UserApiClient
// import com.kakao.sdk.auth.model.OAuthToken
// import com.kakao.sdk.template.model.FeedTemplate
// import com.kakao.sdk.link.LinkClient
// import com.kakao.sdk.link.model.LinkResult
// import com.kakao.sdk.template.model.Button
// import com.kakao.sdk.template.model.Content
// import com.kakao.sdk.template.model.Link
// import java.util.ArrayList
// import com.google.gson.Gson
// import com.kakao.sdk.auth.AuthApiClient
// import com.kakao.sdk.common.model.KakaoSdkError
// import com.kakao.sdk.talk.TalkApiClient
// import com.kakao.sdk.talk.model.Order
// import org.json.JSONArray

// val gson = Gson()

// enum class TokenStatus {
//     LOGIN_NEEDED,
//     ERROR,
//     SUCCEED
// }


// class CapacitorKakao(var activity: AppCompatActivity) {
//     fun initializeKakao(call: PluginCall) {
//         tokenAvailability() { status: TokenStatus ->
//             val ret = JSObject()
//             ret.put("status", status.toString())
//             call.resolve(ret)
//         }
//     }

//     fun kakaoLogin(call: PluginCall) {
//         var serviceTermsParam = call.getArray("serviceTerms");
//         serviceTermsParam = if (serviceTermsParam === null || serviceTermsParam.length() === 0) null else serviceTermsParam;

//         if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
//             if (serviceTermsParam != null) {
//                 UserApiClient.instance.loginWithKakaoTalk(
//                     activity,
//                     serviceTerms = serviceTermsParam.toList<String>()
//                 ) { oAuthToken: OAuthToken?, error: Throwable? ->
//                     handleKakaoLoginResponse(call, oAuthToken, error)
//                 }    
//             } else {
//                 UserApiClient.instance.loginWithKakaoTalk(
//                     activity
//                 ) { oAuthToken: OAuthToken?, error: Throwable? ->
//                     handleKakaoLoginResponse(call, oAuthToken, error)
//                 }
//             }
//         } else {
//             if (serviceTermsParam != null) {
//                 UserApiClient.instance.loginWithKakaoAccount(
//                     activity,
//                     serviceTerms = serviceTermsParam.toList<String>()
//                 ) { oAuthToken: OAuthToken?, error: Throwable? ->
//                     handleKakaoLoginResponse(call, oAuthToken, error)
//                 }
//             } else {
//                 UserApiClient.instance.loginWithKakaoAccount(
//                     activity
//                 ) { oAuthToken: OAuthToken?, error: Throwable? ->
//                     handleKakaoLoginResponse(call, oAuthToken, error)
//                 }
//             }
//         }
//     }

//     private fun handleKakaoLoginResponse(call: PluginCall, oAuthToken: OAuthToken?, error: Throwable?) {
//         if (error != null) {
//             Log.e(TAG, "login fail : ", error)
//             call.reject(error.toString())
//         } else if (oAuthToken != null) {
//             Log.i(TAG, "login success : " + oAuthToken.accessToken)
//             val ret = JSObject()
//             ret.put("accessToken", oAuthToken.accessToken)
//             ret.put("refreshToken", oAuthToken.refreshToken)
//             call.resolve(ret)
//         } else {
//             call.reject("no_data")
//         }
//     }

//     fun sendLinkFeed(call: PluginCall) {
//         val imageLinkUrl = call.getString("imageLinkUrl")
//         val imageUrl: String = if (call.getString("imageUrl") === null) "" else call.getString("imageUrl")!!
//         val title: String = if (call.getString("title") === null) "" else call.getString("title")!!
//         val description = call.getString("description")
//         val buttonTitle: String = if (call.getString("buttonTitle") === null) "" else call.getString("buttonTitle")!!
//         val imageWidth: Int? = call.getInt("imageWidth")
//         val imageHeight: Int? = call.getInt("imageHeight")
        
//         val link = Link(imageLinkUrl, imageLinkUrl, null, null)
//         val content = Content(title, imageUrl, link, description, imageWidth, imageHeight)
//         val buttons = ArrayList<Button>()
//         buttons.add(Button(buttonTitle, link))
//         val feed = FeedTemplate(content, null, buttons)
//         LinkClient.instance
//             .defaultTemplate(
//                     activity,
//                     feed
//             ) { linkResult: LinkResult?, error: Throwable? ->
//                 if (error != null) {
//                     call.reject("kakao link failed: " + error.toString())
//                 } else if (linkResult != null) {
//                     activity.startActivity(linkResult.intent)
//                 }
//                 call.resolve()
//             }
//     }
    
//     fun loginWithNewScopes(call: PluginCall) {
//         var scopes = mutableListOf<String>()
//         val tobeAgreedScopes = call.getArray("scopes").toList<String>()
//         for (scope in tobeAgreedScopes) {
//             scopes.add(scope)
//         }
//         if (scopes.count() > 0) {
//             Log.d(TAG, "사용자에게 추가 동의를 받아야 합니다.")

//             UserApiClient.instance.loginWithNewScopes(activity, scopes) { token, error ->
//                 if (error != null) {
//                     Log.e(TAG, "사용자 추가 동의 실패", error)
//                     // call.reject("scopes agree failed")
//                     if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
//                         UserApiClient.instance
//                             .loginWithKakaoTalk(
//                                     activity
//                             ) { oAuthToken: OAuthToken?, error: Throwable? ->
//                                 if (error != null) {
//                                     Log.e(TAG, "login fail : ", error)
//                                     call.reject(error.toString())
//                                 } else if (oAuthToken != null) {
//                                     Log.i(TAG, "login success : " + oAuthToken.accessToken)
//                                     loginWithNewScopes(call)
//                                 } else {
//                                     call.reject("no_data")
//                                 }
//                             }
//                     } else {
//                         UserApiClient.instance
//                             .loginWithKakaoAccount(
//                                     activity
//                             ) { oAuthToken: OAuthToken?, error: Throwable? ->
//                                 if (error != null) {
//                                     Log.e(TAG, "login fail : ", error)
//                                     call.reject(error.toString())
//                                 } else if (oAuthToken != null) {
//                                     Log.i(TAG, "login success : " + oAuthToken.accessToken)
//                                     val ret = JSObject()
//                                     ret.put("accessToken", oAuthToken.accessToken)
//                                     ret.put("refreshToken", oAuthToken.refreshToken)
//                                     call.resolve(ret)
//                                 } else {
//                                     call.reject("no_data")
//                                 }
//                             }
//                     }
//                 } else {
//                     Log.d(TAG, "allowed scopes: ${token!!.scopes}")
//                     call.resolve()
//                 }
//             }
//         } else {
//             call.resolve()
//         }
//     }

//     fun getUserScopes(call: PluginCall) {
//         UserApiClient.instance.scopes { scopeInfo, error->
//             if (error != null) {
//                 Log.e(TAG, "동의 정보 확인 실패", error)
//                 call.reject("동의 정보 확인 실패" + error.toString())
//             }else if (scopeInfo != null) {
//                 Log.i(TAG, "동의 정보 확인 성공\n 현재 가지고 있는 동의 항목 $scopeInfo")
//                 val scopeList = JSArray()
//                 if (scopeInfo.scopes != null) {
//                     for (scope in scopeInfo.scopes!!) {
//                         scopeList.put(scope)
//                     }
//                 }
//                 val ret = JSObject()
//                 ret.put("value", scopeList)
//                 call.resolve(ret);
//             }
//         }
//     }

//     private fun tokenAvailability(callback: (TokenStatus) -> Unit) {
//         if (AuthApiClient.instance.hasToken()) {
//             UserApiClient.instance.accessTokenInfo { _, error ->
//                 if (error != null) {
//                     if (error is KakaoSdkError && error.isInvalidTokenError() == true) {
//                         callback(TokenStatus.LOGIN_NEEDED)
//                     }
//                     else {
//                         callback(TokenStatus.ERROR)
//                     }
//                 }
//                 else {
//                     callback(TokenStatus.SUCCEED)
//                 }
//             }
//         }
//         else {
//             callback(TokenStatus.LOGIN_NEEDED)
//         }
//     }

//     companion object {
//         private const val TAG = "CapacitorKakao"
//     }
// }
