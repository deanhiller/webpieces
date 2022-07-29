# webpieces-example

You are reading this file from 1 of two locations.
 * On your own computer or your own repo from a generated webpieces project
 * From the legacy project (forcing webpieces devs to incur upgrade pain and leaving you an upgrade breadcrumb trail)

## Legacy Project
  The Legacy project is located at https://github.com/deanhiller/webpiecesexample-all 
  
  Every time we release webpieces that has breaking changes, we MUST ALSO upgrade this project. In fact, if we don't,
  the webpieces build doesn't work.

  This means, the history of the legacy project documents the evolution of breaking changes so you can review
  and make the same upgrades IF we do make breaking changes.  This makes it easier on people that 
  upgrade to check the git history and compare

## Your own project

Example project for using the webserver part of webpieces

Generated from project https://github.com/deanhiller/webpieces/releases

./gradlew test - run all tests excluding tests with @Ignore like the selenium one
./gradlew assembleDist - creates a distribution as a zip and tar format

./gradlew tasks - lists some of the available targets like the ones above