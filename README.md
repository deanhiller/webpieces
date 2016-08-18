# webpieces

To try the webserver

1. Download the release, unzip
2. run ./createProject.sh
3. 

A project containing all the web pieces (WITH apis) to create a web server (and an actual web server, and an actual http proxy and an http client and an independent async http parser1.1 and independent http parser2......getting the idea yet, self contained pieces).  This webserver is also made to be extremely Test Driven Development for web app developers such that tests can be written that will test all your filters, controllers, views, redirects and everything all together in one for GREAT whitebox QE type testing that can be done by the developer.  Don't write brittle low layer tests and instead write high layer tests that are less brittle then their fine grained counter parts (something many of us do at twitter)

This project is essentially pieces that can be used to build any http related software and full stacks as well.  

Some HTTP/2 features
 * better pipelining of requests fixing head of line blocking problem
 * Server push - sending responses before requests even come based on the first page requests (pre-emptively send what you know they will need)
 * Data compression of HTTP headers
 * Multiplexing multiple requests over TCP connection

Pieces with HTTP/2 support
 * async-http parser - feel free to use with any nio library that you like
 * embeddablehttpproxy - a proxy with http 2 support
 * embeddablewebserver - a webserver with http 2 support
 * httpclient - An http client with http 2 support

channelmanager - a very thin layer on nio for speed
asyncserver - a thin wrapper on channelmanager to create a one call tcp server

httpparser - an asynchronous http parser than can accept partial payloads (ie. nio payloads don't have full message)
httpclient - http client built on above core components
httpproxy - build on asyncserver and http client

TODO: 
* get i18n working and then get it working with field.tag then getting it working with arrays and field.tag(which is harder)
* unit test arrays in multi-part form, query params! and test for flash working with arrays(not sure it will or not)
* unit test query param conflict with multipart, query param conflict with path param, and multipart param conflict with path param. specifically createTree stuff PAramNode, etc.
* need to test out cookie length and 150 cookies of certain length
* write an escapehtml tag
* implement error, errorClass, errors, ifError, ifErrors, jsAction, jsRoute, option, select,
* catch-all route with POST as in /{controller}/{action}   {controller}.post{action}
* Need to test theory of a theme can be a unique controllers/views set AND then many unique views on that set.  a theme does not just have to be look but the controller as well possibly
* Test out no such property/pageArg in an expression used in a tag argument and print the line number of the property not found
* AFTER have session and cookies add authenticityToken to make more secure
* gzip/deflate/sdch compression?
* response headers to add - X-Frame-Options (add in consumer webapp so can be changed), Keep-Alive with timeout?, Content-Encoding gzip, Transfer-Encoding chunked, Cache-Control, Expires -1 (http/google.com), Content-Range(range requests)
* CRUD - create re-usable CRUD routes in a scoped re-usable routerModule vs. global POST route as well?
* Stats - Need a library to record stats(for graphing) that can record 99 percentile latency(not just average) per controller method as well as stats for many other things as well
* Management - Need to do more than just integrate with JMX but also tie it to a datastore interface that is pluggable such that as JMX properties are changed, they are written into the database so changes persist (ie. no need for property files anymore except for initial db connection)
* language
* bring back Hotswap for the dev server ONCE the projectTemplate is complete and we are generating projects SUCH that we can add a startup target that adds the Hotswap agent propertly
* Need to add tests for changing the guice modules and router modules in the main server class while dev server is running and then hit website again to make sure it changed
* cookie hpttOnly and the other key as well
* add a lot of pretty print objects/json stuff in the toString so when debugging, there is many less clicks to see the data!!!  it is just right there
* have the dev server display it's OWN 404 page and then in a frame below dispay the webapps actual 404 page.  The dev server's page will have much more detail on what went wrong and why it was a 404 like type translation, etc.  The frame can be a redirect to GET the 404 page directly OR it could render inline maybe.....which one is better..not sure?  rendering inline is probably better so the notFound does not have a direct url to get to that page?  But only if the PRG holds true above!!!!



* ALPN is next!!!! 
