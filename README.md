What is aXMLRPC?
================

aXMLRPC is a Java library with a leightweight XML-RPC client. XML-RPC is
a specification for making remote procedure calls over the HTTP protocol
in an XML format. The specificationc can be found under http://www.xmlrpc.com/spec.

The library was developed for the use with Android. Since it has no dependencies to 
any Android library or any other 3rd-party library, it is fully functional in any
common java virtual machine (not only on Android).

You can control the client with some flags to extend its functionality. See the section
about flags.

How to include it?
==================

How to include the aXMLRPC client into your project?
There are several ways to do that:

### Include the source code

You can just include all the source code from the `src` directory into the sourcecode
of your project. If you use git yourself, you can use submodules to include the code 
as a module to yours. So you will always stay up to date with the library.

### Compile it as library

aXMLRPC uses maven, so you can build it using

    mvn install

### Use Maven

To use it on your Maven project, add it as a dependency on your pom.xml file:

```xml
<dependency>
    <groupId>fr.turri</groupId>
    <artifactId>aXMLRPC</artifactId>
    <version>X.Y.Z</version>
</dependency>
```

where X.Y.Z is the current aXMLRPC version


How to use the library?
=======================

You can use the library by initiating an `XMLRPCClient` and make calls over it:

```java
try {
	XMLRPCClient client = new XMLRPCClient(new URL("http://example.com/xmlrpc"));

	Boolean b = (Boolean)client.call("isServerOk");
	Integer i = (Integer)client.call("add", 5, 10);
} catch(XMLRPCServerException ex) {
	// The server throw an error.
} catch(XMLRPCException ex) {
	// An error occured in the client.
} catch(Exception ex) {
	// Any other exception
}
```

Instead of passing the parameters as seperated values, you can also pack them in
an array and pass the array to the method, like in the following example:

```java
// ... The try-catch has been ommited for clarity.
XMLRPCClient client = new XMLRPCClient(url, "MyUserAgentString");
client.call("someMethod", new Object[]{ o1, o2, o3 });
// ...
```

#### Asynchronous Calls

The above method calls are synchronous. So the method `call` will return when the server responded
or an error occured. There is also a possibility for asynchronous server calls.
You need to implement an XMLRPCCallback that will get noticed about the respone (or error) from
the server. The `callAsync` method can be used to make asynchronous calls. It returns an identifier
that will also be send to the XMLRPCCallback instance with the response of the server, so your
application can make multiple calls concurrently and use one listener for them, that distinguish 
between the different request by their ids.

```java
XMLRPCCallback listener = new XMLRPCCallback() {
	public void onResponse(long id, Object result) {
		// Handling the servers response
	}
	public void onError(long id, XMLRPCException error) {
		// Handling any error in the library
	}
	public void onServerError(long id, XMLRPCServerException error) {
		// Handling an error response from the server
	}
};

XMLRPCClient client = new XMLRPCClient(url);
long id = client.callAsync(listener, "add", 5, 10);
```

You will be also able to cancel an asynchonous call. Just use the `cancel` method on the `XMLRPCClient` instance,
like in the following example. The listener will not be notified, if the call is canceled.

```java
XMLRPCClient client = new XMLRPCClient(url);
long id = client.callAsync(listener, "method", params);
// ...
client.cancel(id);
```

The data types
--------------

The specification give some data tags for the server response. If you want to work on the
type you must cast the returning `Object` from the `call` method to its specific type.
Which type to cast which XML server response, tells the following list:

`i4`,`int`	=> `Integer`

`boolean`	=> `Boolean`

`string`	=> `String`

`double`	=> `Double`

`dateTime.iso8601`	=> `Date`

