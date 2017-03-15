package de.timroes.axmlrpc;

import de.timroes.axmlrpc.serializer.SerializerHandler;
import java.io.IOException;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * An XMLRPCClient is a client used to make XML-RPC (Extensible Markup Language
 * Remote Procedure Calls).
 * The specification of XMLRPC can be found at http://www.xmlrpc.com/spec.
 * You can use flags to extend the functionality of the client to some extras.
 * Further information on the flags can be found in the documentation of these.
 * For a documentation on how to use this class see also the README file delivered
 * with the source of this library.
 *
 * @author Tim Roes
 */
public class XMLRPCClient {

	/**
	 * Constants from the http protocol.
	 */
	static final String CONTENT_TYPE = "Content-Type";
	static final String TYPE_XML = "text/xml; charset=utf-8";

	static final MediaType MEDIA_TYPE_XML = MediaType.parse(TYPE_XML);

	/**
	 * XML elements to be used.
	 */
	static final String METHOD_RESPONSE = "methodResponse";
	static final String PARAMS = "params";
	static final String PARAM = "param";
	public static final String VALUE = "value";
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

	/**
	 * The client will be able to send null values. A null value will be send
	 * as <nil/>. This extension is described under: http://ontosys.com/xml-rpc/extensions.php
	 */
	public static final int FLAGS_NIL = 0x08;

	/**
	 * With this flag enabled, the XML-RPC client will ignore the HTTP status
	 * code of the response from the server. According to specification the
	 * status code must be 200. This flag is only needed for the use with
	 * not standard compliant servers.
	 */
	public static final int FLAGS_IGNORE_STATUSCODE = 0x10;

	/**
	 * With this flag enabled, a value with a missing type tag, will be parsed
	 * as a string element. This is just for incoming messages. Outgoing messages
	 * will still be generated according to specification.
	 */
	public static final int FLAGS_DEFAULT_TYPE_STRING = 0x100;

	/**
	 * With this flag enabled, the {@link XMLRPCClient} ignores all namespaces
	 * used within the response from the server.
	 */
	public static final int FLAGS_IGNORE_NAMESPACES = 0x200;

	/**
	 * This prevents the decoding of incoming strings, meaning &amp; and &lt;
	 * won't be decoded to the & sign and the "less then" sign. See
	 * {@link #FLAGS_NO_STRING_ENCODE} for the counterpart.
	 */
	public static final int FLAGS_NO_STRING_DECODE = 0x800;

	/**
	 * By default outgoing string values will be encoded according to specification.
	 * Meaning the & sign will be encoded to &amp; and the "less then" sign to &lt;.
	 * If you set this flag, the encoding won't be done for outgoing string values.
	 * See {@link #FLAGS_NO_STRING_ENCODE} for the counterpart.
	 */
	public static final int FLAGS_NO_STRING_ENCODE = 0x1000;

	/**
	 * Activate debug mode.
	 * Do NOT use if you don't need it.
	 */
	public static final int FLAGS_DEBUG = 0x2000;

	/**
	 * This flag should be used if the server is an apache ws xmlrpc server.
	 * This will set some flags, so that the not standard conform behavior
	 * of the server will be ignored.
	 * This will enable the following flags: FLAGS_IGNORE_NAMESPACES, FLAGS_NIL,
	 * FLAGS_DEFAULT_TYPE_STRING
	 */
	public static final int FLAGS_APACHE_WS = FLAGS_IGNORE_NAMESPACES | FLAGS_NIL
			| FLAGS_DEFAULT_TYPE_STRING;

	private final int flags;

	private HttpUrl url;
	private OkHttpClient client;

	private ResponseParser responseParser;

	/**
	 * Create a new XMLRPC client for the given url.
	 * No flags will be used.
	 */
	public XMLRPCClient(String url) {
		this(createDefaultOkHttpClient(), url);
	}

	/**
	 * Create a new XMLRPC client for the given url.
	 */
	public XMLRPCClient(String url, int flags) {
		this(createDefaultOkHttpClient(), url, flags);
	}

	/**
	 * Create a new XMLRPC client for the given url.
	 * No flags will be used.
	 */
	public XMLRPCClient(OkHttpClient client, String url) {
		this(client, url, FLAGS_NONE);
	}

	/**
	 * Create a new XMLRPC client for the given url.
	 */
	public XMLRPCClient(OkHttpClient client, String url, int flags) {
		this(client, HttpUrl.parse(url), flags);
	}

	/**
	 * Create a new XMLRPC client for the given url.
	 * No flags will be used.
	 */
	public XMLRPCClient(HttpUrl url) {
		this(url, FLAGS_NONE);
	}

	/**
	 * Create a new XMLRPC client for the given URL.
	 */
	public XMLRPCClient(HttpUrl url, int flags) {
		this(createDefaultOkHttpClient(), url, flags);
	}

