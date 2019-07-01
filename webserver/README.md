# webpieces

modify @backend/secure/loggedinhome to have Document Home(instead of Document) and replace the existing Document Home with Html/Tag QuickRef
Remove documentation page in main app...it looks like crap and the plugin documentation is much much better.
hit backend port and not found page in development server does not list just backend routes :( fix to make it easier to debug
alphabetize the backend lists as it is annoying when it flips
remove this damn thing...System.setProperty("BACKEND_PORT_LIVE", "live");

* (6/27)for windows. fix https://discuss.gradle.org/t/i18n-issue-with-chinese-directly-in-java-string-on-windows-mac-is-fine/32216

add better parsing of flags after the Map so that arguments can be documented from plugins????  How to discover and print out help cleanly??
1. optional args with default(default can be null)
2. required args
3. extra args that clearly are NOT part of this program should fail and not start the server to keep command line args clean

REVISIT if still exist:see exceptions.log as well as POST /quitquitquit and immediately close socket ends in internal error though it was in process of sending notfound

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

* implement https://vladmihalcea.com/how-to-bootstrap-hibernate-without-the-persistence-xml-file/ to get rid of hacks on gradle AND it's just better to control it
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
