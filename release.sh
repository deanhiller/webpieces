#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR
#MUST turn parallel builds off for release or it fails!!!
./gradlew -Dorg.gradle.parallel=false -Dorg.gradle.configureondemand=false -PprojVersion=$1 clean release
