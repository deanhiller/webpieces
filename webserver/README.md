# webpieces

#### TODO:
* Rewire SimpleStorage to be injected in Server.java class instead and passed to router to be bound so there is no weird callback anymore!!!
* debug http client backpressure on downloading from website fun
* add non-selenium test for AJAX CRUD POST form when logged out and then login and then render
* add test for %{..}% to set var and %{..}% to print var to verify it is there so groovy implant is working
* rewire redirects so port stuff is done OUTSIDE the router so we no longer need funky callbacks.  (ie. I made a mistake there that we can fix by providing all the info from the router to the webserver and webserver knows the ports).  This is more complex than that!  
* add test for java enum and list select and multiselect
* add test for java related entity select and multiselect


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
