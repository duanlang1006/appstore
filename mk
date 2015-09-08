cd extras/mitUpdateSDK
../../gradlew makeJar
if [ $? -eq 0 ];then
    echo mitUpdateSDK ok!!!!!!!!!!!!!!!!!!!!!!!!
else
    echo mitUpdateSDK error!!!!!!!!!!!!!!!!!!!!!!!!
    exit 0
fi

cd ../impl
../../gradlew makeJar
if [ $? -eq 0 ];then
    echo impl ok!!!!!!!!!!!!!!!!!!!!!!!!
else
    echo impl error!!!!!!!!!!!!!!!!!!!!!!!!
    exit 0
fi

cd ../applitecommon
../../gradlew makeJar
if [ $? -eq 0 ];then
    echo applitecommon ok!!!!!!!!!!!!!!!!!!!!!!!!
else
    echo applitecommon error!!!!!!!!!!!!!!!!!!!!!!!!
    exit 0
fi

cd ../../app
if [ $# -lt 1 ]; then
../gradlew clean assembleRelease installLeadexceed_buildinRelease
else
../gradlew clean assembleRelease $*
fi

if [ $? -eq 0 ];then
    echo build app ok!!!!!!!!!!!!!!!!!!!!!!!!
else
    echo build app error!!!!!!!!!!!!!!!!!!!!!!!!
    exit 0
fi