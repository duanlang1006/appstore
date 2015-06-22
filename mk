cd extras/applitecommon
../../gradlew makeJar

cd ../impl
../../gradlew makeJar

cd ../mitUpdateSDK
../../gradlew makeJar

cd ../../bundlers/applitelogo
../../gradlew clean makePlugin

cd ../applitehomepage
../../gradlew clean makePlugin

cd ../applitesearch
../../gradlew clean makePlugin

cd ../applitedm
../../gradlew clean makePlugin

cd ../applitedetail
../../gradlew clean makePlugin

cd ../../app
../gradlew clean assembleRelease installLeadexceed_buildinRelease
