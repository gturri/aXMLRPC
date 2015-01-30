package de.timroes.axmlrpc.suites.tests;

import de.timroes.axmlrpc.XmlRpcClient;
import de.timroes.axmlrpc.XmlRpcResponse;
import de.timroes.axmlrpc.suites.TestServerSuite;
import java.net.MalformedURLException;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Tim Roes - mail@timroes.de
 */
public class XmlRpcClientCallTests {
	
	private static XmlRpcClient client;
	
	@BeforeClass
	public static void setupClient() throws MalformedURLException {
		client = XmlRpcClient.forUrl(TestServerSuite.getServerUrl());
	}
	
	@Test
	public void callWithStringResponse() {
		XmlRpcResponse response = client.call("ping");
		assertEquals("pong", response.asString());
	}
	
}
