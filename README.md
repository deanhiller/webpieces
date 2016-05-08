# webpieces
A project containing all the web pieces (WITH apis) to create a web server (and an actual web server, and actual http proxy and http client).

This project is in process of implementing HTTP 2.0 as well.  Most projects like netty, grizzly, mina don't have a clean separation of an async http parser which is needed for nio so that was a priority for this project.  This project is essentially pieces that can be used to build any http related software and full stacks as well.  The http proxy will be very minimable but is for testing purposes of the http parser such that we can put it in the middle of chrome and firefox for integration testing.

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

TODO: 
* AsyncServer - timeout incoming server connection if client sends no data in X seconds
* AsyncServer - timeout server connection if time between data is more than X seconds
* xxxx - make sure we close the connection on a write failure or read failure
* httpparser - limit the payload size of an http request (if it has header after header after head, we should close the connection)
* ChannelManager should offer up a timeout on the writes, the connection is closed (or a wrapper of some sort) so we don't all have to implement this - this is half done....a write() now checks the write at the begin of queue and if hasn't written, it will timeout (The other half is a timer checking all queues every 'timeout' seconds or something like that or the selector could fire and check itself)
* httpproxy - AsyncServer has an overload mode that we should use when we are at a certain amount of outstanding requests that we should use
* httpproxy - keep-alive connections should be timed out at some point albeit this is a demo anyways but we could build it into more
* httpclient - timeout the request/response cycle
* SessionExecutor - should we limit the queue size per channel such that we backpressure a channel when the queue size reaches a certain limit? or at least make it configurable?
* httpparser(then httpclient) - if Content-Length > X, simulate http chunking so large files can be streamed through the system...and if < X just return entire response with body where X is configurable
* Need to go back and write more api level tests to beef up the test suite
* httpproxy - test out the caching of httpSocket in httpproxy further to make sure we understand the corner cases
