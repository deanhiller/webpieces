# webpieces

A project containing all the web pieces (WITH apis) to create a web server (and an actual web server, and an actual http proxy and an http client and an independent asycn http parser1.1 and independent http parser2......getting the idea yet, self contained pieces).  This webserver is also made to be extremely Test Driven Development for web app developers such that tests can be written that will test all your filters, controllers, views, redirects and everything all together in one for GREAT whitebox QE type testing that can be done by the developer.  Don't write brittle low layer tests and instead write high layer tests that are less brittle then their fine grained counter parts (something many of us do at twitter)

This project is in process of implementing HTTP 2.0 as well.  This project is essentially pieces that can be used to build any http related software and full stacks as well.  The http proxy will be very minimable but is for testing purposes of the http parser such that we can put it in the middle of chrome and firefox for integration testing.

Some HTTP/2 features (we are actively working this)
 * better pipelining of requests fixing head of line blocking problem
 * Server push - sending responses before requests even come based on the first page requests (pre-emptively send what you know they will need)
 * Data compression of HTTP headers
 * Multiplexing multiple requests over TCP connection

Pieces with HTTP/2 support
 * async-http parser - feel free to use with any nio library that you like
 * embeddablehttpproxy - a proxy with http 2 support
 * embeddablewebserver - a webserver with http 2 support
 * httpclient - An http client with http 2 support

"Composition over inheritance" is a well documented ideal.  Generally speaking, after years of development a developer comes to understand why composition is preferred over inheritance.  It is generally more flexible to changing requirements.  In this regard, I also believe "libraries over frameworks" is much of the same and there are many frameworks like netty, http servers, etc. that I believe you could actually do as a library that would be more composable(ie. embeddablewebserver and embeddablehttpproxy are BOTH libraries not frameworks!!!!!).  Basically, webpieces is trying to follow the 'libraries over frameworks' idiom.  Creating a main method is easy, and with webpieces, you have so much more control.....lastly, you can swap ANY piece in these libraries by just bindingin a different piece via one line of java code.  ie. These are very hackable libraries to support many different needs

channelmanager - a very thin layer on nio for speed
asyncserver - a thin wrapper on channelmanager to create a one call tcp server

httpparser - an asynchronous http parser than can accept partial payloads (ie. nio payloads don't have full message)
httpclient - http client built on above core components
httpproxy - build on asyncserver and http client

NOTE: There is a Recorder and Playback that if you wire in, you can record things that are going wrong and use the Playback to play it back into your system.  We use this for http parser and SSL Engine so that we can have an automated test suite against very real test cases.

TODO: 
* (working)httpparser - limit the payload size of an http request (if it has header after header after head, we should close the connection)
* (working on right now)break out build.gradle file into multiple ones so as project grows, it scales better
* open connection to google and see how long before timeout
* gzip/deflate/sdch compression?
* open keep alive connection to google, send ONE request and see how long before timeout
* FrontendServer - timeout server connection if time between data is more than X seconds...make sure is more than http2 timeout window that is sent back in frontend server.  ie. implement Keep-Alive: timeout=15, max=100  
* verify keep alive timeout we chose with wireshark trace of google.com or some website (Great, they don't use keep alive)
* Integration test SoTimeout and setKeepAlive on two computers 
* ChannelManager should offer up a timeout on the writes, the connection is closed (or a wrapper of some sort) so we don't all have to implement this - this is half done....a write() now checks the write at the begin of queue and if hasn't written, it will timeout (The other half is a timer checking all queues every 'timeout' seconds or something like that or the selector could fire and check itself)
* httpproxy - AsyncServer has an overload mode that we should use when we are at a certain amount of outstanding requests(maybe?)
* httpproxy - should respect keep-alive responses such that we send max N requests and shut down connection if no requests in keepalive timeout window occur
* httpclient - timeout the request/response cycle
* SessionExecutor - should we limit the queue size per channel such that we backpressure a channel when the queue size reaches a certain limit? or at least make it configurable?  This helps if client holds up incomingData thread to backpressure just the channels that need it
* Need to go back and write more api level tests to beef up the test suite
* httpproxy - test out the caching of httpSocket in httpproxy further to make sure we understand the corner cases
* response headers to add - X-Frame-Options (add in consumer webapp so can be changed), Keep-Alive with timeout?, Content-Encoding gzip, Transfer-Encoding chunked, Cache-Control, Expires -1 (http/google.com), Content-Range(range requests)
* httprouter - tie method param count to path param count unless @Loose is used (we should do this earlier before more and more violations happen...it's easier to loosen constraints later than tighten them up) OR have the routes be of the format <controller>.method(param1, param2) so we can count method count
* CRUD - create re-usable CRUD routes in a scoped re-usable routerModule vs. global POST route as well?
* Stats - Need a library to record stats(for graphing) that can record 99 percentile latency(not just average) per controller method as well as stats for many other things as well
* Management - Need to do more than just integrate with JMX but also tie it to a datastore interface that is pluggable such that as JMX properties are changed, they are written into the database so changes persist (ie. no need for property files anymore except for initial db connection)
* PRG pattern vs. "POST request comes in, path not found, so send back 404 with rendered page".  Currently in this special instance, we violate PRG and go with 404 back to use with the page.  We NEED to test this though and find if this breaks the browser back button and if it does make it more usable for people using every website written on this webserver.  Every other instance, we force apps into PRG so their users have a GREAT experience with the website
* language
* bring back Hotswap for the dev server ONCE the projectTemplate is complete and we are generating projects SUCH that we can add a startup target that adds the Hotswap agent propertly
* We need to run the same class that ./createProject.sh runs and then start that projects webserver and send requests in to make sure the template generation is working and not broken
* Need to add tests for changing the guice modules and router modules in the main server class while dev server is running and then hit website again to make sure it changed
* cookie hpttOnly and the other key as well
* search on Charset.defaultCharset, Charset.forName, StandardCharsets and unify them so it is configurable
* add a lot of pretty print objects/json stuff in the toString so when debugging, there is many less clicks to see the data!!!  it is just right there
* come up with the http 500 strategy for dev server AND for production server
* have the dev server display it's OWN 404 page and then in a frame below dispay the webapps actual 404 page.  The dev server's page will have much more detail on what went wrong and why it was a 404 like type translation, etc.  The frame can be a redirect to GET the 404 page directly OR it could render inline maybe.....which one is better..not sure?  rendering inline is probably better so the notFound does not have a direct url to get to that page?  But only if the PRG holds true above!!!!

* ALPN is next!!!! 