`base64`	=> `byte[]` (`Byte[]` won't work)

`array`		=> `Object[]`

`struct`	=> `Map<String,Object>`

`i8`		=> `Long` (see Flags)


Flags
-----

The client takes as second parameter (or third if an user agent is given) 
a combination of multiple flags. It could work like the following example:

```java
// ...
XMLRPCClient client = new XMLRPCClient(url, 
	XMLRPCClient.FLAGS_STRICT | XMLRPCClient.FLAGS_8BYTE_INT);
// ...
```

The following flags are implemented:


#### FLAGS_STRICT

The client should parse responses strict to specification.
It will check if the given content-type is right.
The method name in a call must only contain of A-Z, a-z, 0-9, _, ., :, /
Normally this is not needed.


#### FLAGS_8BYTE_INT

The client will be able to handle 8 byte integer values (longs).
The xml type tag `<i8>` will be used. This is not in the specification
but some libraries and servers support this behaviour.
If this isn't enabled you cannot recieve 8 byte integers and if you try to
send a long, the value must be within the 4 byte integer range.


#### FLAGS_ENABLE_COOKIES

With this flag, the client will be able to handle cookies, meaning saving cookies
from the server and sending it with every other request again. This is needed
for some XML-RPC interfaces that support login.


#### FLAGS_NIL

The client will be able to send `null` values. A `null` value will be send
as `<nil/>`. This extension is described under: http://ontosys.com/xml-rpc/extensions.php


#### FLAGS_IGNORE_STATUSCODE

With this flag enabled, the XML-RPC client will ignore the HTTP status
code of the response from the server. According to specification the
status code must be 200. This flag is only needed for the use with 
not standard compliant servers.


#### FLAGS_FORWARD

With this flag enabled, the client will forward the request, if
the 301 or 302 HTTP status code has been received. If this flag is not
set, the client will throw an exception on these HTTP status codes.


#### FLAGS_SSL_IGNORE_INVALID_HOST

With this flag enabled, the client will ignore, if the URL doesn't match
the SSL Certificate. This should be used with caution. Normally the URL
should always match the URL in the SSL certificate, even with self signed
certificates.


#### FLAGS_SSL_INGORE_INVALID_CERT

With this flag enabled, the client will ignore all unverified SSL/TLS 
certificates. This must be used, if you use self-signed certificates
or certificated from unknown (or untrusted) authorities.


#### FLAGS_DEFAULT_TYPE_STRING

With this flag enabled, a value with a missing type tag, will be parsed
as a string element. This is just for incoming messages. Outgoing messages
will still be generated according to specification.


#### FLAGS_IGNORE_NAMESPACES
With this flag enabled, the client ignores all namespaces
used within the response from the server.


#### FLAGS_USE_SYSTEM_PROXY
With this flag enabled, the XMLRPCClient will use the system http
proxy to connect to the XML-RPC server.


#### FLAGS_NO_STRING_ENCODE
By default outgoing string values will be encoded according to specification.
Meaning the & sign will be encoded to `&amp;` and the "less then" sign to `&lt;`.
If you set this flag, the encoding won't be done for outgoing string values.
See `FLAGS_NO_STRING_DECODE` for the counterpart.


#### FLAGS_NO_STRING_DECODE
This prevents the decoding of incoming strings, meaning `&amp;` and `&lt;`
won't be decoded to the & sign and the "less then" sign. See
`FLAGS_NO_STRING_ENCODE` for the counterpart.

#### FLAGS_DEBUG
Will display additional information on the console.
Do not use it in production.

#### FLAGS_ACCEPT_NULL_DATE
By default a response with an empty date (eg: `<value><dateTime.iso8601/></value>`)
is invalid and hence throws an exception.
With this flag, this input is accepted, and returns a null date

Meta Flags
----------

This can be used exactly the same as normal flags. But each meta flag is just a
collection of different other flags. There is no functional difference in using
a meta flag or all the containing flags. For detailed documentation on the single
flags read the above section.


#### FLAGS_SSL_IGNORE_ERRORS

This flag disables all SSL warnings. It is an alternative to use
FLAGS_SSL_IGNORE_INVALID_CERT | FLAGS_SSL_IGNORE_INVALID_HOST.


#### FLAGS_APACHE_WS

This flag should be used if the server is an apache ws xmlrpc server.
This will set some flags, so that the not standard conform behavior
of the server will be ignored.
This will enable the following flags: FLAGS_IGNORE_NAMESPACES, FLAGS_NIL,
FLAGS_DEFAULT_TYPE_STRING

Using an arbitrary transport
============================
aXMLRPC uses http with the java.net API. If you want to use another protocol or API, you can do:

```java
    boolean debug = false;
    SerializerHandler serializerHandler = new SerializerHandler(); // or you may build it with flags
    String payload = new Call(serializerHandler, "add", 5, 10).getXML(debug);

    InputStream istream = sendPayloadWithMyTransport(payload); // use your implementation here

    Integer i = (Integer) new ResponseParser.parse(serializerHandler, istream, debug);
```


License
=======

The library is licensed under [MIT License] (http://www.opensource.org/licenses/mit-license.php).
See the LICENSE file for the license text. 

For the uninformed reader: What does MIT mean?

- You can copy this, modify it, distribute it, sell it, eat it.
- You don't need to notice me about anything of the above.
- If you make changes to it, it would be nice (but not obliged), if you would share it with me again.
- Put the copyright notice and the LICENSE file in any copy you make.

Bugs?
=====

If you find a bug or wish some enhancements for the library, please
fill an issue here on github.

Build status
============
Status: ![Status](https://travis-ci.org/gturri/aXMLRPC.svg?branch=master)
