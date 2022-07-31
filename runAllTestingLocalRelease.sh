#!/bin/bash

set -e

start=`date +%s`

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

#RUN test first by building fake release THEN building fake project THEN building the fake project to make sure it works
printf "\n*********Running ./gradlew --stacktrace clean build from ${DIR} *****\n"
./gradlew --stacktrace clean
./gradlew --stacktrace release

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
   cd "${DIR}"
else
   echo "Found legacy project already checked out=../webpiecesexample-all"
fi

cd ../webpiecesexample-all
git pull

printf "\n********** Running ./gradlew clean build assembleDist **************\n"
./gradlew clean
./gradlew assembleDist
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
cd "${DIR}"

echo path=$PWD

##############################################################
# Test creation of project, build of new project and start of the server
#############################################################

cd webserver-templates/build/webpiecesServerBuilder

echo path2=$PWD

printf "**********./createProject.sh running from $PWD**********"
./createProject.sh TemplateBasic WebpiecesExample org.webpieces ..

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

mv ../webpiecesexample ../webpiecesexample-all
ls ../webpiecesexample-all
cd ../webpiecesexample-all
printf "path=$PWD"
printf "\n******** Running ./gradlew build assembleDist from webpiecesexample-all $PWD *********\n"
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

end=`date +%s`
runtime=$((end-start))
echo "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
echo "RUNTIME=$runtime"
echo "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"

