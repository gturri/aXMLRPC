package de.timroes.axmlrpc;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TestResponseParser {

	@Rule public final MockWebServer server = new MockWebServer();

	static final String CONTENT_TYPE = "Content-Type";
	static final String TYPE_XML = "text/xml; charset=utf-8";

	static final String METHOD = "method";

	@Test public void canParseString() throws Exception {
		XMLRPCClient client = new XMLRPCClient(server.url("/"));
		server.enqueue(new MockResponse().setBody(buildBody("<value><string>toto</string></value>")));

		Object response = client.call(METHOD);
		assertThat(response).isEqualTo("toto");

		RecordedRequest request = server.takeRequest();
		assertThat(request.getBody().readUtf8()).isEqualTo(buildRequestBody(METHOD));
		assertThat(request.getHeader(CONTENT_TYPE)).isEqualTo(TYPE_XML);
	}

	@Test public void canParseAsStringWhenTypeIsntExplicitelyProvided() throws Exception {
		XMLRPCClient client = new XMLRPCClient(server.url("/"), XMLRPCClient.FLAGS_DEFAULT_TYPE_STRING);
		server.enqueue(new MockResponse().setBody(buildBody("<value>toto</value>")));

		Object response = client.call(METHOD);
		assertThat(response).isEqualTo("toto");

		RecordedRequest request = server.takeRequest();
		assertThat(request.getBody().readUtf8()).isEqualTo(buildRequestBody(METHOD));
		assertThat(request.getHeader(CONTENT_TYPE)).isEqualTo(TYPE_XML);
	}

	@Test public void canParseInt() throws Exception {
		XMLRPCClient client = new XMLRPCClient(server.url("/"));
		server.enqueue(new MockResponse().setBody(buildBody("<value><i4>32</i4></value>")));

		Object response = client.call(METHOD);
		assertThat(response).isEqualTo(32);

		RecordedRequest request = server.takeRequest();
		assertThat(request.getBody().readUtf8()).isEqualTo(buildRequestBody(METHOD));
		assertThat(request.getHeader(CONTENT_TYPE)).isEqualTo(TYPE_XML);

		server.enqueue(new MockResponse().setBody(buildBody("<value><int>33</int></value>")));

		response = client.call(METHOD);
		assertThat(response).isEqualTo(33);

		request = server.takeRequest();
		assertThat(request.getBody().readUtf8()).isEqualTo(buildRequestBody(METHOD));
		assertThat(request.getHeader(CONTENT_TYPE)).isEqualTo(TYPE_XML);
	}

	@Test public void canParseBoolean() throws Exception {
		XMLRPCClient client = new XMLRPCClient(server.url("/"));
		server.enqueue(new MockResponse().setBody(buildBody("<value><boolean>1</boolean></value>")));

		Object response = client.call(METHOD);
		assertThat(response).isEqualTo(true);

		RecordedRequest request = server.takeRequest();
		assertThat(request.getBody().readUtf8()).isEqualTo(buildRequestBody(METHOD));
		assertThat(request.getHeader(CONTENT_TYPE)).isEqualTo(TYPE_XML);

		server.enqueue(new MockResponse().setBody(buildBody("<value><boolean>0</boolean></value>")));

		response = client.call(METHOD);
		assertThat(response).isEqualTo(false);

		request = server.takeRequest();
		assertThat(request.getBody().readUtf8()).isEqualTo(buildRequestBody(METHOD));
		assertThat(request.getHeader(CONTENT_TYPE)).isEqualTo(TYPE_XML);
	}

	private String buildBody(String body) {
		return "<methodResponse><params><param>" + body + "</param></params></methodResponse>";
	}

	private String buildRequestBody(String method) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<methodCall>\n<methodName>"
				+ method
				+ "</methodName>\n"
				+ "</methodCall>\n";
	}
}
