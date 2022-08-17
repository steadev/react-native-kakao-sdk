# @steadev/react-native-kakao-sdk

---

forked from [@react-native-seoul/kakao-login@4.2.3](https://www.npmjs.com/package/@react-native-seoul/kakao-login)

Supports `Kakao Sync`, `Kakao link`, `loginWithNewScopes`(추가항목 동의받기)

Android Kakao SDK version: 2.11.0
iOS Kakao SDK version: 2.9.0

## Added / Edited Functions

- `initializeKakao()`
- `login(serviceTerms)`
- `loginWithNewScopes(scopes)`
- `sendLinkFeed(params)`

### initializeKakao(): Promise<void>

---

It returns `KakaoStatus` which notice kakao token status.

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

Kakao sync를 지원합니다.
최초의 kakao sync는 기존 login함수로도 동작하지만,
이미 kakao sync로 가입한 유저는 serviceTerm이 수정되거나 추가되어도 그냥 로그인 되버립니다.
이 경우 serviceTerms를 파라미터로 넘기면 해당 terms에 대해 동의 받을 수 있습니다.

### loginWithNewScopes(scopes: [string]): Promise<KakaoOAuthToken>

---

추가로 동의받아야 할 항목을 동의받을 수 있습니다.
ex) 'friends' 권한이 추가되었을 경우 아래처럼 사용하면 됩니다.

```
	await loginWithNewScopes(['friends'])
```

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
