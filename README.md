# webpieces
A project containing all the web pieces (WITH apis) to create a web server (and an actual web server)

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
* ChannelManager should offer up a timeout on the writes, the connection is closed (or a wrapper of some sort) so we don't all have to implement this
* httpproxy - AsyncServer has an overload mode that we should use when we are at a certain amount of outstanding requests that we should use

