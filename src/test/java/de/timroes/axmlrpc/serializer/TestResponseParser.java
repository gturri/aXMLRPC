package de.timroes.axmlrpc.serializer;

import java.net.URL;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

import org.junit.Rule;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import de.timroes.axmlrpc.*;

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

	@Test
	public void canParseDateTime() throws Exception {
		java.util.TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		setMockWithXmlRpcContent("<value><dateTime.iso8601>2018-03-06T06:21:20Z</dateTime.iso8601></value>");
		assertEquals("Can parse normal datetime", new Date(118, 2, 6, 6,21,20), makeDummyCall());

		setMockWithXmlRpcContent("<value><dateTime.iso8601/></value>");
		assertNull("Should get null date because we use the flag to enable this behavior", makeDummyCall(XMLRPCClient.FLAGS_ACCEPT_NULL_DATES));

		boolean didThrow = false;
		setMockWithXmlRpcContent("<value><dateTime.iso8601/></value>");
		try {
			makeDummyCall(XMLRPCClient.FLAGS_NONE);
		} catch(Exception e){
			didThrow = true;
		}
		assertTrue("Should have thrown because date was empty and we used the default behavior", didThrow);
	}

	@Test
	public void robustToCWE611() throws Exception {
		setMockWithRawContent("<?xml version=\"1.0\"?> <!DOCTYPE replace [<!ENTITY ent SYSTEM \"http://localhost/malware\"> ]><methodResponse><params><param><value><string>&ent;</string></value></param></params></methodResponse>");
		boolean didThrowExpectedException = false;

		try {
		  makeDummyCall(XMLRPCClient.FLAGS_NONE);
		} catch(XMLRPCException e) {
			didThrowExpectedException = isCWE611ExceptedException(e);
			if (!didThrowExpectedException) {
				// if we don't get the exception we were expecting, rethrow so user has clues about what went wrong
				// and can troubleshoot the test more easily
				throw e;
			}
		}

		assertTrue("Should have thrown because this payload is malicious", didThrowExpectedException);
	}

	@Test
	public void canParseResponseWithCDATA() throws Exception {
		setMockWithXmlRpcContent("<value><string><![CDATA[ab<&>cd]]></string></value>");

		assertEquals("ab<&>cd", makeDummyCall());
	}

	private void setMockWithXmlRpcContent(String content){
	  setMockWithRawContent("<methodResponse><params><param>" + content + "</param></params></methodResponse>");
	}

	private void setMockWithRawContent(String rawContent) {
		stubFor(post(urlEqualTo(endPoint))
				.willReturn(aResponse()
						.withStatus(200)
						.withBody(rawContent)
						));
	}

	private boolean isCWE611ExceptedException(XMLRPCException e) {
		Throwable cause = e.getCause();
		if (cause == null) {
      return false;
		}
		return cause.getMessage().contains("DOCTYPE is disallowed");
	}

	private Object makeDummyCall() throws Exception {
		return makeDummyCall(XMLRPCClient.FLAGS_NONE);
	}

	private Object makeDummyCall(int flags) throws Exception {
		return new XMLRPCClient(new URL("http://localhost:" + port + endPoint), flags).call("dummy_method");
	}
}
