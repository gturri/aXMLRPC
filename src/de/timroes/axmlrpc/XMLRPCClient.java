package de.timroes.axmlrpc;

import de.timroes.axmlrpc.serializer.SerializerHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * An XMLRPCClient is a client used to make XML-RPC (Extensible Markup Language
 * Remote Procedure Calls).
 * The specification of XMLRPC can be found at http://www.xmlrpc.com/spec.
 * You can use flags to extend the functionality of the client to some extras.
 * Further information on the flags can be found in the documentation of these.
 * For a documentation on how to use this class see also the readme file delivered
 * with the source of this library.
 *
 * @author Tim Roes
 */
public class XMLRPCClient {

	private static final String DEFAULT_USER_AGENT = "aXMLRPC";

	/**
	 * Constants from the http protocol.
	 */
	static final String USER_AGENT = "User-Agent";
	static final String CONTENT_TYPE = "Content-Type";
	static final String TYPE_XML = "text/xml";
	static final String HOST = "Host";
	static final String CONTENT_LENGTH = "Content-Length";
	static final String HTTP_POST = "POST";

	/**
	 * XML elements to be used.
	 */
	static final String METHOD_RESPONSE = "methodResponse";
	static final String PARAMS = "params";
	static final String PARAM = "param";
	static final String VALUE = "value";
	static final String FAULT = "fault";
	static final String METHOD_CALL = "methodCall";
	static final String METHOD_NAME = "methodName";
	static final String STRUCT_MEMBER = "member";

	/**
	 * No flags should be set.
	 */
	public static final int FLAGS_NONE = 0x0;

	/**
	 * The client should parse responses strict to specification.
	 * It will check if the given content-type is right.
	 * The method name in a call must only contain of A-Z, a-z, 0-9, _, ., :, /
	 * Normally this is not needed.
	 */
	public static final int FLAGS_STRICT = 0x01;

	/**
	 * The client will be able to handle 8 byte integer values (longs).
	 * The xml type tag &lt;i8&gt; will be used. This is not in the specification
	 * but some libraries and servers support this behaviour.
	 * If this isn't enabled you cannot recieve 8 byte integers and if you try to
	 * send a long the value must be within the 4byte integer range.
	 */
	public static final int FLAGS_8BYTE_INT = 0x02;

	private int flags;

	private URL url;
	private Map<String,String> httpParameters = new HashMap<String, String>();

	private ResponseParser responseParser;

	/**
	 * Create a new XMLRPC client for the given url.
	 *
	 * @param url The url to send the requests to.
	 * @param userAgent A user agent string to use in the http requests.
	 * @param flags A combination of flags to be set.
	 */
	public XMLRPCClient(URL url, String userAgent, int flags) {

		SerializerHandler.initialize(flags);

		this.url = url;

		this.flags = flags;
		// Create a parser for the http responses.
		responseParser = new ResponseParser();

		httpParameters.put(CONTENT_TYPE, TYPE_XML);
		httpParameters.put(USER_AGENT, userAgent);

	}

	/**
	 * Create a new XMLRPC client for the given url.
	 * The default user agent string will be used.
	 *
	 * @param url The url to send the requests to.
	 * @param flags A combination of flags to be set.
	 */
	public XMLRPCClient(URL url, int flags) {
		this(url, DEFAULT_USER_AGENT, flags);
	}

	/**
	 * Create a new XMLRPC client for the given url.
	 * No flags will be set.
	 *
	 * @param url The url to send the requests to.
	 * @param userAgent A user agent string to use in the http request.
	 */
	public XMLRPCClient(URL url, String userAgent) {
		this(url, userAgent, FLAGS_NONE);
	}

	/**
	 * Create a new XMLRPC client for the given url.
	 * No flags will be used.
	 * The default user agent string will be used.
	 *
	 * @param url The url to send the requests to.
	 */
	public XMLRPCClient(URL url) {
		this(url, DEFAULT_USER_AGENT, FLAGS_NONE);
	}

	/**
	 * Sets the user agent string.
	 * If this method is never called the default
	 * user agent 'aXMLRPC' will be used.
	 *
	 * @param userAgent The new user agent string.
	 */
	public void setUserAgentString(String userAgent) {
		httpParameters.put(USER_AGENT, userAgent);
	}

	/**
	 * Set a http header field to a custom value.
	 * You cannot modify the Host or Content-Type field that way.
	 * If the field already exists, the old value is overwritten.
	 *
	 * @param headerName The name of the header field.
	 * @param headerValue The new value of the header field.
	 */
	public void setCustomHttpHeader(String headerName, String headerValue) {
		if(CONTENT_TYPE.equals(headerName) || HOST.equals(headerName)
				|| CONTENT_LENGTH.equals(headerName)) {
			throw new XMLRPCRuntimeException("You cannot modify the Host, Content-Type or Content-Length header.");
		}
		httpParameters.put(headerName, headerValue);
	}

