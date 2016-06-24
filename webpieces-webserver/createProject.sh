#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

CREATION_JARS=$DIR/creation/*

java -cp $CREATION_JARS org.webpieces.projects.ProjectCreator $@

