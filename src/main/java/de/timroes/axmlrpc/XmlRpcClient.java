package de.timroes.axmlrpc;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Roes - mail@timroes.de
 */
public class XmlRpcClient {
	
	private final static MediaType MEDIA_TYPE = MediaType.parse("text/xml; charset=utf-8");
	
	public static XmlRpcClient forUrl(String url) throws MalformedURLException {
		return new XmlRpcClient(new URL(url));
	}
	
	public static XmlRpcClient forUrl(URL url) {
		return new XmlRpcClient(url);
	}
	
	private final URL url;
	private final OkHttpClient httpClient;
	
	private XmlRpcClient(URL url) {
		this.url = url;
		this.httpClient = new OkHttpClient();
	}
	
	public URL getUrl() {
		return url;
	}
	
	public MethodCall createCall(String method, Object... params) {
		return new MethodCall(method, params);
	}
	
	public XmlRpcResponse call(String method, Object... params) {
		return createCall(method, params).execute(this);
	}
	
	public XmlRpcResponse call(MethodCall call) {
		return call.execute(this);
	}
	
	public static class MethodCall {
		
		private final String method;
		private final Object[] params;

		private MethodCall(String method, Object[] params) {
			this.method = method;
			this.params = params;
		}
		
		private XmlRpcResponse execute(XmlRpcClient xmlRpcClient) {
			
			XmlRpcRequestBody body = new XmlRpcRequestBody(method);
			
			for(Object param : params) {
				body.param(param);
			}
						
			Request request = new Request.Builder()
					.url(xmlRpcClient.url)
					.post(RequestBody.create(MEDIA_TYPE, body.build()))
					.build();

			try {
				Response response = xmlRpcClient.httpClient.newCall(request).execute();
				return new XmlRpcResponse(response.body().string());
			} catch (IOException ex) {
				Logger.getLogger(XmlRpcClient.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			return null;
			
		}
		
		public String getMethodName() {
			return method;
		}
		
	}
	
	/**
	 * A very simple class to build the body of XML-RPC requests.
	 * Basically it uses a {@link StringBuilder} to append strings, the method
	 * name and parameters to generate the request.
	 */
	private static class XmlRpcRequestBody {

		private final StringBuilder builder;
		
		/**
		 * Starts creating a new XML-RPC body representation.
		 * 
		 * @param methodName the method name, that should be called
		 */
		private XmlRpcRequestBody(String methodName) {
			builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodCall><methodName>");
			builder.append(methodName).append("</methodName><params>");
		}
		
		public XmlRpcRequestBody param(Object param) {
			// TODO: search serializer and add parameter tag
			return this;
		}
		
		public String build() {
			return builder.append("</params></methodCall>").toString();
		}
		
	}
	
}
