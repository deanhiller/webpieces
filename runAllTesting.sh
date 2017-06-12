#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

#RUN test first by building fake release THEN building fake project THEN building the fake project to make sure it works
./gradlew clean build release
test_result=$?
if [ $test_result -eq 0 ]
then
  echo "Successfully BUILT FAKE RELEASE $?"
else
  echo "BUILDING FAKE RELEASE FAILED $test_result"
  exit $test_result
fi

cd webserver/output/webpiecesServerBuilder
./createProject.sh MyTest com.test ..
test_result=$?
if [ $test_result -eq 0 ]
then
  echo "Successfully CREATED Project "
else
  echo "Example Project Creation Failed"
  exit $test_result
fi

cd ../mytest-all
./gradlew build assembleDist
test_result=$?
if [ $test_result -eq 0 ]
then
  echo "Successfully BUILT EXAMPLE Project "
else
  echo "Example Project BUILD Failed"
  exit $test_result
fi

cd mytest/output/distributions
unzip mytest.zip
test_result=$?
if [ $test_result -eq 0 ]
then
  echo "Successfully Unzipped Production Server to `pwd`"
else
  echo "Unzip Production server FAILED"
  exit $test_result
fi

#TODO: startup the server in background and run test to grep out success in log files
cd mytest
./bin/mytest &
server_pid=$!

echo "sleep 5 seconds"
sleep 5 
echo "Grepping log"

if grep -q "o.w.w.i.WebServerImpl     server started" logs/server.log; then
  kill -9 $server_pid
  echo "Server is located at `pwd`"
  echo "Server Startup Succeeded!!"
else
  echo "Server Startup Failed to be done in 5 seconds"
  echo "Failed Server is located at `pwd`"
  exit 99
fi

