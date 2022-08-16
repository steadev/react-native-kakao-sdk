package com.dooboolab.kakaologins

import android.content.ActivityNotFoundException
import com.facebook.react.bridge.*
import com.kakao.sdk.common.KakaoSdk.init
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User
import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.common.model.KakaoSdkError
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.share.model.SharingResult
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link

enum class TokenStatus {
    LOGIN_NEEDED,
    ERROR,
    SUCCEED
}

class RNKakaoLoginsModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private fun dateFormat(date: Date?): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(date)
    }

    override fun getName(): String {
        return "RNKakaoLogins"
    }

    @ReactMethod
     fun initializeKakao(promise: Promise) {
         tokenAvailability() { status: TokenStatus ->
             val map = Arguments.createMap()
             map.putString("status", status.toString())
             promise.resolve(map)
         }
     }

    @ReactMethod
    fun login(serviceTerms: ReadableArray?, promise: Promise) {
         var serviceTermsParam = if (serviceTerms === null || serviceTerms.size() === 0) null else serviceTerms;
         val serviceTermList = ArrayList<String>()
         if (serviceTermsParam != null) {
             for (param in serviceTermsParam.toArrayList()) {
                 serviceTermList.add(param.toString())
             }
         }

         if (UserApiClient.instance.isKakaoTalkLoginAvailable(reactContext)) {
             reactContext.currentActivity?.let {
                 if (serviceTermsParam != null) {
                     val serviceTermList = ArrayList<String>()
                     for (param in serviceTermsParam.toArrayList()) {
                         serviceTermList.add(param.toString())
                     }
                     UserApiClient.instance.loginWithKakaoTalk(
                         it,
                         serviceTerms = serviceTermList.toList()
                     ) { oAuthToken: OAuthToken?, error: Throwable? ->
                         handleKakaoLoginResponse(promise, oAuthToken, error)
                         return@loginWithKakaoTalk
                     }
                 } else {
                     UserApiClient.instance.loginWithKakaoTalk(
                         it
                     ) { oAuthToken: OAuthToken?, error: Throwable? ->
                         handleKakaoLoginResponse(promise, oAuthToken, error)
                         return@loginWithKakaoTalk
                     }
                 }
             }

         } else {
             if (serviceTermsParam != null) {
                 UserApiClient.instance.loginWithKakaoAccount(
                     reactContext,
                     serviceTerms = serviceTermList.toList()
                 ) { oAuthToken: OAuthToken?, error: Throwable? ->
                     handleKakaoLoginResponse(promise, oAuthToken, error)
                     return@loginWithKakaoAccount
                 }
             } else {
                 UserApiClient.instance.loginWithKakaoAccount(
                     reactContext
                 ) { oAuthToken: OAuthToken?, error: Throwable? ->
                     handleKakaoLoginResponse(promise, oAuthToken, error)
                     return@loginWithKakaoAccount
                 }
             }
         }
    }

    fun loginWithNewScopes(scopes: ReadableArray, promise: Promise) {
        var scopesParam = if (scopes === null || scopes.size() === 0) null else scopes;
        val scopeList = ArrayList<String>()
        if (scopesParam != null) {
            for (param in scopesParam.toArrayList()) {
                scopeList.add(param.toString())
            }
        }

        if (scopeList.size > 0) {
            UserApiClient.instance.loginWithNewScopes(reactContext, scopeList) { token, error ->
                reactContext.currentActivity?.let {
                    if (error != null) {
                        if (UserApiClient.instance.isKakaoTalkLoginAvailable(it)) {
                            UserApiClient.instance
                                .loginWithKakaoTalk(
                                    it
                                ) { oAuthToken: OAuthToken?, error: Throwable? ->
                                    handleKakaoLoginResponse(promise, oAuthToken, error)
                                }
                        } else {
                            UserApiClient.instance
                                .loginWithKakaoAccount(
                                    it
                                ) { oAuthToken: OAuthToken?, error: Throwable? ->
                                    handleKakaoLoginResponse(promise, oAuthToken, error)
                                }
                        }
                    } else {
                        promise.resolve("succeed")
                    }
                }
            }
        } else {
            promise.resolve("succeed")
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

    @ReactMethod
    fun sendLinkFeed(params: Map<String, String>, promise: Promise) {
        val imageLinkUrl = params["imageLinkUrl"]
        val imageUrl: String = if (params["imageUrl"] === null) "" else params["imageUrl"]!!
        val title: String = if (params["title"] === null) "" else params["title"]!!
        val description = params["description"]
        val buttonTitle: String =
            if (params["buttonTitle"] === null) "" else params["buttonTitle"]!!
        val imageWidth: Int? = params["imageWidth"]?.toInt()
        val imageHeight: Int? = params["imageHeight"]?.toInt()

        val link = Link(imageLinkUrl, imageLinkUrl, null, null)
        val content = Content(title, imageUrl, link, description, imageWidth, imageHeight)
        val buttons = ArrayList<Button>()
        buttons.add(Button(buttonTitle, link))
        val feed = FeedTemplate(content, null, null, buttons)
        if (ShareClient.instance.isKakaoTalkSharingAvailable(reactContext)) {
            reactContext.currentActivity?.let {
                ShareClient.instance
                    .shareDefault(
                        it,
                        feed
                    ) { shareResult: SharingResult?, error: Throwable? ->
                        if (error != null) {
                            promise.reject("RNKakaoLogins", "kakao link failed: $error")
                        } else if (shareResult != null) {
                            it.startActivity(shareResult.intent)
                        }
                        promise.resolve("succeed")
                    }
            }
        } else {
            reactContext.currentActivity?.let {
                // 카카오톡 미설치: 웹 공유 사용 권장
                // 웹 공유 예시 코드
                val sharerUrl = WebSharerClient.instance.makeDefaultUrl(feed)
                var shareResult = true
                // CustomTabs으로 웹 브라우저 열기

                // 1. CustomTabsServiceConnection 지원 브라우저 열기
                // ex) Chrome, 삼성 인터넷, FireFox, 웨일 등
                try {
                    KakaoCustomTabsClient.openWithDefault(it, sharerUrl)
                    promise.resolve("succeed")
                } catch(e: UnsupportedOperationException) {
                    // CustomTabsServiceConnection 지원 브라우저가 없을 때 예외처리
                    shareResult = false
                }

                // 2. CustomTabsServiceConnection 미지원 브라우저 열기
                // ex) 다음, 네이버 등
                try {
                    KakaoCustomTabsClient.open(it, sharerUrl)
                    promise.resolve("succeed")
                } catch (e: ActivityNotFoundException) {
                    // 디바이스에 설치된 인터넷 브라우저가 없을 때 예외처리
                    shareResult = false
                }
            }
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

    private fun tokenAvailability(callback: (TokenStatus) -> Unit) {
        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error != null) {
                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
                        callback(TokenStatus.LOGIN_NEEDED)
                    }
                    else {
                        callback(TokenStatus.ERROR)
                    }
                }
                else {
                    callback(TokenStatus.SUCCEED)
                }
            }
        }
        else {
            callback(TokenStatus.LOGIN_NEEDED)
        }
    }

    private fun handleKakaoLoginResponse(promise: Promise, oAuthToken: OAuthToken?, error: Throwable?) {
        if (error != null) {
            promise.reject("RNKakaoLogins", error.toString())
        } else if (oAuthToken != null) {
            val (accessToken, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt, idToken, scopes) = oAuthToken
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
        } else {
            promise.reject("RNKakaoLogins", "no_data")
        }
    }

    private fun convertValue(`val`: Boolean?): Boolean {
        return `val` ?: false
    }
}
