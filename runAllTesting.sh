#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

#RUN test first by building fake release THEN building fake project THEN building the fake project to make sure it works
./gradlew clean build release -x javadoc
test_result=$?
if [ $test_result -eq 0 ]
then
  echo "##################################"
  echo "Successfully BUILT FAKE RELEASE $?"
  echo "##################################"
else
  echo "##################################"
  echo "BUILDING FAKE RELEASE FAILED $test_result"
  echo "##################################"
  exit $test_result
fi

##############################################################
# Test upgrading legacy code to make sure we stay backwards compatible
#############################################################

if [ ! -d "../webpiecesexample-all" ]; then
   echo "legacy project is not on disk so git cloning now so we can test backwards compatibility"
   cd ..
   git clone https://github.com/deanhiller/webpiecesexample-all.git
   test_result=$?
   if [ $test_result -eq 0 ]
   then
       echo "##################################"
       echo "Successfully cloned legacy repo $?"
       echo "##################################"
   else
       echo "##################################"
       echo "FAILURE IN cloning legacy repo $test_result"
       echo "##################################"
       exit $test_result
   fi
   cd webpieces
else
   echo "Found legacy project already checked out=../webpiecesexample-all"
fi

cd ../webpiecesexample-all

./gradlew clean build assembleDist
test_result=$?
if [ $test_result -eq 0 ]
then
  echo "##################################"
  echo "Successfully BUILT LEGACY PROJECT $?"
  echo "##################################"
else
  echo "##################################"
  echo "BUILDING LEGACY PROJECT FAILED $test_result"
  echo "##################################"
  exit $test_result
fi

#reset to webpieces directory
cd ../webpieces 

##############################################################
# Test creation of project, build of new project and start of the server
#############################################################

cd webserver/output/webpiecesServerBuilder
./createProject.sh MyTest com.test ..
test_result=$?
if [ $test_result -eq 0 ]
then
  echo "##################################"
  echo "Successfully CREATED Project "
  echo "##################################"
else
  echo "##################################"
  echo "Example Project Creation Failed"
  echo "##################################"
  exit $test_result
fi

cd ../mytest-all
./gradlew build assembleDist
test_result=$?
if [ $test_result -eq 0 ]
then
  echo "##################################"
  echo "Successfully BUILT EXAMPLE Project "
  echo "##################################"
else
  echo "##################################"
  echo "Example Project BUILD Failed"
  echo "##################################"
  exit $test_result
fi

cd mytest/output/distributions
unzip mytest.zip
test_result=$?
if [ $test_result -eq 0 ]
then
  echo "##################################"
  echo "Successfully Unzipped Production Server to `pwd`"
  echo "##################################"
else
  echo "##################################"
  echo "Unzip Production server FAILED"
  echo "##################################"
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
  echo "##################################"
  echo "Server is located at `pwd`"
  echo "Server Startup Succeeded!!"
  echo "##################################"
else
  echo "##################################"
  echo "Server Startup Failed to be done in 5 seconds"
  echo "Failed Server is located at `pwd`"
  echo "##################################"
  exit 99
fi

