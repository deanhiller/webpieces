
#if sonatype fails, we can republish without running whole build with this 
./gradlew --stacktrace -PprojVersion="2.1.27" -Pparallel=1 publishMavenJavaPublicationToSonatypeRepository -x test
