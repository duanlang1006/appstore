cd app
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