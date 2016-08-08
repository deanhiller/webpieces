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
* generate file with list of routeid enums on compile and then on startup verify each one can be resolved uniquely so if not found, we can prevent release since there is definitely pages with bugs
* implement field tag next
* implement a real form and try out post redirect get and especially what to do on post not found situation
* catch-all route with POST
* AFTER have session and cookies add authenticityToken to make more secure
* gzip/deflate/sdch compression?
* response headers to add - X-Frame-Options (add in consumer webapp so can be changed), Keep-Alive with timeout?, Content-Encoding gzip, Transfer-Encoding chunked, Cache-Control, Expires -1 (http/google.com), Content-Range(range requests)
* CRUD - create re-usable CRUD routes in a scoped re-usable routerModule vs. global POST route as well?
* Stats - Need a library to record stats(for graphing) that can record 99 percentile latency(not just average) per controller method as well as stats for many other things as well
* Management - Need to do more than just integrate with JMX but also tie it to a datastore interface that is pluggable such that as JMX properties are changed, they are written into the database so changes persist (ie. no need for property files anymore except for initial db connection)
* PRG pattern vs. "POST request comes in, path not found, so send back 404 with rendered page".  Currently in this special instance, we violate PRG and go with 404 back to use with the page.  We NEED to test this though and find if this breaks the browser back button and if it does make it more usable for people using every website written on this webserver.  Every other instance, we force apps into PRG so their users have a GREAT experience with the website
* language
* bring back Hotswap for the dev server ONCE the projectTemplate is complete and we are generating projects SUCH that we can add a startup target that adds the Hotswap agent propertly
* Need to add tests for changing the guice modules and router modules in the main server class while dev server is running and then hit website again to make sure it changed
* cookie hpttOnly and the other key as well
* add a lot of pretty print objects/json stuff in the toString so when debugging, there is many less clicks to see the data!!!  it is just right there
* have the dev server display it's OWN 404 page and then in a frame below dispay the webapps actual 404 page.  The dev server's page will have much more detail on what went wrong and why it was a 404 like type translation, etc.  The frame can be a redirect to GET the 404 page directly OR it could render inline maybe.....which one is better..not sure?  rendering inline is probably better so the notFound does not have a direct url to get to that page?  But only if the PRG holds true above!!!!



* ALPN is next!!!! 
