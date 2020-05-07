#!/bin/bash
if [ $# -eq 0 ]
  then
    echo "A branch name argument is required. USAGE: './prepareForReview.sh {branch_to_review}'"
    exit 1
fi

REVIEW_BRANCH="$1"

if [[ $REVIEW_BRANCH != "review_"* ]]; then
   echo "This script is only for reviewing remote brances starting with review_ (ie. sync review branches)"
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

echo "Checking out master"
git checkout master

SUBMIT_BRANCH="submit${REVIEW_BRANCH:6}"

echo "Checking out submit branch $SUBMIT_BRANCH"
git checkout -b "$SUBMIT_BRANCH"

echo "Merging in changes from $REVIEW_BRANCH"
git merge --squash "$REVIEW_BRANCH"

