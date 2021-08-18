# webpieces

ONLY THESE BIG ITEMS LEFT
* HIGH PRIORITY - somehow most tests were moved into core-channelmanager2/test instead of src/test/java and need to be moved back
* get the runAllTestingLocalRelease.sh ported into gradle - Alex Sweeney working on
* get the webpeices template generators working
* fix SSL tests to use TLS1.3 or in circleCI get javax.net.ssl.SSLHandshakeException: No available authentication scheme
   * TestSslBasicClient.java
   * TestSslCloseClient.java
   * TestSslCloseSvr.java
   * TestSslBasicSvr.java
* cannot run tests from core/core-channelmanager2 nore core/core-asyncserver in INTELLIJ!!!!! i
   * switch to intellij builds and intellij run tests and I can run the test now
* get version numbers on the plugins html compiler to be dynamic using the cmd line arg so we can release
* get the publish working again so we can publish a real release to remote repository
* get a new context that is portable among platforms into the repo as well
* get GCPStorage ported as a first try according to the requirements
* try doing this in our template compiler plugin https://discuss.gradle.org/t/can-anyone-explain-the-javaplugin-source-code/40744 as well as copying the resources into a different directory that is NOT used by the build!!!!!!  In this way, tests work, dev server works, but the *.html files are not put in production!!
* Move ClientImp's and server connectors into webpieces
* XFuture so context issue is solve
* conversations
* ALPN
* IN PROGRESS - streaming filters?
* PLUGIN - Change logging levels in production/development via https://host:port/@logging
* PLUGIN - metrics GUI plugin at https://host:port/@metrics
* PLUGIN - code generation at https://host:port/@codegen
   * website CRUD using all DTOs and client(microservices)
   * website CURD using all DBOs
   * JSON CRUD with DTOs and DBOs
* PLUGIN - GRPC
* swap all DataWrapper with 'http ok client' ByteBuffer pool thingy?  Optimizes stuff more
* microservices 
   * microsvc-client project with Client Creator project -> port work from orderly
   * microsvc-server project with Routes file and filter -> port work from orderly
* port newline streaming lib and json lib and file upload lib into webpieces router api
* PLUGIN - sync with remote Docker system by linking first at https://host:port/@remote
   * In this case, once linked, any changes to your DevServer occur in Docker container remotely in the cloud
   * This means NO MORE 5-10 minute uploads from home

* fix up calling Actions flash and redirect methods.  Use 'password' from the filter to ignore fields and such and make it easier.
* https://intellij-support.jetbrains.com/hc/en-us/requests/2671257?flash_digest=1cd06958f5f8678e6121ed726d13518a88a7b5aa&page=1
* https://intellij-support.jetbrains.com/hc/en-us/community/posts/360008329760-Is-there-a-global-setting-for-Build-project-Automatically-and-JVM-args-like-parameters-
* Perhaps have json filter set flag for dev server to not show not found page since it's confusing?....should we return not found json?  perhaps?  Plain text would be BEST
  * OR OR we can just for ALL Content RouteType display plain text pages that tell the routes not found!!  perhaps we can ctrl escape colors and stuff too?
