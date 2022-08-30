#!/bin/bash

ver=$@
res="${ver//[^.]}"
if [ "${#res}" -lt 2 ]; then
  echo "##################################"
  echo "      Version must be X.Y.Z       "
  echo "##################################"
  exit 1
fi

#EXAMPLE to run to publish individual projects when sonatype fails
#./gradlew -PprojVersion="2.1.37" :core-logging:publishMavenJavaPublicationToSonatypeRepository -x test
#For some reason, the command below is not working

TASK=$2:publishMavenJavaPublicationToSonatypeRepository

echo "Running task ${TASK}"

#if sonatype fails, we can republish without running whole build with this 
./gradlew -PprojVersion="$@" $TASK -x test