	/**
	 * Create a new XMLRPC client for the given url.
	 * No flags will be used.
	 */
	public XMLRPCClient(OkHttpClient client, HttpUrl url) {
		this(client, url, FLAGS_NONE);
	}

	/**
	 * Create a new XMLRPC client for the given url.
	 */
	public XMLRPCClient(OkHttpClient client, HttpUrl url, int flags) {
		this.client = client;
		this.url = url;
		this.flags = flags;

		SerializerHandler.initialize(flags);

		// Create a parser for the http responses.
		responseParser = new ResponseParser();
	}

	private static OkHttpClient createDefaultOkHttpClient() {
		return new OkHttpClient.Builder().build();
	}

	/**
	 * Returns the HttpUrl this XMLRPCClient is connected to.
	 */
	public HttpUrl getHttpUrl() {
		return url;
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

	/**
	 * Call a remote procedure on the server. The method must be described by
	 * a method name. If the method requires parameters, this must be set.
	 * The type of the return object depends on the server. You should consult
	 * the server documentation and then cast the return value according to that.
	 * This method will block until the server returned a result (or an error occurred).
	 * Read the README file delivered with the source code of this library for more
	 * information.
	 *
	 * @param method A method name to call.
	 * @param params An array of parameters for the method.
	 * @return The result of the server.
	 * @throws XMLRPCException Will be thrown if an error occurred during the call.
	 */
	public XMLRPCResponse call(String method, Object... params) throws XMLRPCException, IOException {
		Request request = createRequest(null, method, params);
		Response response = client.newCall(request).execute();
		return parseResponse(response);
	}

	/**
	 * Asynchronously call a remote procedure on the server. The method must be
	 * described by a method  name. If the method requires parameters, this must
	 * be set. When the server returns a response the onResponse method is called
	 * on the listener. If the server returns an error the onServerError method
	 * is called on the listener. The onError method is called whenever something
	 * fails. This method returns immediately and returns an identifier for the
	 * request. All listener methods get this id as a parameter to distinguish between
	 * multiple requests.
	 *
	 * @param tag        A tag for the request. This is passed to the callback. See also {@link #cancel(Object)}.
	 * @param listener   A listener, which will be notified about the server response or errors.
	 * @param methodName A method name to call on the server.
	 * @param params     An array of parameters for the method.
	 */
	public void callAsync(final Object tag, final XMLRPCCallback listener, String methodName, Object... params) {
		try {
			Request request = createRequest(tag, methodName, params);

			client.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(okhttp3.Call call, IOException e) {
					listener.onError(tag, e);
				}

				@Override
				public void onResponse(okhttp3.Call call, Response response) throws IOException {
					try {
						XMLRPCResponse xmlrpcResponse = parseResponse(response);
						listener.onResponse(tag, xmlrpcResponse);
					} catch (XMLRPCException e) {
						listener.onError(tag, e);
					}
				}
			});
		} catch (XMLRPCException e) {
			listener.onError(tag, e);
		}
	}

	private Request createRequest(Object tag, String methodName, Object... params) throws XMLRPCException {
		Call c = createCall(methodName, params);
		RequestBody body = RequestBody.create(MEDIA_TYPE_XML, c.getXML(isFlagSet(FLAGS_DEBUG)));

		return new Request.Builder().url(url).post(body).tag(tag).build();
	}

	private XMLRPCResponse parseResponse(Response response) throws XMLRPCException {
		if (isFlagSet(FLAGS_IGNORE_STATUSCODE) || response.isSuccessful()) {

			// Check for strict parameters
			if (isFlagSet(FLAGS_STRICT) && !response.headers().get(CONTENT_TYPE).startsWith(TYPE_XML)) {
				throw new XMLRPCException("The Content-Type of the response must be text/xml.");
			}

			Object responseBody = responseParser.parse(response.body().byteStream(), isFlagSet(FLAGS_DEBUG));

			return new XMLRPCResponse(response, responseBody);
		}

		return new XMLRPCResponse(response, null);
	}

	/**
	 * Cancel a specific asynchronous call.
	 */
	public void cancel(Object tag) {
		for (okhttp3.Call call : client.dispatcher().queuedCalls()) {
			if (tag.equals(call.request().tag())) call.cancel();
		}
		for (okhttp3.Call call : client.dispatcher().runningCalls()) {
			if (tag.equals(call.request().tag())) call.cancel();
		}
	}

	/**
	 * Create a call object from a given method string and parameters.
	 *
	 * @param method The method that should be called.
	 * @param params An array of parameters or null if no parameters needed.
	 * @return A call object.
	 */
	private Call createCall(String method, Object[] params) {
		if (isFlagSet(FLAGS_STRICT) && !method.matches("^[A-Za-z0-9\\._:/]*$")) {
			throw new XMLRPCRuntimeException("Method name must only contain A-Z a-z . : _ / ");
		}

		return new Call(method, params);
	}
}