* test out intellij and swapping jars....does it swap the classes in those jars or what happens?
* refreshing DevServer internalError.html AFTER compiler error kinda sucks!! ..can we save that page somehow but it's tough in case they make changes?
* Follow up on this post https://stackoverflow.com/questions/62246462/jacoco-not-excluding-files-and-print-not-working-in-jacocotestreport-task-to-deb
* get rid of close connection on response handler!!
* https://github.com/eclipse/buildship/issues/995
* Need a test case for stream to server controller directly to client back to some other controller endpoint (remote test or at least test with parsing!!! as server gets HttpData and passes to client but client
previously was not able to use HttpData)
* send in http request with no chunked transfer but send chunks and things go nasty.  can we say something better maybe?  can we fail better?...on the client way out, we should fail sending any chunks!!!
* Add regex for e.printStackTrace into checkstyle.  Add System.out and System.err as well to not be used and log why
* implement this post https://github.com/diffplug/spotless to organize imports and format code?   
* https://stackoverflow.com/questions/61714407/how-to-enforce-this-pattern-via-gradle-plugins
* For microservice, where NotFoundRoute is not specified, We should throw NoNotFoundRouteSetException
* ADD precondition to not allow developers to accidentally run a Routes file twice which adds the same routes and can fail in a confusing way.
* ResponseOverrideSender is in wrong location...needs to be in top top level proxy of streamhandling(after chunking/compression)
* wire futures backpressure all the way in server and test for async SSL engine to make it more robust!!
* Document prod/staging/local development environments and properties plugin
* Document more examples on the examples page stuff
* Document the ApplicationContext
* http1 parser, write directly to a ByteBuffer[] from the pool with https://stackoverflow.com/questions/1252468/java-converting-string-to-and-from-bytebuffer-and-associated-problems
* Figure out In what case can this ever happen in?(we need a reproducible case). In the meantime, restarting your Dev server fixes this.
* on building up and up the Routers, we SHOULD use guice on startup since we don't need to be EXTREMELY fast on startup AND I could have more easily fixed a bug....I think the tradeoff is there
* test converters for all cases listed in ObjectTranslator.java!!
* test all reverse a URL paths...from redirect as well as from a page
* add method to reverse a URL from the controller using the Action Enum
* Test -> Make it so AFTER you hit the route, '*Managed', works and ends up in the webpage AFTER GET request is made to that url that uses the property
* GET request with no url params nor query params BUT controller method DOES have params results in NotFound which is just weird!!! throw a 500 bug instead and put info in exception
* Need webpieces DevServer test for modifying default argument.  It turns out, arguments are not cleared on restart and something weird goes on.
* Do we need to file chrome ticket OR do we have cache settings wrong?  on internet outage post, user data is blown away.  we need to figure this out
* status on https://discuss.gradle.org/t/intellij-gradle-build-vs-cmdline-build/34761 and https://intellij-support.jetbrains.com/hc/en-us/requests/2474304
* jacoco posts upgrade to bounty...
   * https://discuss.gradle.org/t/merge-jacoco-coverage-reports-for-multiproject-setups/12100/14
   * https://stackoverflow.com/questions/60017758/gradle-5-3-to-5-6-upgrade-and-jacoco-breaks-because-dofirst-and-findall-no-longe
   * https://discuss.gradle.org/t/gradle-upgrade-to-5-6-broke-since-new-gradle-is-claiming-old-dirs-to-be-subprojects/34764
   * https://gist.github.com/aalmiray/e6f54aa4b3803be0bcac
* fix using personal bintray repository deanhiller/maven into using maven central https://stackoverflow.com/questions/60012519/bintray-creating-directories-claims-to-upload-artificats-but-its-blank
* be able to publish gradle compile html plugin AND it's dependencies https://stackoverflow.com/questions/60017878/how-to-publishing-one-subproject-and-its-dependencies-only
* implement proper evaluation of code coverage found here https://discuss.gradle.org/t/jacoco-plugin-with-multi-project-builds/22219
* copy/paste clipboard https://github.com/zeroclipboard/zeroclipboard
* https://css-tricks.com/native-browser-copy-clipboard/
* Add test for CRUD add and add test for CRUD edit to make sure hibernate is always working 100% regarding persist or merge method so we don't get burned on upgrades
* Add test case for json being fed into Development Server in webpieces only since we broke that on accident with the BodyContentBinder
* Add test case, run ./gradlew assembleDist on webpieces, THEN, modify an html file, modify again and make sure the modificaiton/addition shows up on webpage.  We ran into a situation on upgrade where we have to clean our project each time!!!
* 2 webpiecesCache locations are NOT based on the base directory modification and probably should be.  all dirs should base themselves on modified where to run 'I think'
* need a timeout on TransactionFilter hibernate so we don't leave transactions open!!!

add a bunch of beans now that properties file stuff is working? (concentrate on exposing channelmanager properties?)

Think about testing compiling and scaling on LARGE(many many classes) application(and how to test??).  can we separate out slivers so DEV environment
stays fast no matter what!!!!

I still REALLY don't like 5xx on posts as the url then stays on the post url so a redirect would be best on 5xx on post.  ie. back button stops working I think when we encounter a post bug(ick)

* (6/27)TEST WINDOWS NOW...should be fixed with ... fix https://discuss.gradle.org/t/i18n-issue-with-chinese-directly-in-java-string-on-windows-mac-is-fine/32216

attack this issue
https://discuss.gradle.org/t/gradle-5-3-1-jdk-11-yields-new-module-not-found-error/31567/3


* Documentation on EACH html field
  * basic select with enum
  * basic select with list from entity
  * radio buttons
  * other?

Property Management plugin!!!
* add checkbox for true/false
* add pulldown for enum

* Implement CRUD with paging ability before code generation wizard

* Documentation on plugin properties
* Documentation on EVERY single html widget with inline examples!!!

