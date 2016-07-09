#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR
./gradlew installDist

echo $DIR
./build/install/projecttemplates/bin/projecttemplates


