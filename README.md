# webpieces

To try the webserver

1. Download the release(https://github.com/deanhiller/webpieces/releases), unzip
2. run ./createProject.sh
3. cd projectDir
4. ./gradlew test # runs all the tests and verify everything is working
5. ./gradlew assembleDist  #creates the actual webserver distribution zip and tar files
6. ./gradlew eclipse or ./gradlew idea to generate eclipse or idea project
7. Run the dev server which compiles your code as it changes so you don't need to restart the webserver

A project containing all the web pieces (WITH apis) to create a web server (and an actual web server, and an actual http proxy and an http client and an independent async http parser1.1 and independent http parser2 and a templating engine and an http router......getting the idea yet, self contained pieces).  This webserver is also made to be extremely Test Driven Development for web app developers such that tests can be written that will test all your filters, controllers, views, redirects and everything all together in one for GREAT whitebox QE type testing that can be done by the developer.  Don't write brittle low layer tests and instead write high layer tests that are less brittle then their fine grained counter parts (something many of us do at twitter).  

This project is essentially pieces that can be used to build any http related software and full stacks as well.  

Some HTTP/2 features
 * better pipelining of requests fixing head of line blocking problem
 * Server push - sending responses before requests even come based on the first page requests (pre-emptively send what you know they will need)
 * Data compression of HTTP headers
 * Multiplexing multiple requests over TCP connection

Pieces
 * embeddablehttpproxy - a proxy with http 2 support
 * webserver/http-webserver - a webserver with http 2 support
 * http/http-client - An http client built on channelmanager and http parser
 * http/http-frontend - An very thin http library.  call frontEndMgr.createHttpServer(svrChanConfig, serverListener) with a listener and it just fires incoming web http server requests to your listener(webserver/http-webserver uses this piece for the front end)
 * core/runtimecompiler - create a compiler with a list of source paths and then just use this to call compiler.getClass(String className) and it will automatically recompile when it needs to.  this is only used in the dev servers and is not on any production classpaths
 * channelmanager - a very thin layer on nio for speed
 * asyncserver - a thin wrapper on channelmanager to create a one call tcp server (http-frontend sits on top of this and the http parsers)
 * http/http-parser1_1 - an asynchronous http parser than can accept partial payloads (ie. nio payloads don't have full message).  Can be used with ANY nio library.
 * httpclient - http client built on above core components
 * embeddablehttpproxy - build on http-frontend and http client

TODO: 
* version 1.9-developer showing up in bootstrap.js file :( so need to fix templating there
* Solve -parameters not working into eclipse and intellij?  
* Redo gradle to only use resources from src/main/java and src/test/java and screw resource directories as the plugins do not handle it well???  in eclipse, must do ./gradle eclipse then gradle plugin as they each have weird issues....try to solve this
* go see if it works in intellij again
* Test out template compression with very large template.  unit test!!!!! and uncompress on receive and verify!!
* compression is about streaming it through the gzip outputstream.  now need to decide how/when to cache files on filesystem and compress them all on startup and use timestamp as caching mechanism so new files modify cache and finally extension type list as well file!!!!
* 
* fix this...ERROR 2016-08-30 18:41:58,568 [-] [httpFrontEnd7]: This webserver has not thought about supporting header=Cache-Control quite yet.  value=max-age=0 Please let us know and we can quickly add support
* compression should be done by file extension type html, yes, txt, yes, jpg no, etc. and we should whitelist compressed files...enable once we get compression working again
* SSL 
* implement Upgrade-Insecure-Requests where if server has SSL enabled, we redirect all pages to ssl
* implement error, errorClass, errors, ifError, ifErrors, jsAction, jsRoute, option, select,
* catch-all route with POST as in /{controller}/{action}   {controller}.post{action}
* Need to test theory of a theme can be a unique controllers/views set AND then many unique views on that set.  a theme does not just have to be look but the controller as well possibly
* response headers to add - X-Frame-Options (add in consumer webapp so can be changed), Keep-Alive with timeout?, Content-Encoding gzip, Transfer-Encoding chunked, Cache-Control, Expires -1 (http/google.com), Content-Range(range requests)
* CRUD - create re-usable CRUD routes in a scoped re-usable routerModule vs. global POST route as well?
* Metrics/Stats - Need a library to record stats(for graphing) that can record 99 percentile latency(not just average) per controller method as well as stats for many other things as well
* Management - Need to do more than just integrate with JMX but also tie it to a datastore interface that is pluggable such that as JMX properties are changed, they are written into the database so changes persist (ie. no need for property files anymore except for initial db connection)
* bring back Hotswap for the dev server ONCE the projectTemplate is complete and we are generating projects SUCH that we can add a startup target that adds the Hotswap agent propertly
* have the dev server display it's OWN 404 page and then in a frame below dispay the webapps actual 404 page.  The dev server's page will have much more detail on what went wrong and why it was a 404 like type translation, etc.  The frame can be a redirect to GET the 404 page directly OR it could render inline maybe.....which one is better..not sure?  rendering inline is probably better so the notFound does not have a direct url to get to that page?  But only if the PRG holds true above!!!!
* write an escapehtml tag

* ALPN is next!!!! 

* start createing a real website!!!! AND on https


Examples.....

${user.account.address}$
*{ comment ${user.account.address}$ is not executed }*
&{'This is account %1', 'i18nkey', user.account.name}&  // Default text, key, arguments
%{  user = SomeLogic.getUser(); }%
#{if user}#User does exist#{/if}#{elseif}#User does not exist#{/if}#
@[ROUTE_ID, user:account.user.name, arg:'flag']@
@@[ROUTE_ID, user:account.user.name, arg:'flag']@@

The last two are specia and can be used between tag tokens and between i18n tokens like so...
 
In an href tag..                                                  #{a href:@[ROUTE, user:user, arg:'flag']@}#Some Link#{/a}# 
In text..                                                This is some text @[ROUTE, user:user, arg:'flag']@
In basic i18n tag                    &{'Hi, this link is text %1', 'key1', @[ROUTE, user:user, arg:'flag']@}&
In i18n tag...    &{'Hi %1, <a href="%2">Some link text</a>', 'key', arg1, @[ROUTE, user:user, arg:'flag']@}&

generates.....
__getMessage(args)



DOCUMENTATION Notes:

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


Checklist of Release testing (This would be good to automate)
* ./gradle release # release locally
*  cd webserver/output/webpiecesServerBuilder
* ./createProject.sh
* cd {app directory}
* ./gradlew test # verify all tests pass as they should because they did when running ./gradlew release(though the environment differs just slightly)
* ./gradlew assembleDist
* cd {appname}-prod/output/distributions
* unzip zip file
* cd {appname}-prod/bin
* run {appname}-prod script
* hit http://localhost:8080 and then click the link

* import into eclipse or intellij
* open up project {appname}-dev and go into src/main/java and run the dev server
* hit the webpage
* refactor a bunch of code
* hit the webpage (no need to stop dev server) 

I like things to work seamlessly but user.dir is a huge issue in multiple environments I am working in.
main server has to work in N configurations that should be tested and intellij is a PITA since 
it is inconsistent.  PP means runs in the project the file is in and eclipse is consistent with
gradle while intellij is only half the time....

app dev - The environment when you generate a project and import it into an IDE
webpieces - The environment where you test the template directly when bringing in webpieces into an IDE

* app dev / eclipse -
   * PP - running myapp-prod/src/tests/java - user.dir=myapp/myapp-prod
   * PP - SemiProductionServer - user.dir=myapp/myapp-dev
   * PP - DevServer - user.dir=myapp/myapp-dev
   * PP - ProdServer - user.dir=myapp/myapp-prod
* app dev / intellij (it's different paths than eclipse :( ).  user.dir starts as myapp directory
   * PP - running tests - myapp/myapp-prod
   * NO - SemiProductionServer - user.dir=myapp
   * NO - DevServer - user.dir=myapp :( what the hell!  different from running tests
   * NO - ProdServer - user.dir=myapp
* webpieces / eclipse - should be same as app dev I think
* webpieces / intellij - should be same as app dev I think
* PP - tests in webpieces gradle - myapp/myapp-prod
* PP - tests in myapp's gradle run - myapp/myapp-prod
* NO - production - user.dir=from distribution myapp-prod directory which has subdirs bin, lib, config, public
* Future? - run DevSvr,SemiProdSvr,ProdSvr from gradle?....screw that for now..it's easy to run from IDE so why bother

* so in production, the relative paths work from myapp so 'public/' is a valid location for html files resolving to myapp/public
* in testing, IF we want myapp/myapp-prod/src/dist/public involved, it would be best to run from myapp/myapp-prod/src/dist so 'public/' is still a valid location
* in devserver, semiprodserver, and prod server, the same idea follows where myapp/myapp-prod/src/dist should be the user.dir!!!

//sooooo, algorithm is this
//if user.dir=myapp, modify user.dir to myapp/myapp-prod/src/dist (you are in intellij)
//else if myapp-prod has directories bin, lib, config, public, then do nothing
//else modify user.dir=myapp-prod to myapp-prod/src/dist
