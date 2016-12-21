#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

#RUN test first by building fake release THEN building fake project THEN building the fake project to make sure it works
./gradlew clean build release
if [ $? -eq 0 ]
then
  echo "Successfully BUILT FAKE RELEASE"
else
  echo "Build Failed"
  exit $?
fi

cd webserver/output/webpiecesServerBuilder
./createProject.sh MyTest com.test ..

if [ $? -eq 0 ]
then
  echo "Successfully CREATED Project "
else
  echo "Example Project Creation Failed"
  exit $?
fi

cd ../mytest-all
./gradlew build assembleDist

if [ $? -eq 0 ]
then
  echo "Successfully BUILT EXAMPLE Project "
else
  echo "Example Project BUILD Failed"
  exit $?
fi

#TODO: startup the server in background and run test to grep out success in log files

