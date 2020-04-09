#!/bin/bash

BRANCH=`git rev-parse --abbrev-ref HEAD`

git push -u origin $BRANCH

git checkout master

git branch -d $BRANCH
