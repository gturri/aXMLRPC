package de.timroes.axmlrpc;

import java.net.URL;

import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

import org.junit.Rule;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import de.timroes.axmlrpc.XMLRPCClient;

public class TestResponseParser {
	private final int port = 8080;
	private final String endPoint = "/dummyEndPoint";

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(port);

	@Test
	public void canParseString() throws Exception {
		setMockWithXmlRpcContent("<value><string>toto</string></value>");
		assertEquals("toto", makeDummyCall());
	}

	@Test
	public void canParseAsStringWhenTypeIsntExplicitelyProvided() throws Exception {
		setMockWithXmlRpcContent("<value>toto</value>");
		assertEquals("toto", makeDummyCall(XMLRPCClient.FLAGS_DEFAULT_TYPE_STRING));
	}

	@Test
	public void canParseInt() throws Exception {
		setMockWithXmlRpcContent("<value><i4>32</i4></value>");
		assertEquals(32, makeDummyCall());

		setMockWithXmlRpcContent("<value><int>33</int></value>");
		assertEquals(33, makeDummyCall());
	}

	@Test
	public void canParseBoolean() throws Exception {
		setMockWithXmlRpcContent("<value><boolean>1</boolean></value>");
		assertEquals(true, makeDummyCall());

		setMockWithXmlRpcContent("<value><boolean>0</boolean></value>");
		assertEquals(false, makeDummyCall());
	}

	private void setMockWithXmlRpcContent(String content){
		stubFor(post(urlEqualTo(endPoint))
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("<methodResponse><params><param>" + content + "</param></params></methodResponse>")
						));
	}

	private Object makeDummyCall() throws Exception {
		return makeDummyCall(XMLRPCClient.FLAGS_NONE);
	}

	private Object makeDummyCall(int flags) throws Exception {
		return new XMLRPCClient(new URL("http://localhost:" + port + endPoint), flags).call("dummy_method");
	}
}
