# @steadev/react-native-kakao-sdk

---

forked from [@react-native-seoul/kakao-login@4.2.3](https://www.npmjs.com/package/@react-native-seoul/kakao-login)

Supports `Kakao Sync`, `Kakao link`, `loginWithNewScopes`(추가항목 동의받기)

Android Kakao SDK version: 2.11.0<br />
iOS Kakao SDK version: 2.9.0<br />

## Archive Error

`Undefined symbol: _OBJC_CLASS_$_RNKakaoLogins`<br />
When this error occurs, paste below codes to root project's Podfile <br />

```
pre_install do |installer|
  Pod::Installer::Xcode::TargetValidator.send(:define_method, :verify_no_static_framework_transitive_dependencies) {}

  installer.pod_targets.each do |pod|
    if pod.name.start_with?('react-native-kakao-sdk')
      def pod.build_type;
        Pod::BuildType.static_library # get kakao_login static approach instead of dynamic one because of use_frameworks!
      end
    end
  end
end
```

## Added / Edited Functions

- `initializeKakao()`
- `login(serviceTerms)`
- `loginWithNewScopes(scopes)`
- `sendLinkFeed(params)`

<br />

### initializeKakao(): Promise<KakaoOAuthTokenStatusInfo>

---

It returns `KakaoStatus` which notice kakao token status.<br />

<b>Return</b>

```typescript
{
  status: KakaoStatus;
}

enum KakaoStatus {
  LOGIN_NEEDED = 'LOGIN_NEEDED',
  ERROR = 'ERROR',
  SUCCEED = 'SUCCEED',
}
```

### login(serviceTerms?: string[]): Promise<KakaoOAuthToken>

---

Kakao sync를 지원합니다.<br />
최초의 kakao sync는 기존 login함수로도 동작하지만,<br />
이미 kakao sync로 가입한 유저는 serviceTerm이 수정되거나 추가되어도 그냥 로그인 되버립니다.<br />
이 경우 serviceTerms를 파라미터로 넘기면 해당 terms에 대해 동의 받을 수 있습니다.<br />

<br />

### loginWithNewScopes(scopes: [string]): Promise<KakaoOAuthToken>

---

추가로 동의받아야 할 항목을 동의받을 수 있습니다.<br />
ex) 'friends' 권한이 추가되었을 경우 아래처럼 사용하면 됩니다.

`await loginWithNewScopes(['friends'])`

<br />

### sendLinkFeed(params: KakaoLinkParams): Promise<void>

---

카카오 링크를 보냅니다 (공유하기)

<b>Parameter</b>

```typescript
{
  title: string;
  description: string;
  imageUrl: string;
  imageLinkUrl: string;
  buttonTitle: string;
  imageWidth?: number;
  imageHeight?: number;
}
```
