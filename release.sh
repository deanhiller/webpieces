#!/bin/bash

start=`date +%s`

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

./runAllTesting.sh
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

#MUST turn parallel builds off for release or it fails!!!
#We skip tests since those are done in runAllTesting.sh AND that script ALSO test legacy compatibility and building a fake project from the new release AND starting the server
./gradlew --stacktrace -Dorg.gradle.parallel=false -Dorg.gradle.configureondemand=false -PprojVersion=$@ clean release -x test
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

git tag v1.9.$@

end=`date +%s`
runtime=$((end-start))
echo "###################################"
echo "RUNTIME=$runtime (MOST of this is due to sonas plugin needing to be single threaded)"
echo "###################################"
