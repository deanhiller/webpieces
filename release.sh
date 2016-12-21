#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

./runAllTesting.sh
if [ $? -eq 0 ]
then
  echo "Successfully RAN ALL TESTING"
else
  echo "TESTING SYSTEM Failed(don't release this)"
  exit $?
fi

#MUST turn parallel builds off for release or it fails!!!
./gradlew -Dorg.gradle.parallel=false -Dorg.gradle.configureondemand=false -PprojVersion=$@ clean release
