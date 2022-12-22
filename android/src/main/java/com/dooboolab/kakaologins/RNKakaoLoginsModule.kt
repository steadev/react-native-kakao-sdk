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

    @ReactMethod
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
                        promise.reject("RNKakaoLogins", error.message, error)
                    } else {
                        promise.resolve("succeed")
                    }
                }
            }
        } else {
            promise.resolve("empty scopes")
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
    fun sendLinkFeed(params: ReadableMap, promise: Promise) {
        val imageLinkUrl = getString(params, "imageLinkUrl")
        val imageUrl: String = if (getString(params, "imageUrl") === null) "" else getString(params,"imageUrl")!!
        val title: String = if (getString(params,"title") === null) "" else getString(params,"title")!!
        val description = getString(params,"description")
        val buttonTitle: String =
            if (getString(params,"buttonTitle") === null) "" else getString(params,"buttonTitle")!!
        val imageWidth: Int? = getInt(params,"imageWidth")
        val imageHeight: Int? = getInt(params,"imageHeight")

        val link = Link(imageLinkUrl, imageLinkUrl, null, null)
        val content = Content(title, imageUrl = imageUrl, link = link, description = description, imageWidth=imageWidth, imageHeight=imageHeight)
        val buttons = ArrayList<Button>()
        buttons.add(Button(buttonTitle, link))
        val feed = FeedTemplate(content, null, null, buttons)
        if (ShareClient.instance.isKakaoTalkSharingAvailable(reactContext)) {
            ShareClient.instance.shareDefault(reactContext, feed) { sharingResult, error ->
                if (error != null) {
                    promise.reject("E_KAKAO_ERROR", error.message, error)
                    return@shareDefault
                } else {
                    val map = Arguments.createMap()
                    map.putBoolean("result", true)
                    map.putString("intent", sharingResult?.intent.toString())
                    sharingResult?.intent?.let { intent -> reactContext.startActivity(intent, null) }
                    map.putString("warning", sharingResult?.warningMsg.toString())
                    map.putString("argument", sharingResult?.argumentMsg.toString())
                    promise.resolve(map)
                    return@shareDefault
                }
            }
        } else {
            // 카카오톡 미설치: 웹 공유 사용 권장
            // 웹 공유 예시 코드
            val sharerUrl = WebSharerClient.instance.makeDefaultUrl(feed)

            // 1. CustomTabs으로 Chrome 브라우저 열기
            try {
                reactContext.currentActivity?.let { KakaoCustomTabsClient.openWithDefault(it, sharerUrl) }
            } catch (e: UnsupportedOperationException) {
                // 2. CustomTabs으로 디바이스 기본 브라우저 열기
                try {
                    reactContext.currentActivity?.let { KakaoCustomTabsClient.open(it, sharerUrl) }
                } catch (e: ActivityNotFoundException) {
                    // 인터넷 브라우저가 없을 때 예외처리
                    promise.reject("E_KAKAO_NO_BROWSER", e.message, e)
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

    private fun getString(dict: ReadableMap, key: String): String? {
        return if (dict.hasKey(key)) dict.getString(key) else null
    }

    private fun getInt(dict: ReadableMap, key: String): Int? {
        return if (dict.hasKey(key)) dict.getInt(key) else null
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