* respond in eclipse bug report   https://bugs.eclipse.org/bugs/show_bug.cgi?id=548792!

ADD release failure if not calling release.sh under jdk8 for now so we don't accidentally fuck up when switching jdks
https://discuss.gradle.org/t/gradle-5-3-1-jdk-11-yields-new-module-not-found-error/31567/2

bump this bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=548792
bump this bug https://bugreport.java.com/bugreport/submit_start.do
https://jira.qos.ch/browse/LOGBACK-1474 - GEventEvaluator
  blocked on https://lists.apache.org/thread.html/17c553e64b1afb674e2f10f978e366b2b7662319a0c991aa46551241@%3Cusers.groovy.apache.org%3E
https://jira.qos.ch/browse/LOGBACK-1473 - %caller not working for lambdas

//create the documentation on...
phase 1: Server.java will call svr = WebServerFactory.create
           //args from phase 1 created!!! (module creation creates args)
           //unlike webapp modules, args canNOT be used in the module @Provides methods or configure method
phase 2: Server.java will call svr.configure() calls routerService.configure()
           //everything but running startup beans is done
phase 3: Server.java arguments.check done here
phase 4: Server.java will call svr.start() which calls routerSvc.start
           //startup beans run, so injector.getInstance is run so modules may read properties
phase 5: svr.start() also starts reading properties next from phase 1

#### TODO:
* @documentation, need threading section
* @documentation, need section on injecting ScheduledExecutor (hmmmmmmmm, who provides this?)
* Rewire SimpleStorage to be injected in Server.java class instead and passed to router to be bound so there is no weird callback anymore!!!
* debug http client backpressure on downloading from website fun
* add non-selenium test for AJAX CRUD POST form when logged out and then login and then render
* add test for %{..}% to set var and %{..}% to print var to verify it is there so groovy implant is working
* rewire redirects so port stuff is done OUTSIDE the router so we no longer need funky callbacks.  (ie. I made a mistake there that we can fix by providing all the info from the router to the webserver and webserver knows the ports).  This is more complex than that!  
* add test for java enum and list select and multiselect
* add test for java related entity select and multiselect
* on client sending close to us, should we allow 2 seconds before we start throwing exceptions...seems reasonable actually as there WILL be race conditions with apps writing back to clients as clients close but we notify them and they should process that notification in 2 seconds (unless under severe load but at that point, not sure anything is a good solution)

Error route is very complex.  We must deal with these situations (write a test for each one!!!)
1. html POST - in this case, controller must redirect to keep with PRG or deliver weird back button stuff
2. ajax POST - not sure….if this fails, we should deliver an ajax redirect on behalf of the user I ‘think’ but some ajax stuff can’t deal with it
3. html GET - in this case, we can redirect or render but rendering would be delivering an error to the url the user accessed(is that ok)
4. ajax GET - in this case, delivering a error page would be really really weird
5. json GET/POST - I think the json filters should handle this situation
6. Should our platform deliver an ajax redirect on behalf of the user?? in which case the user needs to tell us his desired url path for errors

NotFound is complex as well
   1. Json not found ends up in html not found logic (the filter passes back not found code with json but processor still has to deal with it)
   2. when redirecting, you can end up in not found??  or at least not found processor gets a Redirect command (seems weird and shouldn’t happen)?

From Guice….
WARNING: Please consider reporting this to the maintainers of com.google.inject.internal.cglib.core.$ReflectUtils$1
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release

upgrade groovy…
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.codehaus.groovy.vmplugin.v7.Java7$1 (file:/Users/dhiller/.gradle/caches/modules-2/files-2.1/org.codehaus.groovy/groovy-all/2.4.6/478feadca929a946b2f1fb962bb2179264759821/groovy-all-2.4.6.jar) to constructor java.lang.invoke.MethodHandles$Lookup(java.lang.Class,int)
WARNING: Please consider reporting this to the maintainers of org.codehaus.groovy.vmplugin.v7.Java7$1
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release


ADD TEST CASE FOR THIS… to make sure we don’t try to go to compressed cache?

                        if(meta.getRoute().getRouteType() == RouteType.STATIC) {
				//RESET the encodings to known so we don't try to go the compressed cache which doesn't
				//exist in dev server since we want the latest files always
				ctx.getRequest().encodings = new ArrayList<>();
			} else if(meta.getControllerInstance() == null) {
				finder.loadControllerIntoMetaObject(meta, false);
				finder.loadFiltersIntoMeta(meta, meta.getFilters(), false);
			}

