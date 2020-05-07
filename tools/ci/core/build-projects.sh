#!/bin/bash

# Documentation
read -r -d '' USAGE_TEXT << EOM
Usage:
  build-projects.sh <project>...

  Trigger build for all given projects and wait till all builds are successful.
  Project is identified with relative path to project's root directory from repository root.
  When one of build fail then exit with error message.

  Configurable with additional environment variables:
      BUILD_MAX_SECONDS - maximum time in seconds to wait for all builds (15 minutes by default)
      BUILD_CHECK_AFTER_SECONDS - delay between checking status of builds again (15 seconds by default)

  <project>       id of project to build
                  minimally one, can be multiple
EOM


echo "Building all projects with version=$SHA"

# Find script directory (no support for symlinks)
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Configuration with default values
: "${BUILD_MAX_SECONDS:=$(( 15 * 60 ))}"
: "${BUILD_CHECK_AFTER_SECONDS:=15}"
: "${CI_PLUGIN:=$DIR/../plugins/circleci.sh}"

# Validate requirements
if [[ "$#" -eq 0 ]]; then
    echo "No projects to build." # The changes are not involved in a project that we care about so slam it in
    exit 0
fi

#TODO: Fix this to be in parallel via a gradle script later...

# Trigger build for all given projects
PROJECTS=()
for PROJECT in $@; do
    echo "Triggering build for project '$PROJECT' projects=$@"
    PROJECT_NAME=$(basename $PROJECT)
    cd $DIR/../../../$PROJECT

    if [ -z "$SHA" ]
    then
        echo "Running just xgradlew build (so will not deploy)"
        ../../../tools/ospath/xgradlew build &
    else
        echo "Running xgradlew build -PprojVersion=$SHA"
        ../../../tools/ospath/xgradlew build -PprojVersion=$SHA &
    fi

    PID=$!

    wait $PID

    if [ $? -eq 0 ]
    then
        echo "Successfully build $PROJECT_NAME"
    else
        echo "Build failed for project=$PROJECT_NAME" >&2
        exit 1
    fi

done;


echo "******************************************************************************"
echo "******************************************************************************"
echo "                     This set of Builds PASSED                             "
echo "******************************************************************************"

