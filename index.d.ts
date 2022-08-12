export declare type KakaoOAuthToken = {
    accessToken: string;
    refreshToken: string;
    idToken: string;
    accessTokenExpiresAt: Date;
    refreshTokenExpiresAt: Date;
    scopes: string[];
};
export declare type KakaoAccessTokenInfo = {
    accessToken: string;
    expiresIn: string;
};
export declare type KakaoProfile = {
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
export declare type KakaoProfileNoneAgreement = {
    id: string;
};
export declare const login: () => Promise<KakaoOAuthToken>;
export declare const loginWithKakaoAccount: () => Promise<KakaoOAuthToken>;
export declare const logout: () => Promise<string>;
export declare const unlink: () => Promise<string>;
export declare const getProfile: () => Promise<KakaoProfile | KakaoProfileNoneAgreement>;
export declare const getAccessToken: () => Promise<KakaoAccessTokenInfo>;