	/**
	 * Call a remote procedure on the server. The method must be described by
	 * a method name. If the method requires parameters, this must be set.
	 * The type of the return object depends on the server. You should consult
	 * the server documentation and then cast the return value according to that.
	 * This method will block until the server returned a result (or an error occured).
	 * Read the readme file delivered with the source code of this library for more
	 * information.
	 *
	 * @param method A method name to call.
	 * @param params An array of parameters for the method.
	 * @return The result of the server.
	 * @throws XMLRPCException Will be thrown if an error occured during the call.
	 */
	public Object call(String method, Object[] params) throws XMLRPCException {

		try {
			
			Call c = createCall(method, params);

			URLConnection conn = this.url.openConnection();
			if(!(conn instanceof HttpURLConnection)) {
				throw new IllegalArgumentException("The URL is not for a http connection.");
			}

			HttpURLConnection http = (HttpURLConnection)conn;
			http.setRequestMethod(HTTP_POST);
			http.setDoOutput(true);
			http.setDoInput(true);

			// Set the request parameters
			for(Map.Entry<String,String> param : httpParameters.entrySet()) {
				http.setRequestProperty(param.getKey(), param.getValue());
			}

			OutputStreamWriter stream = new OutputStreamWriter(http.getOutputStream());
			stream.write(c.getXML());
			stream.flush();
			stream.close();

			InputStream istream = http.getInputStream();

			if(http.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new XMLRPCException("The status code of the http response must be 200.");
			}

			// Check for strict parameters
			if(isFlagSet(FLAGS_STRICT)) {
				if(!http.getContentType().startsWith(TYPE_XML)) {
					throw new XMLRPCException("The Content-Type of the response must be text/xml.");
				}
			}

			return responseParser.parse(istream);
		} catch (IOException ex) {
			throw new XMLRPCException(ex);
		}
		
	}

	/**
	 * Call a remote procedure on the server. The method must be described by
	 * a method name. This method is only for methods that doesn't require any parameters.
	 * The type of the return object depends on the server. You should consult
	 * the server documentation and then cast the return value according to that.
	 * This method will block until the server returned a result (or an error occured).
	 * Read the readme file delivered with the source code of this library for more
	 * information.
	 *
	 * @param methodName A method name to call.
	 * @return The result of the server.
	 * @throws XMLRPCException Will be thrown if an error occured during the call.
	 */
	public Object call(String methodName) throws XMLRPCException {
		return call(methodName, null);
	}

	/**
	 * Call a remote procedure on the server. The method must be described by
	 * a method name. If the method requires parameters, this must be set.
	 * The type of the return object depends on the server. You should consult
	 * the server documentation and then cast the return value according to that.
	 * This method will block until the server returned a result (or an error occured).
	 * Read the readme file delivered with the source code of this library for more
	 * information.
	 *
	 * @param method A method name to call.
	 * @param param1 The first parameter of the method.
	 * @return The result of the server.
	 * @throws XMLRPCException Will be thrown if an error occured during the call.
	 */
	public Object call(String methodName, Object param1) throws XMLRPCException {
		return call(methodName, new Object[]{param1});
	}

	/**
	 * Call a remote procedure on the server. The method must be described by
	 * a method name. If the method requires parameters, this must be set.
	 * The type of the return object depends on the server. You should consult
	 * the server documentation and then cast the return value according to that.
	 * This method will block until the server returned a result (or an error occured).
	 * Read the readme file delivered with the source code of this library for more
	 * information.
	 *
	 * @param method A method name to call.
	 * @param param1 The first parameter of the method.
	 * @param param2 The second parameter of the method.
	 * @return The result of the server.
	 * @throws XMLRPCException Will be thrown if an error occured during the call.
	 */
	public Object call(String methodName, Object param1, Object param2) throws XMLRPCException {
		return call(methodName, new Object[]{param1,param2});
	}

	/**
	 * Call a remote procedure on the server. The method must be described by
	 * a method name. If the method requires parameters, this must be set.
	 * The type of the return object depends on the server. You should consult
	 * the server documentation and then cast the return value according to that.
	 * This method will block until the server returned a result (or an error occured).
	 * Read the readme file delivered with the source code of this library for more
	 * information.
	 *
	 * @param method A method name to call.
	 * @param param1 The first parameter of the method.
	 * @param param2 The second parameter of the method.
	 * @param param3 The third parameter of the method.
	 * @return The result of the server.
	 * @throws XMLRPCException Will be thrown if an error occured during the call.
	 */
	public Object call(String methodName, Object param1, Object param2, Object param3)
			throws XMLRPCException {
		return call(methodName, new Object[]{param1,param2,param3});
	}

	/**
	 * Call a remote procedure on the server. The method must be described by
	 * a method name. If the method requires parameters, this must be set.
	 * The type of the return object depends on the server. You should consult
	 * the server documentation and then cast the return value according to that.
	 * This method will block until the server returned a result (or an error occured).
	 * Read the readme file delivered with the source code of this library for more
	 * information.
	 *
	 * @param method A method name to call.
	 * @param param1 The first parameter of the method.
	 * @param param2 The second parameter of the method.
	 * @param param3 The third parameter of the method.
	 * @param param4 The fourth parameter of the method.
	 * @return The result of the server.
	 * @throws XMLRPCException Will be thrown if an error occured during the call.
	 */
	public Object call(String methodName, Object param1, Object param2, Object param3,
			Object param4) throws XMLRPCException {
		return call(methodName, new Object[]{param1,param2,param3,param4});
	}

	/**
	 * Create a call object from a given method string and parameters.
	 *
	 * @param method The method that should be called.
	 * @param params An array of parameters or null if no parameters needed.
	 * @return A call object.
	 */
	private Call createCall(String method, Object[] params) {

		if(isFlagSet(FLAGS_STRICT) && !method.matches("^[A-Za-z0-9\\._:/]*$")) {
			throw new XMLRPCRuntimeException("Method name must only contain A-Z a-z . : _ / ");
		}

		return new Call(method, params);

	}

	/**
	 * Checks whether a specific flag has been set.
	 *
	 * @param flag The flag to check for.
	 * @return Whether the flag has been set.
	 */
	private boolean isFlagSet(int flag) {
		return (this.flags & flag) != 0;
	}

}