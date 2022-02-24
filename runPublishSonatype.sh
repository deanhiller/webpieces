#!/bin/bash

ver=$@
res="${ver//[^.]}"
if [ "${#res}" -lt 2 ]; then
  echo "##################################"
  echo "      Version must be X.Y.Z       "
  echo "##################################"
  exit 1
fi

#if sonatype fails, we can republish without running whole build with this 
./gradlew --stacktrace -PprojVersion="$@" -Pparallel=1 publishMavenJavaPublicationToSonatypeRepository -x test
