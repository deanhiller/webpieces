#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR
echo "*********Running ./gradlew installDist**********"
./gradlew installDist

echo $DIR
echo "********Running webpiecesServerBuilder********"
./build/install/webpiecesServerBuilder/bin/webpiecesServerBuilder $@

