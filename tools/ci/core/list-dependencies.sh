#!/bin/bash

##
# List all dependencies between projects.
# Poject is identified with relative path to project's root directory from repository root.
# Dependencies are based on Gradle's `composite build` feature (https://docs.gradle.org/current/userguide/composite_builds.html).
# Dependency is defined by using `includeBuild` function in project build script.
# 
# Outputs lines of tuples in format PROJECT1 PROJECT2 (separated by space), 
# where PROJECT1 depends on PROJECT2.
#
# Usage:
#   list-dependencies.sh
##

set -e

function log {
    MESSAGE=$1
    >&2 echo "$MESSAGE"
}

log "Running script list-dependencies.sh $@"

# Find script directory (no support for symlinks)
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

OURPWD=$PWD 

cd $DIR/../../../
export ROOT=`pwd`

length=${#ROOT}

# If project contain `includeBuild` function in any of it's *.gradle file
# then there is dependency on included project
for PROJECT in $(${DIR}/list-projects.sh); do    
    cd $DIR/../../../$PROJECT
    log "Looking into #$PROJECT for dependencies path=`pwd`"
    grep --include=\*.gradle -rwh "$DIR/../../../$PROJECT" -e "includeBuild" | while read INCLUDE; do
        cd $DIR/../../../$PROJECT
        log "Parsing ---$INCLUDE---"
        INCLUDE=$(echo "$INCLUDE" | sed "s/includeBuild ['\"]//" | sed "s/['\"]//")
        cd $PWD/$INCLUDE
        INCLUDE=`pwd`
        #cut one more time...(format must be relative paths from root project)
        INCLUDE="${INCLUDE:$length}" 
        INCLUDE="${INCLUDE:1}" 
        log "Output will be ---$PROJECT $INCLUDE---"
        echo "$PROJECT $INCLUDE"
    done
done

cd $OURPWD
