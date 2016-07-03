#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

java -cp $DIR/lib-creation/'*' org.webpieces.projects.ProjectCreator $@

