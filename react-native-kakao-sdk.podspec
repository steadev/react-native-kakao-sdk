# @steadev/react-native-kakao-sdk.podspec

require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))
kakao_sdk_version = "2.11.1"

Pod::Spec.new do |s|
  s.name         = "react-native-kakao-sdk"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                @steadev/react-native-kakao-sdk
                   DESC
  s.homepage     = "https://github.com/steadev/react-native-kakao-sdk"
  # brief license entry:
  s.license      = "MIT"
  # optional - use expanded license entry instead:
  # s.license    = { :type => "MIT", :file => "LICENSE" }
  s.authors      = { "dooboolab" => "support@dooboolab.com" }
  s.platforms    = { :ios => "11.0" }
  s.framework     = 'UIKit'
  s.source       = { :git => "https://github.com/steadev/react-native-kakao-sdk.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,c,cc,cpp,m,mm,swift}"
  s.requires_arc = true

  s.dependency "React"

  if defined?($KakaoSDKVersion)
    Pod::UI.puts "#{s.name}: Using user specified Kakao SDK version '#{$KakaoSDKVersion}'"
    kakao_sdk_version = $KakaoSDKVersion
  end

  # s.dependency 'KakaoSDK', kakao_sdk_version
  s.dependency 'KakaoSDKCommon',  kakao_sdk_version
  s.dependency 'KakaoSDKAuth',  kakao_sdk_version
  s.dependency 'KakaoSDKUser', kakao_sdk_version
  s.dependency 'KakaoSDKTalk', kakao_sdk_version
  s.dependency 'KakaoSDKTemplate', kakao_sdk_version
  s.dependency 'KakaoSDKShare', kakao_sdk_version
end
