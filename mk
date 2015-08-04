cd extras/applitecommon
../../gradlew makeJar
if [ $? -eq 0 ];then
    echo applitecommon ok!!!!!!!!!!!!!!!!!!!!!!!!
else
    echo applitecommon error!!!!!!!!!!!!!!!!!!!!!!!!
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

cd ../mitUpdateSDK
../../gradlew makeJar
if [ $? -eq 0 ];then
    echo mitUpdateSDK ok!!!!!!!!!!!!!!!!!!!!!!!!
else
    echo mitUpdateSDK error!!!!!!!!!!!!!!!!!!!!!!!!
    exit 0
fi

#cd ../../bundlers/applitelogo
#../../gradlew clean makePlugin
#if [ $? -eq 0 ];then
#    echo applitelogo ok!!!!!!!!!!!!!!!!!!!!!!!!
#else
#    echo applitelogo error!!!!!!!!!!!!!!!!!!!!!!!!
#    exit 0
#fi

cd ../../bundlers/applitehomepage
../../gradlew clean makePlugin
if [ $? -eq 0 ];then
    echo applitehomepage ok!!!!!!!!!!!!!!!!!!!!!!!!
else
    echo applitehomepage error!!!!!!!!!!!!!!!!!!!!!!!!
    exit 0
fi

cd ../applitesearch
../../gradlew clean makePlugin
if [ $? -eq 0 ];then
    echo applitesearch ok!!!!!!!!!!!!!!!!!!!!!!!!
else
    echo applitesearch error!!!!!!!!!!!!!!!!!!!!!!!!
    exit 0
fi

cd ../applitedm
../../gradlew clean makePlugin
if [ $? -eq 0 ];then
    echo applitedm ok!!!!!!!!!!!!!!!!!!!!!!!!
else
    echo applitedm error!!!!!!!!!!!!!!!!!!!!!!!!
    exit 0
fi

cd ../applitedetail
../../gradlew clean makePlugin
if [ $? -eq 0 ];then
    echo applitedetail ok!!!!!!!!!!!!!!!!!!!!!!!!
else
    echo applitedetail error!!!!!!!!!!!!!!!!!!!!!!!!
    exit 0
fi

cd ../appliteupdate
../../gradlew clean makePlugin
if [ $? -eq 0 ];then
    echo appliteupdate ok!!!!!!!!!!!!!!!!!!!!!!!!
else
    echo appliteupdate error!!!!!!!!!!!!!!!!!!!!!!!!
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