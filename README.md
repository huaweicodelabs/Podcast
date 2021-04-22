# HuaweiPodcast
# Huawei Mobile Services
Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.

## Table of Contents

 * [Introduction](#introduction)
 * [Installation](#installation)
 * [Configuration ](#configuration)
 * [Supported Environments](#supported-environments)
 * [Sample Code](#sample-code)
 * [License](#license)

## Introduction : HuaweiPodcast
HUAWEI PODCAST App will give insight about how HMS Kits can be easily integrated into your app to help you increase revenue. Here user can Search, Play, Subscribe and Download podcasts according to their interest using HMS capabilities.

## Installation
    To use functions provided by examples, please make sure Huawei Mobile Service 5.0 has been installed on your cellphone.
    There are two ways to install the sample demo:
    You can compile and build the codes in Android Studio. After building the APK, you can install it on the phone and debug it.
    Generate the APK file from Gradle. Use the ADB tool to install the APK on the phone and debug it adb install
    {YourPath}\app\release\app-release.apk

## Supported Environments
    Android Studio 3.X, JDK 1.8 and later , SDK Platform 19 and later, Gradle 4.6 and later
    HMS Core (APK) 4.0.0 or later has been installed on Huawei Android phones.

## Configuration
     Create an app in AppGallery Connect and obtain the project configuration file agconnect-services.json.
     In Android Studio, switch to the Project view and move the agconnect-services.json file to the root directory of the app.
     Change the value of applicationId in the build.gradle file of the app to the name of the app package applied for in the preceding step.

## Sample Code
    Huawei Podcast provides demonstration for following scenarios:

    1. Ads integration and demonstrated in Splash Activity and Episode Detail Activity.
    2. Integrated the Cloud DB. Query, add and retrieve operations are performed using the cloud DB,
       In the app data is fetching from Cloud DB.
    3. Integrated the Audio Kit in Play Audio Activity, the audio url is played by using the Audio Kit. Audio Kit is used to perform, play, pause, stop, next play and previous play functions.
    4. Integrated few common kits like Analytics, Crash Service, APM, AuthService With AGCUI and Push kit.


## License
    Huawei Podcast sample is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
    * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
    * Unless required by applicable law or agreed to in writing, software distributed under the
    * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
    * express or implied. See the License for the specific language governing permissions and
    * limitations under the License.

