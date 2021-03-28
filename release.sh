#!/bin/bash

start=`date +%s`

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

ver=$@
res="${ver//[^.]}"
if [ "${#res}" -lt 2 ]; then
  echo "##################################"
  echo "      Version must be X.Y.Z       "
  echo "##################################"
  exit 1
fi

./runAllTestingLocalRelease.sh
test_result=$?
if [ $test_result -eq 0 ]
then
  echo "##################################"
  echo "Successfully RAN ALL TESTING $?"
  echo "##################################"
else
  echo "##################################"
  echo "TESTING SYSTEM Failed(don't release this) $test_result"
  echo "##################################"
  exit $test_result
fi

echo "##################################"
echo "next copy over legacy project to upgrade it(auto-upgrade)"
echo "##################################"

rm -rf ../webpiecesexample-all/*
cp -r webserver/output/webpiecesexample-all/* ../webpiecesexample-all
#Remove the cache directory that ends up there
rm -rf ../webpiecesexample-all/webpiecesexample/webpiecesCache/

echo "##################################"
echo "next release to maven repositories"
echo "##################################"

#This one is breaking the release/upload to nexus :( such that it creates
#multiple repos which is too bad as it is REALLY fast
#MUST turn parallel builds off for release or it fails!!!
#We skip tests since those are done in runAllTesting.sh AND that script ALSO test legacy compatibility and building a fake project from the new release AND starting the server
./gradlew --stacktrace -PprojVersion=$@ release -x test --scan
test_result=$?
if [ $test_result -eq 0 ]
then
  echo "##################################"
  echo "RELEASE DONE, tagging git next"
  echo "##################################"
else
  echo "##################################"
  echo "RELEASE FAIL, not tagging git $test_result"
  echo "##################################"
  exit $test_result
fi 

git tag v$@

end=`date +%s`
runtime=$((end-start))
echo "###################################"
echo "RUNTIME=$runtime (MOST of this is due to sonas plugin needing to be single threaded)"
echo "###################################"

echo ""
echo ""
echo ""
echo ""

echo "###################################"
echo "IMPORTANT: Next go cd ../webpiecesexample-all and run git status and git diff"
echo "THEN, undo the build.gradle version number, the securekey, and the README usually and leave everything else"
echo "THEN, check it in and commit with the message of this version you just released $@"
echo "###################################"

echo "###########################################################################"
echo "###########################################################################"
echo "###########################################################################"
echo "       DO NOT FORGET, git commit webpiecesexample-all with $@"
echo "###########################################################################"
echo "###########################################################################"
echo "###########################################################################"
echo "###########################################################################"
cd ../webpiecesexample-all
git status
