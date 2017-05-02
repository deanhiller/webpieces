# webpieces

[![Build Status](https://travis-ci.org/deanhiller/webpieces.svg?branch=master)](https://travis-ci.org/deanhiller/webpieces)

Codecov.io / jacoco has two bugs (so we are actually way higher than this number) documented at bottom of this page
[![codecov](https://codecov.io/gh/deanhiller/webpieces/branch/master/graph/badge.svg)](https://codecov.io/gh/deanhiller/webpieces)

#### Steps to try the webserver

1. Download the release(https://github.com/deanhiller/webpieces/releases), unzip
2. run ./createProject.sh
3. cd <projectDir>-all
4. ./gradlew build # runs all the tests and verify everything is working.  If you want the selenium test to work install firefox 47.0.1
5. ./gradlew assembleDist  #creates the actual webserver distribution zip and tar files
6. cd <projectDir>-all/<projectDir>/output/distributions/
7. unzip <projectDir> which is your whole webserver
8. ./bin/<procect> to start the production webserver
6. IF Eclipse, This part gets tricky since eclipse gradle plugin has a bug(and ./gradlew eclipse has a different bug :( )
    NOTE: tested out on Eclipse Neon 4.6.0 build id 20160613-1800 and gradle 2.14.1
  * eclipse gradle plugin - The buildship gradle plugin that you install into eclipse
       eclipse buildship gradle plugin works except for passing in -parameters to the settings file like ./gradlew eclipse did so you have to
       go to eclipse preferences and expand 'Java' and click 'Compiler' and select a checkbox near the bottom that says
       'Store information about method parameters'
  * gradle eclipse plugin - The plugin that runs with ./gradle eclipse (installed with apply 'eclipse' in gradle file)
       NOTE: ./gradlew eclipse does not work unless you delete the conflicting paths in .classpath file after generating it(gradle eclipse plugin bug)
6. IF Intellij, you will have a bit more pain in the debugger(it's not as stable as eclipse BUT the IDE usability is much better).  The first steps are to
  * From Welcome screen, choose Import Project
  * Select your folder {yourapp}-all and click ok
  * Choose 'Import project from external model' and choose gradle and click next
  * Even though gradle location is unknown, that is ok since 'use default gradle wrapper' is selected so click Finish
  * Open Preferences, expand "Build, Execution, and Deployment", expand 'Compiler', and click on 'Java Compiler'.  Add -parameters to the 'Additional Command Line Parameters'
7. From the IDE, expand {yourapp-all}/{yourapp}-dev/src/main/java/{yourpackage}
8. Run OR Debug the class named {YourApp}DevServer.java which compiles your code as it changes so you don't need to restart
     the webserver (even in debug mode)
  * today 4/27/17 newest intellij introduced a bug where dev server fails to start as src/main/resources is not on classpath
9. In a browser go to http://localhost:8080
10. refactor your code like crazy and hit the website again(no restart needed)

#### To try modifying/contributing to the actual webserver

1. clone webpieces
2. The automated build runs "./gradlew -Dorg.gradle.parallel=false -Dorg.gradle.configureondemand=false build -PexcludeSelenium=true -PexcludeH2Spec=true" as it can't run the selenium tests or H2Spec tests at this time
3. If you have selenium setup and h2spec, you can just run "./gradlew build" in which parallel=true and configureondemand=true so it's a faster build
4. debugging with eclipse works better than intellij.  intellij IDE support is better than eclipse(so pick your poison but it works in both)

I want to try something new on this project.  If you want something fixed, I will pair with you to fix it ramping up your knowledge while fixing the issue.  We do this with screenhero(remote desktop sharing and control)

A project containing all the web pieces (WITH apis) to create a web server (and an actual web server, and an actual http proxy and an http client and an independent async http parser1.1 and independent http parser2 and a templating engine and an http router......getting the idea yet, self contained pieces).  This webserver is also made to be extremely 'Feature' Test Driven Development for web app developers such that tests can be written that will test all your filters, controllers, views, redirects and everything all together in one for GREAT whitebox QE type testing that can be done by the developer.  Don't write brittle low layer tests and instead write high layer tests that are less brittle then their fine grained counter parts (something many of us do at twitter).  

This project is essentially pieces that can be used to build any http related software and full stacks as well.  

#### Advantages of webpieces

* LoginFilter automatically adds correct cache headers so if you are logged out, back button will not go back to some logged in page instead redirecting to login
* your project is automatically setup with code coverage (for java and the generated html groovy)
* built in 'very loose' checkstyle such that developers don't create 70+ line methods or 700+ line files or nasty anti-arrow pattern if statements
* unlike Seam/JSF and heavyweight servers, you can slap down 1000+ of these as it is built for clustering and scale and being stateless!!! especially with noSQL databases.  with Seam/JSF, you lock your users to one node and when that goes out, if they are in the middle of buying a plane ticket, they are pretty much screwed.(ie. not a good design for large scale)
* be blown away with the optimistic locking pattern.  If your end users both post a change to the same entity, one will win and the other will go through a path of code where you can decide, 1. show the user his changes and the other users, 2. just tell the user it failed and to start over 3. let it overwrite the previous user code 
* look ma, no restarting the server in development mode with complete java refactoring
* prod server caches files using hash on content so all *.js files and *.css files are cached for a year and if file changes, the hash changes causing a reload
* dev server never tells browser to cache files so developer can modify file and not need to clear browser cache
* %[..]% will verify a file actually exists at that route at build time so that you do not accidentally deploy web pages that link to nonexistent files 
* no erasing users input from forms which many websites do....soooo annoying
* holy crap, my back button always works.  Developers are not even allowed to break that behavior as they are forced to make the back button work...#win
* one liner for declaring a form field which does keeps users input for you, as well as i18n as well as error handling and decorating ALL your fields with your declared field template
* custom tags can be created in any myhtml.tag file to be re-used very easily(much like playframework 1.3.x+)
* production server does not contain a compiler (this was a mistake I believe in the play 1.3.x+ framework)
* production server creates a compressed static file cache on startup and serves pre-compressed files(avoiding on-demand compression)
  * later, we may even have a cache in memory so we don't hit disk at all
* production server has no need to compile templates as they are precompiled in production mode which increases speed for end users
* You should find, we were so anal, we cover way more developer mistakes and way more error messages on what the developer did wrong so they don't have to wonder why something is not working and waste time.  
* Override ANY component in your web application for testing to mock remote endpoints and tests can simulate those
* Override ANY component in the platform server just by binding a subclass of the component(fully customizable server to the extreme unlike any server before it)
* Debug one of the tests after creating the example project and you step right into the platform code making it easier to quickly understand the underlying platform you are using and how componentized it is. (if you happen to run into a bug, this makes it way easier to look into, but of course, we never run into bugs with 3rd party software, right, so you won't need this one) That was in my sarcastic font
* Selenium test case provided as part of the template so skip setting it up except for getting the right firefox version
* Route files are not in yml but are in java so you can have for loops, dynamic routes and anything you can dream up related to http routing
* Full form support for arrays(which is hard and play1.3.x+ never got it right to make it dead simple..let's not even talk about J2EE )
* Protects developers from the frequent caching css/js/html files screwup!!! This is bigger than people realize until they get bitten.  ie. you should not change any static js/css files without also renaming them so that the browser cache is avoided and it loads the new one as soon as a new version is deployed.  Nearly all webservers just let developers screw this up and then customers wonder why things are not working(and it's only specific customers that have old versions that complain making it harder to pinpoint the issue).  Finally, you can live in a world where this is fixed!!!
* supports multiple domains over SSL with multiple certificates but only for advanced users
* JPA/hibernate plugin with filter all setup/working so if your backend is a database, you can crank out db models.  NoSQL scales better but for startups, start simple with a database
* NoSql works AMAZINGLY when using nosql asynch clients as this server supports complete async controllers
* CRUD in routemodules can be done with one method call to create all routes(list, edit and add, post, and delete) or you can vary it with a subset easily
* no session timeout on login EVER(unlike JSF and seam frameworks)
* going to a secure page can redirect you to login and once logged in automatically redirect you back to your original page
* Security - cookie is hashed so can't be modified without failing next request
* Security - Form auth token in play1.3.x+  can be accidentally missed leaving security hole unless app developer is diligent.  By default, we make it near impossible to miss the auth token AND check that token in forms for the developer(putting it in is automatic in play 1.3 but checking it is not leaving a hole if you don't know and many don't know)
* State per tab rather than just per session.  All web frameworks have a location to store session state but if you go to buy a plane ticket in 3 different tabs, the three tabs can step on each other.  A location to store information for each tab is needed

#### Downsides
  Currently documentation is lacking but there is an example for pretty much everything in webpieces/webserver/http-webserver/src/test/java/* as we do something called feature testing(testing as user would use the system) for all paths of code.

#### Some HTTP/2 features
 * better pipelining of requests fixing head of line blocking problem
 * Server push - sending responses before requests even come based on the first page requests (pre-emptively send what you know they will need)
 * Data compression of HTTP headers
 * Multiplexing multiple requests over TCP connection

#### Pieces of Webpieces
 * channelmanager - a very thin layer on nio for speed(used instead of netty but with it's very clean api, anyone could plugin in any nio layer including netty!!!)
 * asyncserver - a thin wrapper on channelmanager to create a one call tcp server (http-frontend sits on top of this and the http parsers together)
 * http/http1_1-parser - An asynchronous http parser that can accept partial payloads (ie. nio payloads don't have full messages).  Can be used with ANY nio library.
 * http/http1_1-client - http1.1 client
 * http/http2-parser - An asynchronous http2 parser with all the advantages of no "head of line blocking" issues and pre-emptively sending responses, etc. etc.
 * http/http2-client - http 2 client built on above core components because you know if you server supports http2 AND noy doing 1.1 keeps it simple!!!
 * http/http-frontend - An very thin http library.  call frontEndMgr.createHttpServer(svrChanConfig, serverListener) with a listener and it just fires incoming web http server requests to your listener(webserver/http-webserver uses this piece for the front end)
 * webserver/http-webserver - a webserver with http 2 support and tons of overriddable pieces via guice
 * core/runtimecompiler - create a compiler with a list of source paths and then just use this to call compiler.getClass(String className) and it will automatically recompile when it needs to.  this is only used in the dev servers and is not on any production classpaths (unlike play 1.4.x)

#### TODO:
* start an actual multi-homed project
* add more and more tag examples
* move examples to @examples url instead
* write http2 tests for server
* write http2 tests for client
* flow control test cases on http client
* error test cases http2 client
* flow control server test?  client may cover that same code
* error test cases on server http2 and then try H2Test
* tests on network outage during ajax calls to make that even cooler
* tests on whitespace issues on tags and formatting so we can isolate the differences
* verify upload file can work http2,etc
* http1.1 protect pipeline errors with tests
* max concurent streams is 50 right now for safety ...need to rework that 
* java tmp locations seem to be deleted after a while.  research this so tests dont' fail(perhaps touch the files each build so all files have same timestamp)
* deal with line '                    if(payloadLength > settings.get(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE)'
* more fully integrate the http2 stack. ie. finish and use http-frontend2 instead of http-frontend
* add optimistic locking test case for hibernate plugin with example in webapp and feature tests as examples as well
* implement Upgrade-Insecure-Requests where if server has SSL enabled, we redirect all pages to ssl
* response headers to add - X-Frame-Options (add in consumer webapp so can be changed), Keep-Alive with timeout?, Expires -1 (http/google.com), Content-Range(range requests)
* Database session state plugin tab state as well?
* Tab state (rather than just global session, but per tab state to put data)
* Metrics/Stats - Need a library to record stats(for graphing) that can record 99 percentile latency(not just average) per controller method as well as stats for many other things as well
* A/B split testing and experiments - hooks to wire into existing system and hooks to make it easier to create A/B pages
* Management - Need to do more than just integrate with JMX but also tie it to a datastore interface that is pluggable such that as JMX properties are changed, they are written into the database so changes persist (ie. no need for property files anymore except for initial db connection)
* google protobuf BodyContentBinder plugin
* thrift BodyContentBinder plugin
* plugin for localhost:8080/@documentation and install on the development server
* streaming forever from controller
* streaming upload but forever into controller
* dev server - when a 404 occurs, list the RouterModule scope found and then the all the routes in that scope since none of them matched
* codecov.io - still reports incorrect coverage results (different from jacoco)
* question out on jacoco code coverage for groovy files (code coverage works but linking to groovy files is not working for some reason)
* playing with channel manager, add testing back maybe from legacy version? OR maybe asyncserver project
* turning the server into a protocol server(with http2, there is no more need for protocol servers...all protocols work over http2 if you own the client and webserver like we do above)
* (no webserver on the planet does this, but with advent of http2 probably don't need this) eventually do 5.0 version where CompletableFuture<...> is returned from all incomingData calls and we load xxxx bytes but backpressure until more bytes released from acking futures....this is VERY difficult to do through the encryption layer, http1.1 parser, and http2 parser, but alleviates slow attacks in an easier way and http2 never needed connection flow ctrl as webservers could have done this  

#### Examples.....

```
${user.account.address}$
*{ comment ${user.account.address}$ is not executed }*
&{'This is account %1', 'i18nkey', user.account.name}&  // Default text, key, arguments
%{  user = SomeLogic.getUser(); }%
#{if user}#User does exist#{/if}#{elseif}#User does not exist#{/if}#
@[ROUTE_ID, user:account.user.name, arg:'flag']@
@@[ROUTE_ID, user:account.user.name, arg:'flag']@@
```

The last two are special and can be used between tag tokens and between i18n tokens like so...

#### @documentation Notes:

* Section on links to tests/html files as examples
* Section on Generator Tags and RuntimeTags and html.tag files
* Section on object to string and string to object bindings
* Section on overriding platform
* Section on overriding web application classes
* Section on i18n (need to explain, do NOT define message.properties since there is a list of Locales and that would create a match on any language)
* Section on escaping html and not escaping html (variable names with _xxx are not escaped) and the verbatim or noescape tag
* Section on testing
* Section on field tag and how to create more of these as your own
* Section on variable scopes... tag arguments, template properties and page arguments (how template props are global)
* Section on PRG pattern (point to flash/Validation)
* Section on Arrays and array based forms
* Section on tab state vs. session vs. flash (Validation, Flash)
* Section on filters
* don't forget special things like optional('property') to make page args optional and _variable to escape being html escaped
* resource bundles are complex needing to provide one for some tags if there is a provider of tags
* unit test query param conflict with multipart, query param conflict with path param, and multipart param conflict with path param. specifically createTree stuff PAramNode, etc.


#### Checklist of Release testing (This would be good to automate)
* ./runAllTesting.sh
* cd webserver/output/mytest-all/mytest/output/distributions/mytest
* run ./bin/mytest
* hit http://localhost:8080 and click around

* import into eclipse or intellij  
 (There used to be 2 ways to import into eclipse and 2 ways to import in intellij but now instead always just use gradle plugin as generator
  has some corner case bugs for our gradle configuration)
* open up project {appname}-dev and go into src/main/java and run the dev server
* hit the webpage
* refactor a bunch of code
* hit the webpage (no need to stop dev server) 

codecov.io bugs
* we have a test in project 2 that covers code in project 1.  That code shows as not being covered in red but is the only way to truly test that code. 
* in the rollup of stats, they incorrectly assume projects are independent so if 1 projed is 0 out of 1000 lines tested WITHIN that project BUT is 100% tested from another project, they ding you big and we are based on feature testing so we get dinged alot
