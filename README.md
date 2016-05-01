# webpieces
A project containing all the web pieces (WITH apis) to create a web server (and an actual web server)

"Composition over inheritance" is a well documented ideal.  Generally speaking, after years of development a developer comes to understand why composition is preferred over inheritance.  It is generally more flexible to changing requirements.  In this regard, I also believe "libraries over frameworks" is much of the same and there are many frameworks like netty, http servers, etc. that I believe you could actually do as a library that would be more composable.  Basically, webpieces is trying to follow the 'libraries over frameworks' idiom.  Creating a main method is easy, and with webpieces, you have so much more control

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
* httpproxy - keep-alive connections should be timed out at some point
