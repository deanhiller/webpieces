#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

BRANCH=`git rev-parse --abbrev-ref HEAD`

echo "Starting to build on branch=$BRANCH"

if [ "$BRANCH" == "master" ]; then
   echo "monobuild.sh canNOT be run on master branch"
   exit 1
fi

echo "Beginning monobuild"

cd $DIR/../..

$DIR/../ci/core/build.sh $@

if [ $? -eq 0 ]
then
   echo "Successfully built"
else
   exit 1
fi

