import { NativeModules } from 'react-native';

const { RNKakaoLogins } = NativeModules;

export enum KakaoOAuthTokenStatus {
  LOGIN_NEEDED = 'LOGIN_NEEDED',
  ERROR = 'ERROR',
  SUCCEED = 'SUCCEED'
}

export type KakaoOAuthTokenStatusInfo = {
  status: KakaoOAuthTokenStatus;
}

export type KakaoOAuthToken = {
  accessToken: string;
  refreshToken: string;
  idToken: string;
  accessTokenExpiresAt: Date;
  refreshTokenExpiresAt: Date;
  scopes: string[];
};

export type KakaoAccessTokenInfo = {
  accessToken: string;
  refreshToken: string;
  expiresIn: string;
};

export type KakaoProfile = {
  id: string;
  email: string;
  nickname: string;
  profileImageUrl: string;
  thumbnailImageUrl: string;
  phoneNumber: string;
  ageRange: string;
  birthday: string;
  birthdayType: string;
  birthyear: string;
  gender: string;
  isEmailValid: boolean;
  isEmailVerified: boolean;
  isKorean: boolean;
  ageRangeNeedsAgreement?: boolean;
  birthdayNeedsAgreement?: boolean;
  birthyearNeedsAgreement?: boolean;
  emailNeedsAgreement?: boolean;
  genderNeedsAgreement?: boolean;
  isKoreanNeedsAgreement?: boolean;
  phoneNumberNeedsAgreement?: boolean;
  profileNeedsAgreement?: boolean;
};

export type KakaoLinkParams = {
  title: string;
  description: string;
  imageUrl: string;
  imageLinkUrl: string;
  buttonTitle: string;
  imageWidth?: number;
  imageHeight?: number;
}

export type KakaoProfileNoneAgreement = {
  id: string;
};

export const initializeKakao = async (): Promise<KakaoOAuthTokenStatusInfo> => {
  try {
    const result: KakaoOAuthTokenStatusInfo = await RNKakaoLogins.initializeKakao();

    return result;
  } catch (err) {
    throw err;
  }
}

export const login = async (serviceTerms?: string[]): Promise<KakaoOAuthToken> => {
  try {
    const result: KakaoOAuthToken = await RNKakaoLogins.login(serviceTerms);

    return result;
  } catch (err) {
    throw err;
  }
};

export const loginWithNewScopes = async (scopes: [string]): Promise<KakaoOAuthToken> => {
  try {
    const result: KakaoOAuthToken = await RNKakaoLogins.loginWithNewScopes(scopes);

    return result;
  } catch (err) {
    throw err;
  }
}

export const logout = async (): Promise<string> => {
  try {
    const result: string = await RNKakaoLogins.logout();

    return result;
  } catch (err) {
    throw err;
  }
};

export const unlink = async (): Promise<string> => {
  try {
    const result: string = await RNKakaoLogins.unlink();

    return result;
  } catch (err) {
    throw err;
  }
};

export const getProfile = async (): Promise<
  KakaoProfile | KakaoProfileNoneAgreement
> => {
  try {
    const result: KakaoProfile = await RNKakaoLogins.getProfile();

    return result;
  } catch (err) {
    throw err;
  }
};

export const getAccessToken = async (): Promise<KakaoAccessTokenInfo> => {
  try {
    const result: KakaoAccessTokenInfo = await RNKakaoLogins.getAccessToken();

    return result;
  } catch (err) {
    throw err;
  }
};

export const sendLinkFeed = async (params: KakaoLinkParams): Promise<void> => {
  try {
    await RNKakaoLogins.sendLinkFeed(params);

  } catch (err) {
    throw err;
  }
}