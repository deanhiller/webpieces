# webpieces

[![Build Status](https://travis-ci.org/deanhiller/webpieces.svg?branch=master)](https://travis-ci.org/deanhiller/webpieces)


Codecov.io / jacoco has two bugs (so we are actually way higher than this number) documented at bottom of this page
[![codecov](https://codecov.io/gh/deanhiller/webpieces/branch/master/graph/badge.svg)](https://codecov.io/gh/deanhiller/webpieces)

This 23 minute video barely scratches the surface but demonstrates
* Download zip and create first project
* run test suite
* start up the production server
* view main default page of templated app
* Visit the pluggable backend at http://localhost:8080/@backend
* importing into intellij
* starting up the DevelopmentServer
* Adding Routes
* Adding Controller
* Adding Entity Bean
* Adding CRUD operations, errors, persistence
* Quick example of back button WORKING
* Quick example of 'DevelopmentServer' not found page helping you debug faster
* Quite a few examples where I screw up to show how good the errors are
* Logging Error/Warn is done in RED, to help you not miss errors in your application
* development server only in-memory database GUI at http://localhost:8080/@db
* development server only in-memory documentation at http://localhost:8080/@documentation

#### BIG NOTE: There is 1 location in the video I had to restart the server ONLY because there are setting in intelij I forgot to setup
[Webpieces YouTube QuickStart](https://youtu.be/4FtdAX_wKD0)

One thing to note in the video is I was caught off guard by a minor bug(that is easily worked around) and had to restart the DevelopmentServer as for some reason, the hibernate rescan of entities and table creations did not work.  We may have that fixed by the time you watch the video(hopefully)

#### A few key selling points( More advantages in another section below as well)
* Contains a Backend Plugin which BackendPlugins plug into exposing new html pages, controllers, startup code all for that plugin
  * One example is the InstallCert plugin which allows you to manage the https cert and one click install
  * Another example will be a StatsBackendPlugin to manage stats
  * Another example will be a management plugin allowing you to modify properties(and you can stop using property files!!!!).  This is all saved to a database.
* The server automatically puts backpressure on clients when needed preventing clients from writing to their sockets during extreme load so the server never falls over
  * This feature is tough requiring the need for backpressure translation across our http2 parser, http1.1 parser, and the SSL encryption layer which is why nearly all webservers do not do this feature
* Run SingleSocketThroughput.java to see performance.  Well, on my small laptop at least and single threaded, 6 ,000,000 requests per minute(100,000 requests per second)
* Run IntegTestLocalhostThroughput.java to see MB throughput of re-usable NIO layer (On my machine it was 24 Gigabits/second for a single thread, single socket).  It is a library not a framework (I think of frameworks as inheritance and libraries as composition(ie. prefer composition over inheritance)).  netty is a framwork making it tougher to use.
* The http1.1 and http2 clients can backpressure the server as well (if a client backpressures webpieces server, the server will then backpressure to the client sending it through a chain all automatically).  Beware, some servers may close their socket on you or fall over if you backpressure them. You can turn backpressure off if desired.
* look ma, no restarting the server in development mode with complete java refactoring
* holy crap, my back button always works.  Developers are not even allowed to break that behavior as they are forced to make the back button work...#win
* Override ANY component in the platform server just by binding a subclass of the component(fully customizable server to the extreme unlike any server before it)
* SSL Cert wizard installs the certificate on ALL hosts in one go.  no more uploading certs to each host you maintain(what a pain that was)
* and sooooooo much more

#### 9 Steps to try the webserver (and view official documentation)

1. Download the release(https://github.com/deanhiller/webpieces/releases), unzip
2. run ./createProject.sh
3. cd {projectDir}-all
4. ./gradlew build # runs all the tests and verify everything is working. 
5. ./gradlew assembleDist  #creates the actual webserver distribution zip and tar files
6. cd {projectDir}-all/{projectDir}/output/distributions/
7. unzip {projectDir} which is your whole webserver
8. ./bin/{project} to start the production webserver
9. In a browser go to http://localhost:8080
10. To view the documentation, you need to start the development server in eclipse setup or intellij setup below

### Eclipse Setup

NOTE: last tested running eclipse on jdk-11.0.3.jdk Eclipse 2019-06 Version: 2019-06 (4.12.0) Build id: 20190614-1200
BIG NOTE: I was having huge troubles with Eclipse 2019-03 but it could have been my environment and I could not figure out what was wrong, so try 2019-06 if you have issues

1. (if not installed already) install eclipse gradle plugin - The buildship gradle plugin that you install into eclipse
   * Click Help menu -> Eclipse Marketplace...
   * Type in 'Gradle' in the Find text box
   * Click install on Buildship Gradle Integration 2.0
2. import project into eclipse using gradle
   * Click File menu -> Import...
   * Expand Gradle folder
   * Choose Existing Gradle Project and click Next
   * Click Next
   * Click Finish
3. eclipse buildship gradle plugin works except for passing in -parameters to the settings file like ./gradlew eclipse did so you have to do a few more steps here
   * Open eclipse preferences
   * Expand 'Java' and click 'Compiler' 
   * select a checkbox near the bottom that says 'Store information about method parameters'
4. From the IDE, expand {yourapp-all}/{yourapp}-dev/src/main/java/{yourpackage}
5. Run OR Debug the class named {YourApp}DevServer.java which compiles your code as it changes so you don't need to restart the webserver (even in debug mode)
6. In a browser go to http://localhost:8080
7. refactor your code like crazy and hit the website again(no restart needed)
8. As you upgrade, we just started(7/20/17) to have a legacy project we run the webpieces build against.  This means we HAVE to make upgrades to it to see how it affects clients.  You can copy the upgrades needed(some are not necessarily needed but recommended) here https://github.com/deanhiller/webpiecesexample-all/commits/master (We are going to try to standardize the comments better as well.
9. For Documentation go to http://localhost:8080/@documentation and you can access the references and tutorials

### Intellij Setup

In Intellij, you will have a bit more pain in the debugger(the debugger is not as nice as eclipse BUT the IDE usability is much better). 
NOTE: last tested on Intellij 2019.1

1. Import Project
   * From Welcome screen, choose Import Project
   * Select your folder {yourapp}-all and click ok
   * Choose 'Import project from external model' and choose gradle and click next
   * Even though gradle location is unknown, that is ok since 'use default gradle wrapper' is selected so click Finish
2. Modify compiling with parameters option(Intellij does not suck this setting in from gradle :( )
   * Open Preferences, expand "Build, Execution, and Deployment", 
   * expand 'Compiler', and click on 'Java Compiler'.  Add -parameters to the 'Additional Command Line Parameters'
   * Click Ok to close dialogue
   * Click Build menu and click Rebuild Project
3. Modify TWO auto-recompile settings documented here https://stackoverflow.com/questions/12744303/intellij-idea-java-classes-not-auto-compiling-on-save
4. From the IDE, expand {yourapp-all}/{yourapp}-dev/src/main/java/{yourpackage}
5. Run OR Debug the class named {YourApp}DevServer.java which compiles your code as it changes so you don't need to restart the webserver (even in debug mode)
6. In a browser go to http://localhost:8080
7. refactor your code like crazy and hit the website again(no restart needed)
8. As you upgrade, we just started(7/20/17) to have a legacy project we run the webpieces build against.  This means we HAVE to make upgrades to it to see how it affects clients.  You can copy the upgrades needed(some are not necessarily needed but recommended) here https://github.com/deanhiller/webpiecesexample-all/commits/master (We are going to try to standardize the comments better as well.
9. For Documentation go to http://localhost:8080/@documentation and you can access the references and tutorials

#### To try modifying/contributing to the actual webserver

1. clone webpieces
2. The automated build runs "./gradlew -Dorg.gradle.parallel=false -Dorg.gradle.configureondemand=false build -PexcludeSelenium=true -PexcludeH2Spec=true" as it can't run the selenium tests or H2Spec tests at this time
3. If you have selenium setup and h2spec, you can just run "./gradlew build" in which parallel=true and configureondemand=true so it's a faster build
4. debugging with eclipse works better than intellij.  intellij IDE support is better than eclipse(so pick your poison but it works in both)

I want to try something new on this project.  If you want something fixed, I will pair with you to fix it ramping up your knowledge while fixing the issue.  We do this with screenhero(remote desktop sharing and control)

A project containing all the web pieces (WITH apis) to create a web server (and an actual web server, and an actual http proxy and an http 1.1 client and an http2 client and an independent async http parser1.1 and independent http2 parser and a templating engine and an http router......getting the idea yet, self contained pieces).  This webserver is also made to be extremely 'Feature' Test Driven Development for web app developers such that tests can be written that will test all your filters, controllers, views, redirects and everything all together in one.  This gives GREAT whitebox QE type testing that can be done by the developer.  Don't write brittle low layer tests and instead write high layer tests that are less brittle then their fine grained counter parts (something many of us do at twitter).  

This project is essentially pieces that can be used to build any http related software and full stacks as well.  

#### Advantages of webpieces

* Over 370 customer facing tests(QA tests testing from customers point of view)
* Ability to backpressure to prevent clients from writing to their sockets during extreme load so the server never falls over
* Clients and the nio library uses also have backpressure mechanics that can be used to backpressure servers if desired(generally you should keep up with the server since you are a client though!!).  If you back pressure a webpieces client talking to a webpieces server, the server could start backpressuring your client requests as well
* holy crap, my back button always works.  Developers are not even allowed to break that behavior as they are forced to make the back button work...#win
* look ma, no restarting the server in development mode with complete java refactoring
* your project is automatically setup with code coverage (for java and the generated html groovy)
* LoginFilter automatically adds correct cache headers so if you are logged out, back button will not go back to some logged in page instead redirecting to login
* built in 'very loose' checkstyle such that developers don't create 70+ line methods or 700+ line files or nasty anti-arrow pattern if statements
* unlike Seam/JSF and heavyweight servers, you can slap down 1000+ of these as it is built for clustering and scale and being stateless!!! especially with noSQL databases.  with Seam/JSF, you lock your users to one node and when that goes out, if they are in the middle of buying a plane ticket, they are pretty much screwed.(ie. not a good design for large scale)
* be blown away with the optimistic locking pattern.  If your end users both post a change to the same entity, one will win and the other will go through a path of code where you can decide, 1. show the user his changes and the other users, 2. just tell the user it failed and to start over 3. let it overwrite the previous user code 
* prod server caches files using hash on content so all *.js files and *.css files are cached for a year and if file changes, the hash changes causing a reload
* dev server never tells browser to cache files so developer can modify file and not need to clear browser cache
* %[..]% will verify a file actually exists at that route at build time so that you do not accidentally deploy web pages that link to nonexistent files 
* no erasing users input from forms which many websites do....soooo annoying
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
* Tests now test for backwards compatibility so we(developers) do not accidentally break compatibility with your app except on major releases

#### Downsides

May have to Deal with Dean

#### Some HTTP/2 features
 * better pipelining of requests fixing head of line blocking problem
 * Server push - sending responses before requests even come based on the first page requests (pre-emptively send what you know they will need)
 * Data compression of HTTP headers
 * Multiplexing multiple requests over TCP connection

#### Pieces of Webpieces

There is a jpeg of these pieces and relationships in the http://localhost:8080/@documentation pages (on DevelopmentServer only)

 * channelmanager - a very thin layer on nio for speed(used instead of netty but with it's very clean api, anyone could plugin in any nio layer including netty!!!)
 * asyncserver - a thin wrapper on channelmanager to create a one call tcp server (http-frontend sits on top of this and the http parsers together)
 * http/http1_1-parser - An asynchronous http parser that can accept partial payloads (ie. nio payloads don't have full messages).  Can be used with ANY nio library.
 * http/http1_1-client - http1.1 client
 * http/http2-parser - An asynchronous http2 parser with all the advantages of no "head of line blocking" issues and pre-emptively sending responses, etc. etc.
 * http/http2-client - http 2 client built on above core components because you know if you server supports http2 AND noy doing 1.1 keeps it simple!!!
 * http/http2to1_1-client - http1.1 client with an http2 interface SOOOOOO http2-client and http2to1_1-client are swappable as they implement the same api
 * http/http-frontend - An very thin http webserver.  call frontEndMgr.createHttpServer(svrChanConfig, serverListener) with a listener and it just fires incoming web http server requests to your listener(webserver/http-webserver uses this piece for the front end and adds it's own http-router and templating engine)
 * webserver/http-webserver - a webserver with http2 and http1.1 support and tons of overriddable pieces via guice
 * core/runtimecompiler - create a runtime compiler with a list of source paths and then just use this to call compiler.getClass(String className) and it will automatically recompile when it needs to.  this is only used in the dev servers and is not on any production classpaths (unlike play 1.4.x)
