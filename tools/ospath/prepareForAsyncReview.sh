#!/bin/bash
if [ $# -eq 0 ]
  then
    echo "A branch name argument is required. USAGE: './prepareForAsyncReview.sh {branch_to_review}'"
    exit 1
fi

REVIEW_BRANCH="$1"
if [[ $REVIEW_BRANCH != "asyncreview_"* ]]; then
   echo "This tool is only for remote branches starting with asyncreview_ (ie. async reviews).  your branch=$REVIEW_BRANCH"
   exit 1
fi

function evil_git_dirty {
  [[ $(git diff --shortstat 2> /dev/null | tail -n1) != "" ]] && echo "*"
}

NUM_FILES="$(evil_git_dirty)"
echo "Num files not committed=$NUM_FILES"
if [[ "$NUM_FILES" == "*" ]]; then
   echo ""
   echo "You have outstanding files that are not committed.  commit first or stash, then you can push"
   echo ""
   git status
   echo "----------------------------------------------------------------------------------------------"
   echo "You have outstanding files that are not committed.  commit first or stash, then you can push"
   echo "----------------------------------------------------------------------------------------------"
   exit 1
fi


echo "Checking out review branch $REVIEW_BRANCH"
git checkout "$REVIEW_BRANCH"

if [ $? -eq 0 ]
then
   echo "Your argument for review branch is invalid."
else
   exit 1
fi

echo "*************************************************"
echo "NEXT use intellij to compare and modify stuff"
echo "***************************************************"
