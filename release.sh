#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

./runAllTesting.sh
test_result=$?
if [ $test_result -eq 0 ]
then
  echo "Successfully RAN ALL TESTING $?"
else
  echo "TESTING SYSTEM Failed(don't release this) $test_result"
  exit $test_result
fi

#MUST turn parallel builds off for release or it fails!!!
#We skip tests since those are done in runAllTesting.sh AND that script ALSO test legacy compatibility and building a fake project from the new release AND starting the server
./gradlew --stacktrace -Dorg.gradle.parallel=false -Dorg.gradle.configureondemand=false -PprojVersion=$@ clean release -x test
