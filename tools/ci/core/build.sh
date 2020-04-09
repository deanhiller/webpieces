#!/bin/bash

##
# Main entry for monorepository build.
# Triggers builds for all modified projects in order respecting their dependencies.
# 
# Usage:
#   build.sh
##

function log {
    MESSAGE=$1
    >&2 echo "$MESSAGE"
}


# Find script directory (no support for symlinks)
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Configuration with default values
: "${CI_TOOL:=circleci}"
: "${CI_PLUGIN:=$DIR/../plugins/${CI_TOOL}.sh}"

# Resolve commit range for current build 
LAST_SUCCESSFUL_COMMIT=$(${CI_PLUGIN} hash last)
log "Last commit: ${LAST_SUCCESSFUL_COMMIT}"
if [[ ${LAST_SUCCESSFUL_COMMIT} == "null" ]]; then
    COMMIT_RANGE="origin/master"
else
    COMMIT_RANGE="${LAST_SUCCESSFUL_COMMIT}"
fi
log "Commit range: $COMMIT_RANGE"

# Ensure we have all changes from last successful build
if [[ -f $(git rev-parse --git-dir)/shallow ]]; then
    if [[ ${LAST_SUCCESSFUL_COMMIT} == "null" ]]; then
        git fetch --unshallow
    else 
        DEPTH=1
        until git show ${LAST_SUCCESSFUL_COMMIT} > /dev/null 2>&1
        do
            DEPTH=$((DEPTH+5))
            log "Last commit not fetched yet. Fetching depth $DEPTH."
            git fetch --depth=$DEPTH
        done
    fi
fi

# Collect all modified projects
PROJECTS_TO_BUILD=$($DIR/list-projects-to-build.sh $COMMIT_RANGE)

# If nothing to build inform and exit
if [[ -z "$PROJECTS_TO_BUILD" ]]; then
    echo "No projects to build"
    exit 0
fi

echo "----------------------- Projects to Build -----------------------"
echo "$PROJECTS_TO_BUILD"
echo "-----------------------------------------------------------------"

# Build all modified projects
echo "$PROJECTS_TO_BUILD" | while read PROJECTS; do
    log "Calling build-projects.sh ${PROJECTS}"
    $DIR/build-projects.sh ${PROJECTS}

    if [ $? -eq 0 ]
    then
        log "Successfully built projects=$PROJECTS"
    else
        log "Build failed for projects=$PROJECTS" >&2
        exit 1
    fi
done;

if [ $? -eq 0 ]
then
   log "Successfully built"
else
   log "Build failed" >&2
   exit 1
fi

echo "******************************************************************************"
echo "******************************************************************************"
echo "                     ALL BUILDS PASSED                             "
echo "******************************************************************************"
echo "******************************************************************************"

