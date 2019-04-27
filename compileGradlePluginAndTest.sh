#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

#RUN test first by building fake release THEN building fake project THEN building the fake project to make sure it works
./gradlew :webserver:gradle-plugin-htmlcompiler:uploadArchives -P=true -x javadoc

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

