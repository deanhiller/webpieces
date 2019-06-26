#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

#RUN test first by building fake release THEN building fake project THEN building the fake project to make sure it works
#./gradlew clean build release -x javadoc
printf "\n*********Running ./gradlew --stacktrace clean build release -x javadoc *****\n"
./gradlew clean build release -x javadoc
#./gradlew -Dorg.gradle.parallel=false -Dorg.gradle.configureondemand=false build -PincludeH2Spec=true

test_result=$?
if [ $test_result -eq 0 ]
then
  echo "##################################"
  echo "Successfully BUILT FAKE MAVEN RELEASE(FOR TESTING) $?"
  echo "##################################"
else
  echo "##################################"
  echo "BUILDING FAKE MAVEN RELEASE(FOR TESTING) FAILED $test_result"
  echo "##################################"
  exit $test_result
fi

##############################################################
# Test upgrading legacy code to make sure we stay backwards compatible
#############################################################

if [ ! -d "../webpiecesexample-all" ]; then
   echo "legacy project is not on disk so git cloning now so we can test backwards compatibility"
   cd ..
   printf "\n*******Running git clone https://github.com/deanhiller/webpiecesexample-all.git  *****\n"
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
git checkout master # just in case checkout the project to master

printf "\n********** Running ./gradlew clean build assembleDist **************\n"
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

echo path=$PWD

##############################################################
# Test creation of project, build of new project and start of the server
#############################################################

cd webserver/output/webpiecesServerBuilder

echo path2=$PWD

printf "**********./createProject.sh running from $PWD**********"
./createProject.sh WebpiecesExample org.webpieces ..

echo createproject done
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

cd ../webpiecesexample-all
printf "\n******** Running ./gradlew build assembleDist from webpiecesexample-all *********\n"
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

cd webpiecesexample/output/distributions
unzip webpiecesexample.zip
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
cd webpiecesexample
printf "\n************* Run ./bin/webpiecesexample to start server ***********\n"
./bin/webpiecesexample &
server_pid=$!

#AHHHHH, my computer with this code is clocking 5 seconds to lookup localhost all of the sudden...
#	public static void main(String[] args) throws UnknownHostException {
#		long start = System.currentTimeMillis();
#        InetAddress localhost = InetAddress.getLocalHost();
#
#        long total = (System.currentTimeMillis() - start) / 1000;
#        System.out.println("total="+total);
#	}
#
#SOOO sleep time is increased from 5 seconds to 10 so builds pass...this sucks, my server used to startup in 3 seconds

#Logback has a 5 second pause we should go debug on jdk9 at least causing this to be 10 seconds instead of 5
SLEEP_TIME=10
echo "sleep $SLEEP_TIME seconds while server starts up"
sleep $SLEEP_TIME 
echo "Grepping log"

if grep -q "o.w.w.i.WebServerImpl     server started" logs/server.log; then
  echo "##################################"
  echo "11111 Server is located at `pwd`"
  echo "Server Startup Succeeded within $SLEEP_TIME seconds!!"
  echo "##################################"
else
  echo "##################################"
  echo "11111 Server Startup Failed to be done in $SLEEP_TIME seconds"
  echo "Failed Startup.  Server is located at `pwd`"
  echo "##################################"
  kill -9 $server_pid
  exit 99
fi

#Downloading https page on server

#Test out a curl request to localhost to make sure basic webpage is working
curl -kL https://localhost:8443/@backend/secure/sslsetup > downloadedhtml.txt

if grep -q "BACKEND Login" downloadedhtml.txt; then
  kill -9 $server_pid
  echo "##################################"
  echo "2222 Server is located at `pwd`"
  echo "Server Download Page Successful!!"
  echo "##################################"
else
  echo "##################################"
  echo "2222 Server Startup Failed to be done in $SLEEP_TIME seconds"
  echo "Failed Download https page.  Server is located at `pwd`"
  echo "##################################"
  kill -9 $server_pid
  exit 99
fi
