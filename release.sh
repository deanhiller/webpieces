#!/bin/bash

start=`date +%s`

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

ver=$@
res="${ver//[^.]}"
if [ "${#res}" -lt 2 ]; then
  echo "##################################"
  echo "      Version must be X.Y.Z       "
  echo "##################################"
  exit 1
fi

./runAllTestingLocalRelease.sh
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

cd webpiecesexample/build/distributions
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
#       public static void main(String[] args) throws UnknownHostException {
#               long start = System.currentTimeMillis();
#        InetAddress localhost = InetAddress.getLocalHost();
#
#        long total = (System.currentTimeMillis() - start) / 1000;
#        System.out.println("total="+total);
#       }
#
#SOOO sleep time is increased from 5 seconds to 10 so builds pass...this sucks, my server used to startup in 3 seconds

#From before, but no more right now at least:
#Logback has a 5 second pause we should go debug on jdk9 at least causing this to be 10 seconds instead of 5
SLEEP_TIME=5
echo "sleep $SLEEP_TIME seconds while server starts up"
sleep $SLEEP_TIME
echo "Grepping log"

if grep -q "o.w.w.i.WebServerImpl     All servers started" logs/server.log; then
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
curl -kL https://localhost:8443/@sslcert > downloadedhtml.txt

end=`date +%s`
runtime=$((end-start))

if grep -q "BACKEND Login" downloadedhtml.txt; then
  kill -9 $server_pid
  echo "###########################################################################"
  echo "2222 Server is located at `pwd`"
  echo "Server Download Page Successful!! Build took $runtime seconds"
  echo "###########################################################################"
else
  echo "##################################"
  echo "2222 Server Startup Failed to be done in $SLEEP_TIME seconds"
  echo "Failed Download https page.  Server is located at `pwd`"
  echo "Build took $runtime seconds"
  echo "##################################"
  kill -9 $server_pid
  exit 99
fi










echo "##################################"
echo "next copy over legacy project to upgrade it(auto-upgrade)"
echo "##################################"

rm -rf ../webpiecesexample-all/*
cp -r webserver/output/webpiecesexample-all/* ../webpiecesexample-all
#Remove the cache directory that ends up there
rm -rf ../webpiecesexample-all/webpiecesexample/webpiecesCache/

echo "##################################"
echo "next release to maven repositories"
echo "##################################"

#This one is breaking the release/upload to nexus :( such that it creates
#multiple repos which is too bad as it is REALLY fast
#MUST turn parallel builds off for release or it fails!!!
#We skip tests since those are done in runAllTesting.sh AND that script ALSO test legacy compatibility and building a fake project from the new release AND starting the server
./gradlew --stacktrace -PprojVersion="$@" release -x test --scan && ./gradlew --stacktrace -PprojVersion="$@" :webserver:gradle-plugin-htmlcompiler:publishPlugins
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

end=`date +%s`
runtime=$((end-start))
echo "###################################"
echo "RUNTIME=$runtime (MOST of this is due to sonas plugin needing to be single threaded)"
echo "###################################"

echo ""
echo ""
echo ""
echo ""

echo "###################################"
echo "IMPORTANT: Next go cd ../webpiecesexample-all and run git status and git diff"
echo "THEN, undo the build.gradle version number, the securekey, and the README usually and leave everything else"
echo "THEN, check it in and commit with the message of this version you just released $@"
echo "###################################"

echo "###########################################################################"
echo "###########################################################################"
echo "###########################################################################"
echo "       DO NOT FORGET, git commit webpiecesexample-all with $@"
echo "###########################################################################"
echo "###########################################################################"
echo "###########################################################################"
echo "###########################################################################"
cd ../webpiecesexample-all
git status