THIS TEST CASE NEEDS to change in TestDevSynchronousErrors to “A controller for your url was found, but that controller threw NotFoundException”

	@Test
	public void testWebappThrowsNotFound() {
		HttpFullRequest req = Requests.createRequest(KnownHttpMethod.GET, "/throwNotFound");
		
		CompletableFuture<HttpFullResponse> respFuture = http11Socket.send(req);
		
		ResponseWrapper response = ResponseExtract.waitResponseAndWrap(respFuture);
		response.assertStatusCode(KnownStatusCode.HTTP_404_NOTFOUND);
		response.assertContains("Your app's webpage was not found");		
	}

TEST PRG for internal error on POST….what is the behavior.

* tie together BufferPool max size, http1.1 max size, http2 max local size, channelmanager backpressure (size*10 (and for ssl*1.2))
* tests, tests, tests
  * test backpressure on upload file http1.1
  * test backpressure on download webpieces webserver static route file, http1.1
  * finish up the statemachine tests and with closing stream delay!!
  * IOException ChannelManager (needs to be manually tested on diff. scenarios)
    * manual then simulate - close remote socket on mac, windows, linux and then simulate automated test(each may be different)
    * manual then simulate - unplug computer from network and simulate
  * cancel request cancels the future AND all promises
  * cancel request cancets filedowload
  * cancel request cancels static file download(ie. stops reading from filesystem)
  * cancel request cancels file upload
  * remote and local flow control test cases on http client and server together OR just one(they use the same engine...soooo?)
  * add test on client cancelling request stream, cancelling push stream
  * add test on server cancelling request stream, cancelling push stream
  * add test to farEndClose on http1.1 and http2 clients and near end close and ensure all outstanding requests are failed AND ensure the futures for simple send request response all work
  * error test cases http2 client
  * tests on network outage during ajax calls to make that even cooler (ie. remember how redirect is screwed up.  we need to make network outage behave really nice as well)
  * tests on whitespace issues on tags and formatting so we can isolate the differences
  * http1.1 protect pipeline errors with tests(hmmm, is this error out one request and make second request work or something)
  * channelmanager error testing
* error test cases on server http2 and then try H2Test
* move gzip to frontend and move gzip to http client as well!!
* Need to make sure EVERY exit point calling into the client applications have try...catch and handle to not let their exceptions into the engine which WILL close the socket down and should not!!
* move this into http11 frontend only channelCloser.closeIfNeeded(request, stream);
* remove synchronized from http2 engine remote and local flow controls
* file upload
* file download
* streaming forever from controller
* streaming upload but forever into controller
* range request?
* start an actual multi-homed project
* add more and more tag examples
* move examples to @examples url instead
* (post ALPN implementation)verify upload file can work http2,etc
* java tmp locations seem to be deleted after a while.  research this so tests dont' fail(perhaps touch the files each build so all files have same timestamp)
* deal with line '                    if(payloadLength > settings.get(Http2Settings.Parameter.SETTINGS_MAX_FRAME_SIZE)'
* add optimistic locking test case for hibernate plugin with example in webapp and feature tests as examples as well
* implement Upgrade-Insecure-Requests where if server has SSL enabled, we redirect all pages to ssl
* response headers to add - X-Frame-Options (add in consumer webapp so can be changed), Keep-Alive with timeout?, Expires -1 (http/google.com), Content-Range(range requests)
* Tab state cookies (rather than just global session, but per tab state to put data)
* Database session state plugin(not tab, session) (ie. can use db for session or cookie)
* Database tab session state plugin (ie. can use db for tab state instead of cookie?)
* Metrics/Stats - Need a library to record stats(for graphing) that can record 99 percentile latency(not just average) per controller method as well as stats for many other things as well
* A/B split testing and experiments - hooks to wire into existing system and hooks to make it easier to create A/B pages
* Management - Need to do more than just integrate with JMX but also tie it to a datastore interface that is pluggable such that as JMX properties are changed, they are written into the database so changes persist (ie. no need for property files anymore except for initial db connection)
* google protobuf BodyContentBinder plugin
* thrift BodyContentBinder plugin
* codecov.io - still reports incorrect coverage results (different from jacoco)
* question out on jacoco code coverage for groovy files (code coverage works but linking to groovy files is not working for some reason)

* Section on i18n (need to explain, do NOT define message.properties since there is a list of Locales and that would create a match on any language)
* Section on Arrays and array based forms
* Section on tab state vs. session vs. flash (Validation, Flash)
* Section on filters

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
